package com.enterprise.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class InsurancePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsurancePlatformApplication.class, args);
    }

}
