package com.swaybridge.common.enums;

import lombok.Getter;

@Getter
public enum KafkaTopicEnum {

    ERC20_TRANSFER_TOPIC("erc20.event.transfer", "ERC20合约的Transfer事件Topic"),
    OTHER_TOPIC("other.topic", "其他Topic");

    private final String topic;
    private final String remark;

    KafkaTopicEnum(String topic, String remark) {
        this.topic = topic;
        this.remark = remark;
    }

}
