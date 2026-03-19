1：Swaybridge 框架介绍
1.1：诞生背景
在当前的以太坊生态中，合约开发已有非常成熟的工具，例如 Hardhat、Foundry 等，它们专注于智能合约的编写、测试、部署和调试。然而在应用层（后端服务、业务逻辑层），开发者仍然主要依赖基础的客户端库来与区块链交互：
- Java → web3j
- js、ts → ethers.js / web3.js
- Go → go-ethereum
  这些库提供了查询链上状态、获取交易回执、发送交易等基本能力，但其中最复杂、最容易出错的部分 —— 合约事件的可靠监听 —— 至今缺乏一个真正好用、规范化的解决方案。
  合约事件监听需要同时解决以下难题：
- 实时性与低延迟
- 事件不丢失、不重复
- 区块重组（reorg）处理
- 断线重连与历史事件回补
- 高并发场景下的性能与资源管理
- 事件处理逻辑的标准化与可维护性
  这些工作本应由框架层统一屏蔽，而不是让每位业务开发者重复造轮子。Swaybridge 正是为此而生，它专注于为 Java 开发者提供生产级别的合约事件可靠订阅 + 消息驱动能力，让开发者可以将主要精力放在业务逻辑串联和合约安全审计上。
  另外一个重要的背景是：中国拥有庞大的 Java 开发者群体，其中不少人正在转向 Web3 领域。过去，为了追求极致高并发性能，许多开发者不得不学习 Go 语言，但语言习惯的切换成本较高。
  随着 JDK 21 虚拟线程（Virtual Threads） 的正式发布和成熟，Java 在高并发场景下的表现已获得质的飞跃（轻量级线程模型 + 结构化并发），使其重新具备了在区块链应用后端大规模使用的竞争力。
  Swaybridge 的目标正是：让习惯 Java 的开发者能够零成本或低成本接入以太坊应用开发中最棘手的部分——可靠的合约事件监听与异步消息驱动体系。同时强烈建议开发者阅读并基于完整源码进行二次开发，这样可以最快理解底层设计意图，未来扩展和维护也会更加得心应手。

1.2：命名由来
Swaybridge = Sway + Bridge
- Sway：摇动、撼动，象征区块链技术对传统网络架构与价值传递方式的深刻变革
- Bridge：桥梁，寓意连接任意内容、任意链、任意系统
  两者结合，既表达了对区块链未来影响力的期待，也清晰传达了框架的核心价值 —— 为 Java 生态搭建通往以太坊世界的一座坚实桥梁。

1.3：核心能力
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
13. “可插拔”的声明式Springboot注解开发风格，所有功能均可通过启动类添加指定注解开启/关闭功能

---
2：环境准备
2.1：MySQL
services:
mysql-local:
image: mysql:8.0.35  # 使用 8.0.35 版本，这是一个稳定版本
container_name: mysql-local
environment:
- MYSQL_ROOT_PASSWORD=123456
ports:
- "3306:3306"
volumes:
# 使用 Docker 命名卷进行数据持久化
- mysql_local_data:/var/lib/mysql
# 默认配置文件
- /etc/mysql/conf.d:/etc/mysql/conf.d
command:
- --character-set-server=utf8mb4
- --collation-server=utf8mb4_unicode_ci
- --default-authentication-plugin=mysql_native_password

volumes:
mysql_local_data:
name: mysql_local_data
- 启动：docker-compose up -d
- 停止：docker-compose down
  2.2：Redis
  services:
  redis:
  image: redis:7-alpine
  container_name: redis-local
  ports:
  - "6379:6379"
  volumes:
  # 使用 Docker 命名卷进行数据持久化
  - redis_data:/data
  # Redis 默认配置文件
  - /etc/redis/redis.conf:/usr/local/etc/redis/redis.conf
  command: redis-server /usr/local/etc/redis/redis.conf --appendonly yes
  restart: unless-stopped

volumes:
redis_data:
# Docker 命名卷，数据会持久化在 Docker 管理的位置
name: redis_local_data
- 启动：docker-compose up -d
- 停止：docker-compose down
- 查看日志：docker logs -f redis-local
- 进入容器：docker exec -it redis-local redis-cli
  2.3：Kafka & Kafka-ui
  services:
  kafka:
  image: apache/kafka:3.7.0
  container_name: kafka
  hostname: kafka
  ports:
  - "9092:9092"
  volumes:
  - kafka-data:/var/lib/kafka/data
  environment:
  # ===== KRaft 模式核心配置 =====
  KAFKA_NODE_ID: 1
  KAFKA_PROCESS_ROLES: broker,controller
  KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093

      # ===== 监听器配置 =====
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER

      KAFKA_LISTENERS: >
        PLAINTEXT://0.0.0.0:9092,
        INTERNAL://0.0.0.0:29092,
        CONTROLLER://0.0.0.0:9093

      KAFKA_ADVERTISED_LISTENERS: >
        PLAINTEXT://127.0.0.1:9092,
        INTERNAL://kafka:29092

      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: >
        PLAINTEXT:PLAINTEXT,
        INTERNAL:PLAINTEXT,
        CONTROLLER:PLAINTEXT


      # ===== 单节点学习环境 =====
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0

  kafka-ui:
  image: provectuslabs/kafka-ui:v0.7.2
  container_name: kafka-ui
  ports:
  - "8080:8080"
  depends_on:
  - kafka
  environment:
  KAFKA_CLUSTERS_0_NAME: local-kafka
  # 注意, 这里是29092
  KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092

volumes:
kafka-data:
如果你对Kafka不熟悉，或者想避免出错，那就使用我上述提供的compose文件，是我测试过多个版本后确认无误的版本。
运行Kafka和Kafka-ui：
- docker compose up -d
- docker compose ps
  运行后，通过http://localhost:8080访问Kafka图形化界面，可通过以下命令对Kafka环境进行测试：
- 进入容器：
  docker exec -it kafka /bin/bash
  cd /opt/kafka/bin
- 列出所有主题：
  ./kafka-topics.sh --list --bootstrap-server localhost:9092
- 创建主题ricky：
  ./kafka-topics.sh --create --topic ricky --bootstrap-server localhost:9092
- 查看ricky主题详情：
  ./kafka-topics.sh --describe --topic ricky --bootstrap-server localhost:9092
- 给ricky主题中发送消息：
  ./kafka-console-producer.sh --topic ricky --bootstrap-server localhost:9092
> message1..
> msg2..
> msg3...

...

3：快速开始
3.1：源码下载（推荐）
https://github.com/duruikun/swaybridge-all
推荐使用基于源代码二开，项目clone到本地后，提供了swaybridge-demo模块，是一个Springboot Web服务，作为所有功能使用演示。

3.2：导入SQL
默认集成MySQL，使用其他数据库可替换数据库连接依赖。
- 创建swaybridge数据库
  -- 创建使用 utf8mb4 字符集的数据库（支持emoji和所有Unicode字符）
  CREATE DATABASE IF NOT EXISTS swaybridge CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
- 创建sway_user表
  记录登录用户信息，基于Sa-Token，实现Web3登录，具体登录流程后续讲解，属于附加功能，如不需要web3登录功能，可以忽略
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
  ) comment '全局用户表';
- 创建blockchain_event表
  记录所有被监听到的合约事件，实时监听和最终补偿的事件，都会记录在该表，通过唯一约束去重
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
- 创建chain_http_scan_sync表
  由于WebSocket监听存在断连等异常情况，该表记录了ws断连重连时，可能丢失的合约事件，通过HTTP补偿扫描区块的进度表
  create table chain_http_scan_sync
  (
  id               bigint auto_increment
  primary key,
  chain_id         bigint      not null comment '链ID，如 ETH=1',
  contract_address varchar(64) not null comment '合约地址',
  last_block       bigint      not null comment '最近已扫描完成的区块',
  update_time      timestamp   not null comment '最后更新时间',
  constraint uk_chain_contract unique (chain_id, contract_address)
  );

3.3：框架结构
[图片]
Maven模块化结构，每个模块对应各自功能，可插拔，按需使用，所有模块组合出框架的全部功能。
- swaybridge-auth：登录认证模块，默认提供了四个登录认证相关的接口，引入该模块，即具备web3登录认证功能
- swaybridge-common：公共模块，存储常量、枚举、模型以及智能合约相关（包括智能合约abi、bin文件和web3j生成的Java合约类）
- swaybridge-data-repository：数据访问模块，该模块提供访问MySQL的所有接口方法
- swaybridge-demo：示例模块，框架所有的功能，该模块均有详细示例
- swaybridge-http-feed：HTTP补偿模块，由于WebSocket监听存在断连情况，需要补偿断连-重连过程中新产生的事件，为数据最终一致性兜底
- swaybridge-ws-listener-core：WebSocket实时监听模块，实时监听合约事件

3.4：一切从swaybridge-demo模块开始
application.yml配置文件
开发者可以使用作者提供的区块链节点链接作为测试使用，真实项目中，请换成自己的Endpoint。
server:
port: 10000

web3:
# 网络配置
rpc:
ws-url-sepolia: wss://sepolia.infura.io/ws/v3/7efe2645a3054720a2041488c1e27329
http-endpoint-sepolia: https://sepolia.infura.io/v3/7efe2645a3054720a2041488c1e27329
http-endpoint-avalanche-fuji: https://avalanche-fuji.infura.io/v3/7efe2645a3054720a2041488c1e27329
http-endpoint-polygon-amoy: https://polygon-amoy.infura.io/v3/7efe2645a3054720a2041488c1e27329

spring:
data:
redis:
host: localhost
database: 0
port: 6379
datasource:
url: jdbc:mysql://localhost:3306/swaybridge?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
username: root
password: 123456
driver-class-name: com.mysql.cj.jdbc.Driver
kafka:
bootstrap-servers: 127.0.0.1:9092
producer:
key-serializer: org.apache.kafka.common.serialization.StringSerializer
value-serializer: org.apache.kafka.common.serialization.StringSerializer
acks: all
retries: 3
properties:
linger.ms: 5
enable.idempotence: true   # 生产者幂等

mybatis-plus:
mapper-locations:
- classpath:mapper/*.xml
- classpath*:mapper/**/*.xml
configuration:
map-underscore-to-camel-case: true
# log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

############## Sa-Token 配置 (文档: https://sa-token.cc) ##############
sa-token:
token-name: token           # token 名称（同时也是 cookie 名称）
timeout: 2592000            # token 有效期（单位：秒） 默认30天，-1 代表永久有效
active-timeout: -1          # token 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，默认-1 代表不限制，永不冻结
is-concurrent: true         # 是否允许同一账号多地同时登录 （为 true 时允许一起登录, 为 false 时新登录挤掉旧登录）
is-share: false             # 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个 token, 为 false 时每次登录新建一个 token）
token-style: random-32      # token 风格（默认可取值：uuid、simple-uuid、random-32、random-64、random-128、tik）
is-log: true                # 是否输出操作日志

chain:
confirms: 6
http:
scan-step: 2000           # http补偿扫描区块数量
scheduler:
feed-interval: 5000     # 定时任务执行一次http feed的周期 测试为了看效果, 调整成5000 5s, 生产环境需考虑, 推荐: 600000ms = 10min
initial-delay: 5000     # 服务启动到第一次执行的延迟时间
# check-pending: 3600000  # 检查pending事务的执行周期
check-pending: 5000  # 检查pending事务的执行周期

logging:
pattern:
dateformat: yyyy-MM-dd HH:mm:ss
以上配置文件非常简单，基本上都是spring项目的常规配置，唯一需要说的两个配置：
- web3.rpc.xxx：区块链网络节点相关的endpoint配置
   - ws-url-sepolia表示sepolia测试网的WebSocket节点；http-endpoint-sepolia表示sepolia测试网的http节点，以此类推…
- chain.xxx：HTTP补偿相关配置
   - confirms：表示区块确认数
   - http.xxx：上述配置文件注释中已介绍

启动类DemoApplication.java
@SpringBootApplication
@ComponentScan(basePackages = "com.swaybridge")
@MapperScan("com.swaybridge.datarepository.mapper")
@EnableSpecificEventListener
@EnableGlobalListener
@EnableCheckingPendingEventSchedule
@EnableEventHttpFeedSchedule
public class DemoApplication {
public static void main(String[] args) {
SpringApplication.run(DemoApplication.class, args);
}
}







