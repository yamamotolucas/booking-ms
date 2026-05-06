package br.com.booking_ms.dto.input;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BarberServicesInput(

        @NotBlank(message = "Name is required.")
        String name,

        @NotNull(message = "Price is required.")
        @Positive(message = "Price must be a positive value.")
        BigDecimal price,

        @NotNull(message = "Duration is required.")
        @Positive(message = "Duration must be a positive value.")
        int durationMinutes

) {
}
