package com.swaybridge.ws_listener_core.listener_manager;

import com.swaybridge.ws_listener_core.listener.AbstractGlobalTopicListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

// 不需要@Component了, 由 @EnableEventListener 和 @EnableGlobalListener 注解管理
public class ListenerGlobalManager implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ListenerGlobalManager.class);

    private final List<AbstractGlobalTopicListener> globalTopicListeners;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ListenerGlobalManager(List<AbstractGlobalTopicListener> globalTopicListeners) {
        this.globalTopicListeners = globalTopicListeners;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) return;
        log.info("启动 Global Listener 数量: {}", globalTopicListeners.size());
        globalTopicListeners.forEach(AbstractGlobalTopicListener::start);
    }

    @Override
    public void stop() {
        if (!running.compareAndSet(true, false)) return;
        log.info("停止所有 Global Listener");
        globalTopicListeners.forEach(AbstractGlobalTopicListener::shutdown);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

}
