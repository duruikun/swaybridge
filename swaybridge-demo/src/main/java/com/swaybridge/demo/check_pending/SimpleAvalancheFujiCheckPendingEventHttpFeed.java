package com.swaybridge.demo.check_pending;

import com.swaybridge.httpfeed.core.CheckPendingBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;

@Component
public class SimpleAvalancheFujiCheckPendingEventHttpFeed implements CheckPendingBeanFactory {

    @Autowired
    @Qualifier("web3j-avalanche-fuji")
    private Web3j web3j;

    @Override
    public void checkPendingEventToCompleted() throws Exception {
        BigInteger fuJiBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();
        System.out.println(Thread.currentThread().getName() + " Avalanche Fuji block number: " + fuJiBlockNumber);
    }
}
