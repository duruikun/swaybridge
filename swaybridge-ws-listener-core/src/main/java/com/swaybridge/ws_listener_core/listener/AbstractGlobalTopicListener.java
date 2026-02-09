package com.swaybridge.ws_listener_core.listener;

import cn.hutool.core.collection.CollectionUtil;
import com.swaybridge.ws_listener_core.log_offramp.LogOffRamp;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 监听某一个 topic0 的全链日志，并保证“只有符合该 topic 语义的 Log”才会交给子类
 * <p>
 * 全链 topic0 监听抽象父类
 * 保证：只有通过 gate 的 Log 才会进入子类
 */
public abstract class AbstractGlobalTopicListener {

    private static final Logger log = LoggerFactory.getLogger(AbstractGlobalTopicListener.class);

    private final String wsUrl;
    private final List<String> contractAddressList;

    protected volatile Web3j web3j;
    protected volatile WebSocketService webSocketService;
    protected volatile ClientTransactionManager txManager;
    protected volatile ContractGasProvider gasProvider;

    private volatile Disposable subscription;
    private final AtomicBoolean restarting = new AtomicBoolean(false);

    private final ScheduledExecutorService watchdog =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, getClass().getSimpleName() + "-watchdog"));

    protected AbstractGlobalTopicListener(String wsUrl, List<String> contractAddressList) {
        this.wsUrl = wsUrl;
        this.contractAddressList = contractAddressList;
    }

    /* ============ 子类必须实现 ============ */

    protected abstract String topic0();

    protected abstract LogOffRamp logOffRamp();

    /**
     * 只会接收到「已通过 logOffRamp 的 Log」
     */
    protected abstract void onEvent(Log log);

    /* ============ 生命周期 ============ */

    public void start() {
        synchronized (this) {
            log.info("启动 GlobalTopicListener: {}", getClass().getSimpleName());
            rebuildClient();
            startSubscription();
            startWatchdog();
        }
    }

    public void shutdown() {
        synchronized (this) {
            log.info("关闭 GlobalTopicListener: {}", getClass().getSimpleName());
            stopSubscription();
            stopClient();
            watchdog.shutdownNow();
        }
    }

    /* ============ 内部实现 ============ */

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
        } catch (Exception ignored) {}
        web3j = null;
        webSocketService = null;
    }

    private void startSubscription() {

        EthFilter filter = new EthFilter(
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST,
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST,
                CollectionUtil.isEmpty(contractAddressList) ? Collections.emptyList() : contractAddressList
        ).addSingleTopic(topic0());

        subscription = web3j.ethLogFlowable(filter)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        this::handleLog,
                        e -> {
                            log.error("AbstractGlobalTopicListener 订阅异常，触发重启: {}", getClass().getSimpleName(), e);
                            restartAsync();
                        }
                );
    }

    private void handleLog(Log elog) {
        try {
            if (null == logOffRamp() || logOffRamp().allow(elog)) {
                onEvent(elog);
            }
        } catch (Exception e) {
            log.error("处理 Log 失败: {}", getClass().getSimpleName(), e);
        }
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
                log.warn("开始重启 GlobalTopicListener: {}", getClass().getSimpleName());
                stopSubscription();
                stopClient();
                TimeUnit.SECONDS.sleep(5);
                rebuildClient();
                startSubscription();
                log.info("GlobalTopicListener 重启成功: {}", getClass().getSimpleName());
            } catch (Exception e) {
                log.error("GlobalTopicListener 重启失败", e);
            }
        }
    }
}
