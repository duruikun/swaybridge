package com.swaybridge.demo.log_offramp;

import com.swaybridge.common.enums.StandardEventSignatureEnum;
import com.swaybridge.ws_listener_core.log_offramp.LogOffRamp;
import org.web3j.protocol.core.methods.response.Log;

import java.util.List;

public class StandardErc721TransferOffRamp implements LogOffRamp {

    @Override
    public boolean allow(Log log) {

        List<String> topics = log.getTopics();

        if (topics == null || topics.size() != 4) {
            return false;
        }

        if (!StandardEventSignatureEnum.ERC20_ERC721_TRANSFER_EVENT.getKeccak256Hash().equalsIgnoreCase(topics.getFirst())) {
            return false;
        }

        // 可进一步增加 eth_call supportInterface 判断

        return true;
    }

}
