package br.com.booking_ms.dto;

import java.math.BigDecimal;

public record ServicesDto(
        String name,
        BigDecimal price
) {
}
