package br.com.booking_ms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-ms")
public interface BarberClient {

    @GetMapping("/v1/barber/findById/{id}")
    String getBarberName(@PathVariable("id") String id);

}
