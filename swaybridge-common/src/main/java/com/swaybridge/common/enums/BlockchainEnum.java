package com.swaybridge.common.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum BlockchainEnum {

    /* ======================= Ethereum ======================= */

    ETHEREUM_MAINNET("ethereum", 1),
    ETHEREUM_GOERLI("goerli", 5),
    ETHEREUM_SEPOLIA("sepolia", 11155111),

    /* ======================= BNB Chain ======================= */

    BSC_MAINNET("bsc", 56),
    BSC_TESTNET("bsc-testnet", 97),

    /* ======================= Polygon ======================= */

    POLYGON_MAINNET("polygon", 137),
    POLYGON_MUMBAI("mumbai", 80001),
    POLYGON_AMOY("amoy", 80002), // 新测试网，逐步替代 Mumbai

    /* ======================= Arbitrum ======================= */

    ARBITRUM_ONE("arbitrum-one", 42161),
    ARBITRUM_NOVA("arbitrum-nova", 42170),
    ARBITRUM_SEPOLIA("arbitrum-sepolia", 421614),

    /* ======================= Optimism ======================= */

    OPTIMISM_MAINNET("optimism", 10),
    OPTIMISM_SEPOLIA("optimism-sepolia", 11155420),

    /* ======================= Avalanche ======================= */

    AVALANCHE_C("avalanche-c", 43114),
    AVALANCHE_FUJI("avalanche-fuji", 43113),

    /* ======================= Base ======================= */

    BASE_MAINNET("base", 8453),
    BASE_SEPOLIA("base-sepolia", 84532),

    /* ======================= Tron（非 EVM，但常见） ======================= */

    TRON_MAINNET("tron", 728126428),
    TRON_NILE("tron-nile", 3448148188L),

    /* ======================= Solana（非 EVM） ======================= */

    SOLANA_MAINNET("solana", -1),
    SOLANA_DEVNET("solana-devnet", -2),

    /* ======================= Unknown ======================= */

    UNKNOWN("unknown", -999);

    private final String name;
    private final long chainId;

    BlockchainEnum(String name, long chainId) {
        this.name = name;
        this.chainId = chainId;
    }

    /* ======================= 工具方法 ======================= */

    public static BlockchainEnum fromChainId(long chainId) {
        return Arrays.stream(values())
                .filter(c -> c.chainId == chainId)
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static BlockchainEnum fromName(String name) {
        return Arrays.stream(values())
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
