package br.com.booking_ms.integration.factory;

import br.com.booking_ms.model.BarberServices;
import java.math.BigDecimal;

public class BarberServiceFactory {

    public static BarberServices createBarberService() {
        BarberServices service = new BarberServices();

        service.setName("Haircut");
        service.setPrice(new BigDecimal("35.00"));
        service.setDurationMinutes(30);

        return service;
    }
}