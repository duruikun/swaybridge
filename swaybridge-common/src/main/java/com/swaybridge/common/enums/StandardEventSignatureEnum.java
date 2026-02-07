package com.swaybridge.common.enums;

import lombok.Getter;

@Getter
public enum StandardEventSignatureEnum {

    /**
     * ERC20:  Transfer(address indexed from, address indexed to, uint256 value)
     * ERC721: Transfer(address indexed from, address indexed to, uint256 indexed tokenId)
     * ERC20 ERC721的Transfer事件签名完全一致: Transfer(address,address,uint256)
     */
    ERC20_ERC721_TRANSFER_EVENT("Transfer", "Transfer(address,address,uint256)", "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");

    private final String eventName;
    private final String eventSignature;
    private final String keccak256Hash;

    StandardEventSignatureEnum(String eventName, String eventSignature, String keccak256Hash) {
        this.eventName = eventName;
        this.eventSignature = eventSignature;
        this.keccak256Hash = keccak256Hash;
    }

}
