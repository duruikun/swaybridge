package com.swaybridge.common.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TimeUtil {

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    private TimeUtil() {

    }

    public static LocalDateTime now() {
        return LocalDateTime.now(SHANGHAI_ZONE);
    }

}
