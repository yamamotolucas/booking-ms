package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BookingPastDateValidator implements BookingCreationValidator {

    @Override
    public void validate(Booking booking) {
        if(booking.getStartTime().isBefore(LocalDateTime.now())) {
           throw new ValidationException("Cannot create a booking in the past.");
        }
    }

}
