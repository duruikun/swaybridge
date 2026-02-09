package com.swaybridge.httpfeed.registrar;

import com.swaybridge.httpfeed.core.CheckPendingBeanFactory;
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
public class CheckingPendingEventScheduleRegistrar implements DisposableBean {

    private final List<CheckPendingBeanFactory> httpFeedList;
    private final TaskScheduler taskScheduler;

    @Value("${chain.http.scheduler.check-pending}")
    private long fixedDelayMs;

    private ScheduledFuture<?> future;

    public CheckingPendingEventScheduleRegistrar(List<CheckPendingBeanFactory> httpFeedList, ApplicationContext context) {
        this.httpFeedList = httpFeedList;
        // 独立调度器, 不影响全局调度器
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("check-pending-evnet-");
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    @PostConstruct
    public void register() {
        log.info("register checking pending event-http-feed schedule");
        this.future = taskScheduler.scheduleWithFixedDelay(() -> {
            try {
                log.info("定时任务执行 - checkPendingEventToCompleted()");
                for (CheckPendingBeanFactory httpFeed : this.httpFeedList) {
                    httpFeed.checkPendingEventToCompleted();
                }
            } catch (Exception e) {
                log.error("check pending event failed", e);
            }
        }, Duration.ofMillis(fixedDelayMs));
    }

    @PreDestroy
    @Override
    public void destroy() {
        if (this.future != null) {
            future.cancel(true);
        }
        log.info("checking pending event-http-feed schedule stopped");
    }
}
