package br.com.booking_ms.dto.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record BookingCreateInput(

        @NotNull(message = "Barber ID is required")
        Long barberId,

        @NotEmpty(message = "Services are required")
        List<Long> services,

        @NotNull(message = "StartTime is required.")
        LocalDateTime startTime //aaaa-mm-ddThh:mm:ss

) {
}
