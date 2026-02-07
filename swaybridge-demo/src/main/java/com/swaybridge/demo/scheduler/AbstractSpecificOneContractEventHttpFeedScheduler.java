package com.swaybridge.demo.scheduler;

import com.swaybridge.httpfeed.core.AbstractSpecificOneContractEventHttpFeed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AbstractSpecificOneContractEventHttpFeedScheduler {

    @Autowired
    private List<AbstractSpecificOneContractEventHttpFeed> feeds;

    // 单位ms
    @Scheduled(initialDelayString = "${chain.http.scheduler.initial-delay}", fixedDelayString = "${chain.http.scheduler.feed-interval}")
    public void runFeeds() throws Exception {
        log.debug("Http扫盘 Scheduler started");
        for (AbstractSpecificOneContractEventHttpFeed feed : feeds) {
            feed.scanOnce();
        }
    }

}
