package com.swaybridge.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class Web3jConfig {

    @Value("${web3.rpc.http-endpoint}")
    private String httpEndpoint;

    /**
     * 只用于补偿查询, 不订阅, 不轮询
     *
     * @return
     */
    @Bean("sepolia-web3j")
    public Web3j web3j() {
        ScheduledExecutorService dummy = Executors.newSingleThreadScheduledExecutor();
        return Web3j.build(new HttpService(httpEndpoint), Long.MAX_VALUE, dummy);
    }

}
