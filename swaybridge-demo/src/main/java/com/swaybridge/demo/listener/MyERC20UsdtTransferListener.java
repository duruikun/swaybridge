package com.swaybridge.demo.listener;

import cn.hutool.json.JSONUtil;
import com.swaybridge.common.enums.BlockchainEnum;
import com.swaybridge.common.enums.StandardEventSignatureEnum;
import com.swaybridge.common.smart_contracts.sepolia.MyERC20Usdt.MyERC20Usdt;
import com.swaybridge.common.utils.EventFactory;
import com.swaybridge.common.utils.TimeUtil;
import com.swaybridge.datarepository.entity.BlockchainEventPO;
import com.swaybridge.datarepository.service.BlockchainEventService;
import com.swaybridge.ws_listener_core.listener.AbstractSpecificEventListener;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventValues;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class MyERC20UsdtTransferListener extends AbstractSpecificEventListener<MyERC20Usdt.TransferEventResponse> {

    @Autowired
    private BlockchainEventService blockchainEventService;

    private static final String CONTRACT_ADDRESS = "0x6B7D360173Be3846Ef2b2e95Ad8F9D48eE9E7195";

    protected MyERC20UsdtTransferListener(@Value("${web3.rpc.ws-url}") String wsUrl) {
        super(wsUrl);
    }

    @Override
    protected Flowable<MyERC20Usdt.TransferEventResponse> subscribe(Web3j web3j) {
        MyERC20Usdt myErc20Usdt = MyERC20Usdt.load(CONTRACT_ADDRESS, web3j, txManager, gasProvider);
        return myErc20Usdt.transferEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST);
    }

    @SuppressWarnings("all")
    @Override
    protected void onEvent(MyERC20Usdt.TransferEventResponse event) {
        System.out.println("MyERC20UsdtTransferListener onEvent");

        BlockchainEventPO eventPO = new BlockchainEventPO();

        eventPO.setId(null);
        eventPO.setChainId(BlockchainEnum.ETHEREUM_SEPOLIA.getChainId());
        eventPO.setNetwork(BlockchainEnum.ETHEREUM_SEPOLIA.getName());

        eventPO.setBlockNumber(event.log.getBlockNumber().longValue());
        eventPO.setBlockHash(event.log.getBlockHash());
        eventPO.setBlockTimestamp(null);

        eventPO.setTxHash(event.log.getTransactionHash());
        eventPO.setTxIndex(event.log.getTransactionIndex().intValue());
        eventPO.setLogIndex(event.log.getLogIndex().intValue());
        eventPO.setRemoved(event.log.isRemoved());

        eventPO.setContractAddress(event.log.getAddress());
        eventPO.setEventSignature(StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getEventSignature());
        eventPO.setEventName(StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getEventName());

        eventPO.setTopic0(event.log.getTopics().get(0));
        eventPO.setTopic1(event.log.getTopics().size() > 1 ? event.log.getTopics().get(1) : null);
        eventPO.setTopic2(event.log.getTopics().size() > 2 ? event.log.getTopics().get(2) : null);
        eventPO.setTopic3(event.log.getTopics().size() > 3 ? event.log.getTopics().get(3) : null);
        eventPO.setTopic4(event.log.getTopics().size() > 4 ? event.log.getTopics().get(4) : null);
        eventPO.setTopic5(event.log.getTopics().size() > 5 ? event.log.getTopics().get(5) : null);

        eventPO.setData(event.log.getData());
        eventPO.setDecodedData(generateDecodedData(event.log));

        eventPO.setStatus("pending");
        eventPO.setExtra(null);

        eventPO.setSource("ws-listener");

        eventPO.setCreateTime(TimeUtil.now());

        blockchainEventService.save(eventPO);
        log.info("MyERC20UsdtTransferListener onEvent save to db, tx={}", eventPO.getTxHash());

    }

    private String generateDecodedData(Log log) {
        Map<String, Object> decodedMap = new LinkedHashMap<>();

        EventValues eventValues = Contract.staticExtractEventParameters(EventFactory.stdErc20TransferEvent(), log);

        decodedMap.put("from", eventValues.getIndexedValues().get(0).getValue());
        decodedMap.put("to", eventValues.getIndexedValues().get(1).getValue());
        decodedMap.put("value", eventValues.getNonIndexedValues().get(0).getValue());

        return JSONUtil.toJsonStr(decodedMap);
    }
}
