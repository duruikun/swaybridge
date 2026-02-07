package com.swaybridge.httpfeed.core;

/**
 * WebSocket监听到事件, 就会立刻交给Kafka消费者消费, 消费者将blockchain_event推入数据库, 推入表中的数据, status均为pending
 * 该补偿器作用是: 由框架定时任务调用, 定期拉取表中所有pending状态的事件, 依次去链上查询其状态后更新
 */
public class CheckPendingEventHttpFeed {
}
