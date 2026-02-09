package com.swaybridge.httpfeed.annotation;

import com.swaybridge.httpfeed.registrar.EventHttpFeedScheduleRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EventHttpFeedScheduleRegistrar.class)
public @interface EnableEventHttpFeedSchedule {
}
