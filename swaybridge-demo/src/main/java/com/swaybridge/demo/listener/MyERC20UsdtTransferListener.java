package com.swaybridge.demo.listener;

import com.swaybridge.common.smart_contracts.sepolia.MyERC20Usdt.MyERC20Usdt;
import com.swaybridge.ws_listener_core.listener.AbstractSpecificEventListener;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;

@Component
public class MyERC20UsdtTransferListener extends AbstractSpecificEventListener<MyERC20Usdt.TransferEventResponse> {

    private static final String CONTRACT_ADDRESS = "0x6B7D360173Be3846Ef2b2e95Ad8F9D48eE9E7195";

    protected MyERC20UsdtTransferListener(@Value("${web3.rpc.ws-url}") String wsUrl) {
        super(wsUrl);
    }

    @Override
    protected Flowable<MyERC20Usdt.TransferEventResponse> subscribe(Web3j web3j) {
        MyERC20Usdt myErc20Usdt = MyERC20Usdt.load(CONTRACT_ADDRESS, web3j, txManager, gasProvider);
        return myErc20Usdt.transferEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST);
    }

    @Override
    protected void onEvent(MyERC20Usdt.TransferEventResponse event) {
        System.out.println("MyERC20UsdtTransferListener onEvent");
    }
}
