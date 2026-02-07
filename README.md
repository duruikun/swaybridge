## 严格约定

### 数据模型相关

1. swaybrige-common模块com.swaybridge.common.model.persistence.entity中定义与数据库表严格一对一的Java Bean
2. 业务模块需继承entity的类, 并加以PO后缀, 举例: entity包中 BlockchainEvent.java, 在业务模块中使用需要:
   BlockchainEventPO extends BlockchainEvent
3. 



### sql文件

#### blockchain_event.sql

```sql
CREATE TABLE blockchain_event
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',

    -- 基础链信息
    chain_id         BIGINT       NULL COMMENT '链ID，如 1 / 11155111',
    network          VARCHAR(32)  NULL COMMENT '网络名，如 mainnet / sepolia',

    -- 区块信息
    block_number     BIGINT       NULL COMMENT '区块高度',
    block_hash       VARCHAR(66)  NULL COMMENT '区块Hash',
    block_timestamp  BIGINT       NULL COMMENT '区块时间戳（秒）',

    -- 交易信息
    tx_hash          VARCHAR(66)  NULL COMMENT '交易Hash',
    tx_index         INT          NULL COMMENT '交易在区块中的索引',

    -- Log 定位
    log_index        INT          NULL COMMENT 'Log在交易中的索引',
    removed          BOOLEAN     DEFAULT FALSE COMMENT '是否被回滚（链重组）',

    -- 合约与事件
    contract_address VARCHAR(42)  NULL COMMENT '合约地址',
    event_signature  VARCHAR(255) NULL COMMENT '事件签名，如 Transfer(address,address,uint256)',
    event_name       VARCHAR(64)  NULL COMMENT '事件名，如 Transfer',

    -- Topics
    topic0           VARCHAR(66)  NULL COMMENT 'topic0 = keccak(event signature)',
    topic1           VARCHAR(66) DEFAULT NULL,
    topic2           VARCHAR(66) DEFAULT NULL,
    topic3           VARCHAR(66) DEFAULT NULL,
    topic4           VARCHAR(66) DEFAULT NULL,
    topic5           VARCHAR(66) DEFAULT NULL,

    -- 数据体
    data             TEXT COMMENT '原始data字段（hex）',
    decoded_data     JSON COMMENT 'ABI解码后的参数（JSON）',

    -- 状态控制
    status           VARCHAR(32) DEFAULT 'PENDING' COMMENT 'PENDING / CONFIRMED / REORGED',

    -- 扩展字段
    extra            JSON COMMENT '预留扩展字段',

    -- 事件来源
    source           VARCHAR(16)  NULL COMMENT '事件来源：WS / HTTP',

    -- 时间
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 唯一约束
    UNIQUE KEY uk_event (chain_id, tx_hash, log_index)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_block ON blockchain_event (chain_id, block_number);
CREATE INDEX idx_contract_event ON blockchain_event (contract_address, event_name);
CREATE INDEX idx_tx ON blockchain_event (tx_hash);
CREATE INDEX idx_topic0 ON blockchain_event (topic0);
```



#### sway_user.sql

```sql
create table sway_user
(
    id              bigint auto_increment primary key,
    chain_id        bigint       null,
    network         varchar(32)  null,
    address         varchar(128) null,
    username        varchar(64)  null,
    password        varchar(255) null,
    nick_name       varchar(64)  null,
    email           varchar(128) null,
    create_time     varchar(32)  null,
    update_time     varchar(32)  null,
    last_login_time varchar(32)  null,
    constraint uk_address_chain
        unique (address, chain_id)
)
    comment '全局用户表';
```

