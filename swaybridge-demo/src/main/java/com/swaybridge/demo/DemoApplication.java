package com.swaybridge.demo;

import com.swaybridge.ws_listener_core.annotation.EnableSpecificEventListener;
import com.swaybridge.ws_listener_core.annotation.EnableGlobalListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSpecificEventListener
@EnableGlobalListener
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}