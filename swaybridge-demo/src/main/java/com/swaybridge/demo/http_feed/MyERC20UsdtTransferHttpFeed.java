package com.swaybridge.demo.http_feed;

import com.swaybridge.common.enums.BlockchainEnum;
import com.swaybridge.common.enums.StandardEventSignatureEnum;
import com.swaybridge.common.model.persistence.entity.BlockchainEvent;
import com.swaybridge.datarepository.service.ChainHttpScanSyncService;
import com.swaybridge.httpfeed.core.AbstractSpecificOneContractEventHttpFeed;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.tx.Contract;

import java.math.BigInteger;
import java.util.List;

@Component
public class MyERC20UsdtTransferHttpFeed extends AbstractSpecificOneContractEventHttpFeed {

    private static final String CONTRACT_ADDRESS = "0x6B7D360173Be3846Ef2b2e95Ad8F9D48eE9E7195";

    private static final Event TRANSFER = new Event(
            StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getEventName(),
            List.of(
                    new TypeReference<Address>(true) {},
                    new TypeReference<Address>(true) {},
                    new TypeReference<Uint256>(false) {}
            )
    );

    protected MyERC20UsdtTransferHttpFeed(@Qualifier("web3j-sepolia") Web3j web3j, KafkaTemplate<String, String> kafkaTemplate, ChainHttpScanSyncService chainSyncService, @Value("${chain.http.scan-step}") int scanStep) {
        super(web3j, kafkaTemplate, chainSyncService, scanStep, BlockchainEnum.ETHEREUM_SEPOLIA);
    }

    @Override
    protected BigInteger chainId() {
        return BigInteger.valueOf(BlockchainEnum.ETHEREUM_SEPOLIA.getChainId());
    }

    @Override
    protected String contractAddress() {
        return CONTRACT_ADDRESS;
    }

    @Override
    protected List<Event> events() {
        return List.of(TRANSFER);
    }

    @Override
    protected BigInteger currentMaxBlock() throws Exception {
        BigInteger last = web3j.ethBlockNumber().send().getBlockNumber();
        return last.subtract(BigInteger.valueOf(12));   // 12 confirmations
    }

    @Override
    protected BlockchainEvent doTidyResult(Log log, Event event) {
        EventValues eventValues = Contract.staticExtractEventParameters(event, log);

        String from = (String) eventValues.getIndexedValues().get(0).getValue();
        String to = (String) eventValues.getIndexedValues().get(1).getValue();
        BigInteger value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();

        BlockchainEvent _event = new BlockchainEvent();
        _event.setEventName(event.getName());
        _event.setBlockHash(log.getBlockHash());

        System.out.println(log);

        return _event;
    }
}
