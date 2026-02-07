package com.swaybridge.demo.listener;

import cn.hutool.json.JSONUtil;
import com.swaybridge.common.enums.BlockchainEnum;
import com.swaybridge.common.enums.StandardEventSignatureEnum;
import com.swaybridge.common.model.persistence.entity.BlockchainEvent;
import com.swaybridge.common.utils.EventFactory;
import com.swaybridge.demo.log_offramp.StandardErc721TransferOffRamp;
import com.swaybridge.ws_listener_core.listener.AbstractGlobalTopicListener;
import com.swaybridge.ws_listener_core.log_offramp.LogOffRamp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventValues;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GlobalErc721TransferListener extends AbstractGlobalTopicListener {

    protected GlobalErc721TransferListener(@Value("${web3.rpc.ws-url}") String wsUrl) {
        super(wsUrl, null);
    }

    @Override
    protected String topic0() {
        return StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getKeccak256Hash();
    }

    @Override
    protected LogOffRamp logOffRamp() {
        return new StandardErc721TransferOffRamp();
    }

    @Override
    protected void onEvent(Log log) {

        BlockchainEvent event = new BlockchainEvent();
        event.setChainId(BlockchainEnum.ETHEREUM_SEPOLIA.getChainId());
        event.setNetwork(BlockchainEnum.ETHEREUM_SEPOLIA.getName());
        event.setBlockNumber(log.getBlockNumber().longValue());
        event.setBlockHash(log.getBlockHash());
        // web3j.ethGetBlockByHash(log.getBlockHash(), false).send().getBlock().getTimestamp().longValue()
        event.setBlockTimestamp(null);
        event.setTxHash(log.getTransactionHash());
        event.setTxIndex(log.getTransactionIndex().intValue());
        event.setLogIndex(log.getLogIndex().intValue());
        event.setRemoved(log.isRemoved());
        event.setContractAddress(log.getAddress());
        event.setEventName(StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getEventName());
        event.setTopic0(log.getTopics().getFirst());
        event.setTopic1(log.getTopics().size() > 1 ? log.getTopics().get(1) : null);
        event.setTopic2(log.getTopics().size() > 2 ? log.getTopics().get(2) : null);
        event.setTopic3(log.getTopics().size() > 3 ? log.getTopics().get(3) : null);
        event.setTopic4(log.getTopics().size() > 4 ? log.getTopics().get(4) : null);
        event.setTopic5(log.getTopics().size() > 5 ? log.getTopics().get(5) : null);
        event.setData(log.getData());
        event.setDecodedData(generateDecodedData(log));
        event.setSource("WS");
        event.setCreatedAt(LocalDateTime.now());

        System.out.println();
    }

    @SuppressWarnings("all")
    private String generateDecodedData(Log log) {
        Map<String, Object> decodedMap = new LinkedHashMap<>();
        EventValues eventValues = Contract.staticExtractEventParameters(EventFactory.stdErc721TransferEvent(), log);
        decodedMap.put("from", eventValues.getIndexedValues().get(0).getValue());
        decodedMap.put("to", eventValues.getIndexedValues().get(1).getValue());
        decodedMap.put("tokenId", eventValues.getIndexedValues().get(2).getValue());
        return JSONUtil.toJsonStr(decodedMap);
    }
}
