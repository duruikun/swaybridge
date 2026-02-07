package com.swaybridge.common.model.persistence.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 与数据库表字段保持严格一致, 如果数据表发生修改, 需要同步修改com.swaybridge.common.model.persistence.entity和com.swaybridge.datarepository.entity
 */
@Data
public class SwayUser implements Serializable {

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
