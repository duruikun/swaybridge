package com.swaybridge.datarepository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 与数据库表字段保持严格一致, 如果数据表发生修改, 需要同步修改com.swaybridge.common.model.persistence.entity和com.swaybridge.datarepository.entity
 */
@Data
@TableName("blockchain_event")
public class BlockchainEventPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long chainId;           // 链ID，如 1 / 11155111
    private String network;         // 网络名，如 mainnet / sepolia

    private Long blockNumber;       // 区块高度
    private String blockHash;       // 区块Hash
    private Long blockTimestamp;    // 区块时间戳（秒）

    private String txHash;          // 交易Hash
    private Integer txIndex;        // 交易在区块中的索引
    private Integer logIndex;       // Log在交易中的索引
    private Boolean removed;        // 是否被回滚（链重组） 默认false

    private String contractAddress; // 合约地址
    private String eventSignature;  // 事件签名，如 Transfer(address,address,uint256)
    private String eventName;       // 事件名，如 Transfer

    private String topic0;          // topic0 = keccak(event signature)
    private String topic1;
    private String topic2;
    private String topic3;
    private String topic4;          // 冗余保留
    private String topic5;          // 冗余保留

    private String data;            // 原始data字段（hex）
    private String decodedData;     // ABI解码后的参数（JSON）

    private String status;          // PENDING / CONFIRMED / REORGED
    private String extra;           // 预留扩展字段(JSON)

    private String source;          // 事件来源：WS / HTTP

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
