package com.swaybridge.httpfeed.core;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swaybridge.common.utils.TimeUtil;
import com.swaybridge.datarepository.entity.BlockchainEventPO;
import com.swaybridge.datarepository.service.BlockchainEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

/**
 * WebSocket监听到事件, 就会立刻交给Kafka消费者消费, 消费者将blockchain_event推入数据库, 推入表中的数据, status均为pending
 * 该补偿器作用是: 由框架定时任务调用, 定期拉取表中所有pending状态的事件, 依次去链上查询其状态后更新
 */
@Slf4j
@Component
public class SimpleSepoliaCheckPendingEventHttpFeed implements CheckPendingBeanFactory {

    @Value("${chain.confirms}")
    private Integer chainConfirms;

    @Autowired
    @Qualifier("web3j-sepolia")
    private Web3j web3j;

    @Autowired
    private BlockchainEventService blockchainEventService;

    public void checkPendingEventToCompleted() throws Exception {
        BigInteger latestBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        if (latestBlockNumber.compareTo(BigInteger.valueOf(chainConfirms)) < 0) {
            return;
        }
        // 有效的最大区块高度
        BigInteger validBlockNumberMax = latestBlockNumber.subtract(BigInteger.valueOf(this.chainConfirms));

        LambdaQueryWrapper<BlockchainEventPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BlockchainEventPO::getStatus, "pending").lt(BlockchainEventPO::getBlockNumber, validBlockNumberMax);

        List<BlockchainEventPO> blockchainEventPOList = blockchainEventService.list(queryWrapper);

        if (CollectionUtil.isEmpty(blockchainEventPOList)) {
            return;
        }

        for (BlockchainEventPO eventPO : blockchainEventPOList) {
            checkEveryone(eventPO);
        }

    }

    private void checkEveryone(BlockchainEventPO event) {
        try {
            EthGetTransactionReceipt resp = web3j.ethGetTransactionReceipt(event.getTxHash()).send();
            // 交易还没进块, 极少数情况但需考虑
            if (resp.getTransactionReceipt().isEmpty()) return;
            TransactionReceipt receipt = resp.getTransactionReceipt().get();
            // 步骤一: 处理交易失败的情况
            if (!"0x1".equals(receipt.getStatus())) markFailed(event, "tx_failed");
            // 步骤二: 日志是否真的存在(防止reorg)
            boolean logExists = receipt.getLogs().stream().anyMatch(_log -> _log.getLogIndex().equals(BigInteger.valueOf(event.getLogIndex())) && _log.getTransactionHash().equals(event.getTxHash()));
            if (!logExists) {
                markFailed(event, "log_not_found");
                return;
            }
            // 步骤三: 再判断一下confirm数量(严谨, 一开始已经判断过了, 能走到这里, 说明已经满足配置文件中的确认数)

            markCompleted(event);
        } catch (Exception e) {
            log.error("check pending events failed, tx={}", event.getTxHash(), e);
        }
    }

    private void markCompleted(BlockchainEventPO event) {
        event.setStatus("completed");
        event.setUpdateTime(TimeUtil.now());
        blockchainEventService.updateById(event);
        log.info("mark completed tx={}", event.getTxHash());
    }

    private void markFailed(BlockchainEventPO event, String reason) {
        event.setStatus(reason);
        event.setUpdateTime(TimeUtil.now());
        blockchainEventService.updateById(event);
        log.warn("mark failed tx={}, reason={}", event.getTxHash(), reason);
    }

}
