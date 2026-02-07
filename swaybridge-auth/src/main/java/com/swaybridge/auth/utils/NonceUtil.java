package com.swaybridge.auth.utils;

import cn.hutool.core.util.HexUtil;

import java.security.SecureRandom;

public class NonceUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static String randomNonce() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexUtil.encodeHexStr(bytes).toLowerCase();
    }

}
