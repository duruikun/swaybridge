package com.swaybridge.demo;

import com.swaybridge.ws_listener_core.annotation.EnableGlobalListener;
import com.swaybridge.ws_listener_core.annotation.EnableSpecificEventListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

//@ComponentScan({"com.swaybridge.auth", "com.swaybridge.datarepository", "com.swaybridge.demo"})
@SpringBootApplication
@ComponentScan(basePackages = "com.swaybridge")
@MapperScan("com.swaybridge.datarepository.mapper")
@EnableSpecificEventListener
@EnableGlobalListener
@EnableScheduling
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}