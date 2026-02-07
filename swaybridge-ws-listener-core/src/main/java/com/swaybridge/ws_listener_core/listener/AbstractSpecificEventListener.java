package com.swaybridge.ws_listener_core.listener;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractSpecificEventListener<E> {

    private static final Logger log = LoggerFactory.getLogger(AbstractSpecificEventListener.class);

    private final String wsUrl;

    protected volatile Web3j web3j;
    protected volatile WebSocketService webSocketService;

    protected volatile ClientTransactionManager txManager;
    protected volatile ContractGasProvider gasProvider;

    private volatile Disposable subscription;

    private final AtomicBoolean restarting = new AtomicBoolean(false);

    private final ScheduledExecutorService watchdog =
            Executors.newSingleThreadScheduledExecutor(
                    r -> new Thread(r, getClass().getSimpleName() + "-watchdog")
            );

    protected AbstractSpecificEventListener(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    /**
     * 子类基于 Web3j 创建订阅
     */
    protected abstract Flowable<E> subscribe(Web3j web3j);

    /**
     * 子类处理事件
     */
    protected abstract void onEvent(E event);

    /* ================= 生命周期 ================= */

    public void start() {
        synchronized (this) {
            log.info("启动 Specific Listener: {}", getClass().getSimpleName());
            rebuildClient();
            startSubscription();
            startWatchdog();
        }
    }

    public void shutdown() {
        synchronized (this) {
            log.info("关闭 Specific Listener: {}", getClass().getSimpleName());
            stopSubscription();
            stopClient();
            watchdog.shutdownNow();
        }
    }

    /* ================= 内部实现 ================= */

    private void rebuildClient() {
        try {
            this.webSocketService = new WebSocketService(wsUrl, false);
            this.webSocketService.connect();

            this.web3j = Web3j.build(webSocketService);

            this.txManager = new ClientTransactionManager(web3j, null);
            this.gasProvider = new DefaultGasProvider();

        } catch (Exception e) {
            throw new RuntimeException("WebSocket 初始化失败", e);
        }
    }

    private void stopClient() {
        try {
            if (webSocketService != null) {
                webSocketService.close();
            }
        } catch (Exception ignored) {
        }
        web3j = null;
        webSocketService = null;
    }

    @SuppressWarnings("all")
    private void startSubscription() {
        subscription = subscribe(web3j)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        event -> this.onEvent(event),
                        err -> {
                            log.error("订阅异常，触发重启: {}", getClass().getSimpleName(), err);
                            restartAsync();
                        }
                );
    }

    private void stopSubscription() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
        subscription = null;
    }

    private void startWatchdog() {
        watchdog.scheduleAtFixedRate(() -> {
            try {
                web3j.ethBlockNumber().send();
            } catch (Exception e) {
                log.warn("WebSocket 心跳失败，准备重启: {}", getClass().getSimpleName());
                restartAsync();
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private void restartAsync() {
        if (!restarting.compareAndSet(false, true)) {
            return;
        }

        watchdog.execute(() -> {
            try {
                restartInternal();
            } finally {
                restarting.set(false);
            }
        });
    }

    private void restartInternal() {
        synchronized (this) {
            try {
                log.warn("开始重启 Specific Listener: {}", getClass().getSimpleName());
                stopSubscription();
                stopClient();
                TimeUnit.SECONDS.sleep(5);
                rebuildClient();
                startSubscription();
                log.info("Specific Listener 重启成功: {}", getClass().getSimpleName());
            } catch (Exception e) {
                log.error("Specific Listener 重启失败，将由 watchdog 再次尝试", e);
            }
        }
    }
}
