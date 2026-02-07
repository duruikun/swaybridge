package com.swaybridge.common.model.persistence.entity;

import lombok.Data;

/**
 * HTTP RPC 扫描区块信息同步记录表
 * <p>
 * 用来补偿ws方案断线, 重启等意外情况下产生的事件
 * 与数据库表字段保持严格一致, 如果数据表发生修改, 需要同步修改com.swaybridge.common.model.persistence.entity和com.swaybridge.datarepository.entity
 */
@Data
public class ChainHttpScanSync {

    private Long id;
    private Long chainId;
    private String contractAddress;
    private Long lastBlock;
    private String updateTime;

}
