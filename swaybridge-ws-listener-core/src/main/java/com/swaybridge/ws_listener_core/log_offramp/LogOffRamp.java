package com.swaybridge.ws_listener_core.log_offramp;

import org.web3j.protocol.core.methods.response.Log;

/**
 * Log 语义闸门
 * 决定一个 Log 是否“符合某种事件语义”
 */
public interface LogOffRamp {

    /**
     * @return true  → 允许进入 onEvent
     * false → 丢弃
     */
    boolean allow(Log log);

}
