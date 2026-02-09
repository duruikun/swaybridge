package com.swaybridge.httpfeed.annotation;

import com.swaybridge.httpfeed.registrar.CheckingPendingEventScheduleRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CheckingPendingEventScheduleRegistrar.class)
public @interface EnableCheckingPendingEventSchedule {
}
