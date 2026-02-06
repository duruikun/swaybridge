package com.swaybridge.ws_listener_core.annotation;

import com.swaybridge.ws_listener_core.listener_manager.ListenerManager;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ListenerManager.class)
public @interface EnableEventListener {
}