package com.swaybridge.httpfeed.core;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.swaybridge.common.enums.BlockchainEnum;
import com.swaybridge.common.model.persistence.entity.BlockchainEvent;
import com.swaybridge.common.utils.TimeUtil;
import com.swaybridge.datarepository.entity.ChainHttpScanSyncPO;
import com.swaybridge.datarepository.service.ChainHttpScanSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.List;

/**
 * 合约事件 HTTP 补偿
 * <p>
 * 用来补偿WebSocket断连, 重启等短暂意外情况下产生的未监听到的合约事件
 * 该抽象父类, 可以补偿某一具体合约的任意事件
 *
 */
@Slf4j
public abstract class AbstractSpecificOneContractEventHttpFeed {
    protected final Web3j web3j;
    protected final KafkaTemplate<String, String> kafkaTemplate;
    protected final ChainHttpScanSyncService chainSyncService;
    protected final int scanStep;   // 每次扫描区块数量
    protected final BlockchainEnum chainInfo;

    protected AbstractSpecificOneContractEventHttpFeed(Web3j web3j, KafkaTemplate<String, String> kafkaTemplate, ChainHttpScanSyncService chainSyncService, int scanStep, BlockchainEnum chainInfo) {
        this.web3j = web3j;
        this.kafkaTemplate = kafkaTemplate;
        this.chainSyncService = chainSyncService;
        this.scanStep = scanStep;
        this.chainInfo = chainInfo;
    }

    // 子类实现
    protected abstract BigInteger chainId();

    protected abstract String contractAddress();

    protected abstract List<Event> events();

    protected abstract BigInteger currentMaxBlock() throws Exception;   // // 当前可扫描到的最大区块(通常=latest-confirmBlocks)

    protected abstract BlockchainEvent doTidyResult(Log log, Event event);

    // 获取区块上指定合约扫描的最新进度
    private ChainHttpScanSyncPO loadOrInitProgress() {
        LambdaQueryWrapper<ChainHttpScanSyncPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChainHttpScanSyncPO::getChainId, this.chainId());
        queryWrapper.eq(ChainHttpScanSyncPO::getContractAddress, this.contractAddress());
        ChainHttpScanSyncPO one = chainSyncService.getOne(queryWrapper);
        if (ObjUtil.isEmpty(one)) {
            ChainHttpScanSyncPO _entity = new ChainHttpScanSyncPO();
            _entity.setChainId(this.chainId().longValue());
            _entity.setContractAddress(this.contractAddress());
            _entity.setLastBlock(0L);
            _entity.setUpdateTime(TimeUtil.now());
            chainSyncService.save(_entity);
            return _entity;
        }
        return one;
    }

    /**
     * 父类的 scanOnce 方法, 就是去扫描区块链, 拉取指定范围区块的某一合约的特定事件
     * 该方法需要由框架定时任务触发
     */
    @Transactional(rollbackFor = Exception.class)
    public void scanOnce() throws Exception {
        ChainHttpScanSyncPO progress = loadOrInitProgress();

        BigInteger from = BigInteger.valueOf(progress.getLastBlock()).add(BigInteger.ONE);
        BigInteger max = this.currentMaxBlock();
        if (from.compareTo(max) > 0) {
            log.info("无需扫盘 {} {}", chainId(), contractAddress());
            return;
        }
        BigInteger to = from.add(BigInteger.valueOf(scanStep - 1));
        if (to.compareTo(max) > 0) {
            to = max;
        }
        log.info("HTTP 扫盘 [{}] {} blocks {} -> {}", contractAddress(), chainId(), from, to);

        for (Event event : events()) {
            scanEvent(event, from, to);
        }

        progress.setLastBlock(to.longValue());
        progress.setUpdateTime(TimeUtil.now());

        LambdaUpdateWrapper<ChainHttpScanSyncPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChainHttpScanSyncPO::getChainId, this.chainId());
        updateWrapper.eq(ChainHttpScanSyncPO::getContractAddress, this.contractAddress());
        chainSyncService.update(progress, updateWrapper);
    }

    private void scanEvent(Event event, BigInteger from, BigInteger to) throws Exception {
        EthFilter filter = new EthFilter(new DefaultBlockParameterNumber(from), new DefaultBlockParameterNumber(to), contractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        EthLog ethLog = web3j.ethGetLogs(filter).send();
        for (EthLog.LogResult<?> r : ethLog.getLogs()) {
            Log _log = (Log) r.get();
            BlockchainEvent useEvent = doTidyResult(_log, event);
            // kafkaTemplate.send("chain.raw.event", useEvent.getTxHash(), JSONUtil.toJsonStr(useEvent));
            log.info("HTTP feed 推送成功 tx={}", _log.getTransactionHash());
        }
    }

}
