## 严格约定

### 数据模型相关

1. swaybrige-common模块com.swaybridge.common.model.persistence.entity中定义与数据库表严格一对一的Java Bean
2. 业务模块需继承entity的类, 并加以PO后缀, 举例: entity包中 BlockchainEvent.java, 在业务模块中使用需要:
   BlockchainEventPO extends BlockchainEvent
3. 如果你希望通过Java开发区块链智能合约相关业务, 并且希望提供以下功能:
   1. 标准的Springboot-Web应用，Clone到本地后，提供MVC项目结构Demo示例代码，清晰易上手
   2. Springboot模块化开发，模块分类、项目结构清晰，二开难度极低，低耦合，自由扩缩容，按需引入依赖即可拥有对应能力
   3. 完备的Web3登录鉴权功能（用户钱包登录、签名、验签、生成JWT返回（基于Sa-Token）、自动更新用户状态、Redis存储登录信息等）
   4. WebSocket实时监听某个智能合约的某个事件（比如你想监听地址为0x123..合约的Transfer事件，全自动运行，只需要在启动类开启@EnableSpecificEventListener注解）
   5. WebSocket实时监听整个区块链的某个事件（比如你想监听Sepolia上的所有Transfer事件，@EnableGlobalListener）
   6. 监听整个区块链事件时，支持自定义更具体的事件监听过滤器
   7. 完善的WebSocket链接管理机制和断连自动重启方案（无需配置，框架自带）
    8. WebSocket断连、重启时，针对未监听到遗漏的事件，框架提供完备的HTTP-RPC补偿方案（@EnableEventHttpFeedSchedule）
    9. 事件最终一致性状态确认：通过定时任务定时自动轮询pending状态的事件，检查其是否真正上链，并自动更新状态（框架全自动执行，只需要在启动类添加一个@EnableCheckingPendingEventSchedule注解搞定）
    10. 支持合约级、事件级的细粒度endpoint设置，极简配置，就能轻松实现监听不同链的不同合约事件
   11. 现成的事件持久化（MySQL）方案，开箱即用
   12. 现成的Kafka事件消息驱动模型，开箱即用
   13. ”可插拔“的声明式Springboot注解开发风格，所有功能均可通过启动类添加指定注解开启/关闭功能


### sql文件

#### blockchain_event.sql

```sql
CREATE TABLE blockchain_event
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自增主键',

    -- 基础链信息
    chain_id         BIGINT NULL COMMENT '链ID，如 1 / 11155111',
    network          VARCHAR(32) NULL COMMENT '网络名，如 mainnet / sepolia',

    -- 区块信息
    block_number     BIGINT NULL COMMENT '区块高度',
    block_hash       VARCHAR(66) NULL COMMENT '区块Hash',
    block_timestamp  BIGINT NULL COMMENT '区块时间戳（秒）',

    -- 交易信息
    tx_hash          VARCHAR(66) NULL COMMENT '交易Hash',
    tx_index         INT NULL COMMENT '交易在区块中的索引',

    -- Log 定位
    log_index        INT NULL COMMENT 'Log在交易中的索引',
    removed          BOOLEAN     DEFAULT FALSE COMMENT '是否被回滚（链重组）',

    -- 合约与事件
    contract_address VARCHAR(42) NULL COMMENT '合约地址',
    event_signature  VARCHAR(255) NULL COMMENT '事件签名，如 Transfer(address,address,uint256)',
    event_name       VARCHAR(64) NULL COMMENT '事件名，如 Transfer',

    -- Topics
    topic0           VARCHAR(66) NULL COMMENT 'topic0 = keccak(event signature)',
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
    source           VARCHAR(16) NULL COMMENT '事件来源：WS / HTTP',

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
    chain_id        bigint null,
    network         varchar(32) null,
    address         varchar(128) null,
    username        varchar(64) null,
    password        varchar(255) null,
    nick_name       varchar(64) null,
    email           varchar(128) null,
    create_time     varchar(32) null,
    update_time     varchar(32) null,
    last_login_time varchar(32) null,
    constraint uk_address_chain
        unique (address, chain_id)
) comment '全局用户表';
```



