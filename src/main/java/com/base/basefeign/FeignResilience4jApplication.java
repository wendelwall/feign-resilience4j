package com.base.basefeign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
//@EnableCircuitBreaker
public class FeignResilience4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignResilience4jApplication.class, args);
    }

}
