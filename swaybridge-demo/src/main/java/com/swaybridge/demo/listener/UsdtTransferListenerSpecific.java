package com.swaybridge.demo.listener;

import com.swaybridge.common.smart_contracts.sepolia.usdt.USDT;
import com.swaybridge.ws_listener_core.listener.AbstractSpecificEventListener;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

@Component
public class UsdtTransferListenerSpecific extends AbstractSpecificEventListener<USDT.TransferEventResponse> {

    private final String usdtAddress = "0x1932277d08f673d045d264ae90097bb99c772d92";

    protected UsdtTransferListenerSpecific(@Value("${web3.rpc.ws-url}") String wsUrl) {
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
    protected void onEvent(USDT.TransferEventResponse event) {
        System.out.println("event = " + event);
    }

}
