package com.enterprise.insurance.lines.motor.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "yakeen-client", url = "${services.yakeen.url}")
public interface YakeenClient {

    @GetMapping("/api/v1/validate/{nationalId}")
    boolean validateNationalId(@PathVariable("nationalId") String nationalId);
}
