package com.swaybridge.httpfeed.registrar;

import com.swaybridge.httpfeed.core.AbstractSpecificOneContractEventHttpFeed;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Slf4j
//@Configuration
public class EventHttpFeedScheduleRegistrar implements DisposableBean {

    private final List<AbstractSpecificOneContractEventHttpFeed> feeds;
    private final TaskScheduler taskScheduler;

    @Value("${chain.http.scheduler.feed-interval}")
    private long fixedDelayMs;

    private ScheduledFuture<?> future;

    public EventHttpFeedScheduleRegistrar(List<AbstractSpecificOneContractEventHttpFeed> feeds, ApplicationContext context) {
        this.feeds = feeds;
        // 独立调度器, 不影响全局调度器
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("event-http-feed-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    @PostConstruct
    public void register() {
        log.info("register event http feed schedule 注册http扫盘");
        this.future = taskScheduler.scheduleWithFixedDelay(() -> {
            try {
                for (AbstractSpecificOneContractEventHttpFeed feed : this.feeds) {
                    feed.scanOnce();
                }
            } catch (Exception e) {
                log.error("register event http feed failed", e);
            }
        }, Duration.ofMillis(fixedDelayMs));
    }


    @PreDestroy
    @Override
    public void destroy() throws Exception {
        if (this.future != null) {
            future.cancel(true);
        }
        log.info("event-http-feed schedule stopped");
    }

}
