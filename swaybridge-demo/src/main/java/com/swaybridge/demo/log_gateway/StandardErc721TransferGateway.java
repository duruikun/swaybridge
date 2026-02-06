package com.swaybridge.demo.log_gateway;

import com.swaybridge.common.enums.StandardEventSignatureEnum;
import com.swaybridge.ws_listener_core.log_gateway.LogGateway;
import org.web3j.protocol.core.methods.response.Log;

import java.util.List;

public class StandardErc721TransferGateway implements LogGateway {

    @Override
    public boolean allow(Log log) {

        List<String> topics = log.getTopics();
        if (topics == null || topics.size() != 4) {
            return false;
        }
        if (!StandardEventSignatureEnum.ERC721_TRANSFER_EVENT.getKeccak256Hash().equalsIgnoreCase(topics.get(0))) {
            return false;
        }

        return true;
    }

}
