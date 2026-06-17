package io.github.dbwhd5566.trafficqueuelab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TrafficQueueLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficQueueLabApplication.class, args);
    }

}
