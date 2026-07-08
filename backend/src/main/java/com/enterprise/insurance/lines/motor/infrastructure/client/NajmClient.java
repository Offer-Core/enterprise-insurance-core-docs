package com.enterprise.insurance.lines.motor.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "najm-client", url = "${services.najm.url}")
public interface NajmClient {

    @GetMapping("/api/v1/vehicles/{chassisNumber}/accidents")
    AccidentHistory checkAccidentHistory(@PathVariable("chassisNumber") String chassisNumber);
}
