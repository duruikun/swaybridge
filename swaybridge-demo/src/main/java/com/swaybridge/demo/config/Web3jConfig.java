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

    @Value("${web3.rpc.http-endpoint-sepolia}")
    private String httpEndpointSepolia;

    @Value("${web3.rpc.http-endpoint-avalanche-fuji}")
    private String httpEndpointAvalancheFuji;

    @Value("${web3.rpc.http-endpoint-polygon-amoy}")
    private String httpEndpointPolygonAmoy;

    /**
     * 只用于补偿查询, 不订阅, 不轮询
     *
     * @return
     */
    @Bean("web3j-sepolia")
    public Web3j web3jSepolia() {
        ScheduledExecutorService dummy = Executors.newSingleThreadScheduledExecutor();
        return Web3j.build(new HttpService(httpEndpointSepolia), Long.MAX_VALUE, dummy);
    }

    @Bean("web3j-avalanche-fuji")
    public Web3j web3jAvalancheFuji() {
        ScheduledExecutorService dummy = Executors.newSingleThreadScheduledExecutor();
        return Web3j.build(new HttpService(httpEndpointAvalancheFuji), Long.MAX_VALUE, dummy);
    }

    @Bean("web3j-polygon-amoy")
    public Web3j web3jPolygonAmoy() {
        ScheduledExecutorService dummy = Executors.newSingleThreadScheduledExecutor();
        return Web3j.build(new HttpService(httpEndpointPolygonAmoy), Long.MAX_VALUE, dummy);
    }

}
