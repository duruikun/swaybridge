package com.swaybridge.demo.http_feed.kafka_consumer;

import cn.hutool.json.JSONUtil;
import com.swaybridge.common.enums.BlockchainEnum;
import com.swaybridge.datarepository.entity.BlockchainEventPO;
import com.swaybridge.datarepository.service.BlockchainEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class MyERC20UsdtTransferConsumer {

    @Autowired
    private BlockchainEventService blockchainEventService;

    @KafkaListener(topics = "chain.raw.event", containerFactory = "kafkaListenerContainerFactory", groupId = "random-group-id")
    public void consumer1(String data, Acknowledgment ack) {

        try {

            BlockchainEventPO eventPO = JSONUtil.toBean(data, BlockchainEventPO.class);

            eventPO.setId(null);
            eventPO.setChainId(BlockchainEnum.ETHEREUM_SEPOLIA.getChainId());
            // ...省略
            eventPO.setSource("http-feed(kafka consumer)");

            blockchainEventService.save(eventPO);

            ack.acknowledge();

            System.out.println("Received data: " + data);
        } catch (Exception e) {
            // ignore
        }

    }

}
