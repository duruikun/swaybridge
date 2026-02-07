package com.swaybridge.auth.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VerifyReq {

    private String address;

    @JsonProperty("chain_id")
    private Long chainId;

    private String message;

    private String signature;

}

