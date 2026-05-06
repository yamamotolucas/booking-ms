package br.com.booking_ms.dto.output;

import br.com.booking_ms.model.BarberServices;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record BarberServicesOutput(

        Long id,
        String name,
        BigDecimal price,
        int durationMinutes

){
    public BarberServicesOutput(BarberServices barberServices) {
        this(
                barberServices.getId(),
                barberServices.getName(),
                barberServices.getPrice().setScale(2, RoundingMode.HALF_UP),
                barberServices.getDurationMinutes()
        );
    }
}
