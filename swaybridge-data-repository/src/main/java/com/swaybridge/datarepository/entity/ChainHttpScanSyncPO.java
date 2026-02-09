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
@TableName("chain_http_scan_sync")
public class ChainHttpScanSyncPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chainId;
    private String contractAddress;
    private Long lastBlock;

    private LocalDateTime updateTime;

}
