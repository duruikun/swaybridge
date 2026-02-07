package com.swaybridge.datarepository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 与数据库表字段保持严格一致, 如果数据表发生修改, 需要同步修改com.swaybridge.common.model.persistence.entity和com.swaybridge.datarepository.entity
 */
@Data
@TableName("sway_user")
public class SwayUserPO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chainId;
    private String network;
    private String address;
    private String username;
    private String password;
    private String nickName;
    private String email;
    private String createTime;
    private String updateTime;
    private String lastLoginTime;

}
