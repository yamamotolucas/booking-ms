package br.com.booking_ms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "barberClient",
        url = "${identity-ms.url}",
        fallback = BarberClientFallback.class
)
public interface BarberClient {

    @GetMapping("/v1/barber/{id}")
    String getBarberName(@PathVariable("id") String id);

}
