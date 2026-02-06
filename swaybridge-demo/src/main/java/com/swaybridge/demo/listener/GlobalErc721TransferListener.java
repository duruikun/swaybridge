package com.swaybridge.demo.listener;

import com.swaybridge.common.enums.StandardEventSignatureEnum;
import com.swaybridge.demo.log_gateway.StandardErc721TransferGateway;
import com.swaybridge.ws_listener_core.listener.AbstractGlobalTopicListener;
import com.swaybridge.ws_listener_core.log_gateway.LogGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.Log;

@Component
public class GlobalErc721TransferListener extends AbstractGlobalTopicListener {

    protected GlobalErc721TransferListener(@Value("${web3.rpc.ws-url}") String wsUrl) {
        super(wsUrl, null);
    }

    @Override
    protected String topic0() {
        return StandardEventSignatureEnum.ERC721_TRANSFER_EVENT.getKeccak256Hash();
    }

    @Override
    protected LogGateway gate() {
        return new StandardErc721TransferGateway();
    }

    @Override
    protected void onEvent(Log log) {
        System.out.println(log.getAddress());
    }
}
