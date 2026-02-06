package com.swaybridge.common.enums;

import lombok.Getter;

@Getter
public enum StandardEventSignatureEnum {

    ERC721_TRANSFER_EVENT("Transfer(address,address,uint256)","0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");

    private final String eventSignature;
    private final String keccak256Hash;

    StandardEventSignatureEnum(String eventSignature, String keccak256Hash) {
        this.eventSignature = eventSignature;
        this.keccak256Hash = keccak256Hash;
    }

}
