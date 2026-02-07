package com.swaybridge.auth.model.dto.request;

import lombok.Data;

@Data
public class NonceReq {

    private String address;
    private Integer chainId;

}
