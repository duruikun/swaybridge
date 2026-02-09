package com.swaybridge.demo.listener;

import cn.hutool.json.JSONUtil;
import com.swaybridge.common.enums.BlockchainEnum;
import com.swaybridge.common.enums.StandardEventSignatureEnum;
import com.swaybridge.common.model.persistence.entity.BlockchainEvent;
import com.swaybridge.common.smart_contracts.sepolia.usdt.USDT;
import com.swaybridge.common.utils.EventFactory;
import com.swaybridge.common.utils.TimeUtil;
import com.swaybridge.ws_listener_core.listener.AbstractSpecificEventListener;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventValues;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class UsdtTransferListener extends AbstractSpecificEventListener<USDT.TransferEventResponse> {

    private final static String usdtAddress = "0x1932277d08f673d045d264ae90097bb99c772d92";

    protected UsdtTransferListener(@Value("${web3.rpc.ws-url-sepolia}") String wsUrl) {
        super(wsUrl);
    }

    @Override
    protected Flowable<USDT.TransferEventResponse> subscribe(Web3j web3j) {
        // ⚠️ 每次重连都新建合约实例
        USDT usdt = USDT.load(usdtAddress, web3j, txManager, gasProvider);

        return usdt.transferEventFlowable(
                DefaultBlockParameterName.LATEST,
                DefaultBlockParameterName.LATEST
        );
    }

    @Override
    protected void onEvent(USDT.TransferEventResponse e) {
        BlockchainEvent event = new BlockchainEvent();
        event.setChainId(BlockchainEnum.ETHEREUM_SEPOLIA.getChainId());
        event.setNetwork(BlockchainEnum.ETHEREUM_SEPOLIA.getName());
        event.setBlockNumber(e.log.getBlockNumber().longValue());
        event.setBlockHash(e.log.getBlockHash());
        event.setBlockTimestamp(null);
        event.setTxHash(e.log.getTransactionHash());
        event.setTxIndex(e.log.getTransactionIndex().intValue());
        event.setLogIndex(e.log.getLogIndex().intValue());
        event.setRemoved(e.log.isRemoved());
        event.setContractAddress(e.log.getAddress());
        event.setContractTags("ERC20");
        event.setEventName(StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getEventName());
        event.setTopic0(e.log.getTopics().getFirst());
        event.setTopic1(e.log.getTopics().size() > 1 ? e.log.getTopics().get(1) : null);
        event.setTopic2(e.log.getTopics().size() > 2 ? e.log.getTopics().get(2) : null);
        event.setTopic3(e.log.getTopics().size() > 3 ? e.log.getTopics().get(3) : null);
        event.setTopic4(e.log.getTopics().size() > 4 ? e.log.getTopics().get(4) : null);
        event.setTopic5(e.log.getTopics().size() > 5 ? e.log.getTopics().get(5) : null);
        event.setData(e.log.getData());
        event.setDecodedData(generateDecodedData(e.log));
        event.setSource("ws-listener");
        event.setCreateTime(TimeUtil.now());

        System.out.println("UsdtTransferListenerSpecific: " + event.getContractAddress());
    }

    @SuppressWarnings("all")
    private String generateDecodedData(Log log) {
        Map<String, Object> decodedMap = new LinkedHashMap<>();
        EventValues eventValues = Contract.staticExtractEventParameters(EventFactory.stdErc20TransferEvent(), log);
        decodedMap.put("from", eventValues.getIndexedValues().get(0).getValue());
        decodedMap.put("to", eventValues.getIndexedValues().get(1).getValue());
        decodedMap.put("value", eventValues.getNonIndexedValues().get(0).getValue());
        return JSONUtil.toJsonStr(decodedMap);
    }

}
