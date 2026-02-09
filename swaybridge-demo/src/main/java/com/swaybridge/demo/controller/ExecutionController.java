package com.swaybridge.demo.controller;

import com.swaybridge.common.model.dto.response.Result;
import com.swaybridge.httpfeed.core.CheckPendingEventHttpFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@RestController
@CrossOrigin
@RequestMapping("execution")
public class ExecutionController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("todo1")
    public Result<String> todo1() {
        kafkaTemplate.send("todo1.topic", "todo1");
        return Result.ok("推送Kafka成功");
    }

    @Autowired
    private CheckPendingEventHttpFeed feed;

    @GetMapping("todo2")
    public Result<BigInteger> todo2() throws Exception {
        feed.checkPendingEventToCompleted();
        return Result.ok();
    }

}
