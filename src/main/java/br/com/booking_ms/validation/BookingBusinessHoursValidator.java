package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class BookingBusinessHoursValidator implements BookingCreationValidator {

    private final LocalTime startTime = LocalTime.of(8, 0);
    private final LocalTime endTime = LocalTime.of(17, 0);

    @Override
    public void validate(Booking booking) {

        LocalTime bookingTime = booking.getStartTime().toLocalTime();
        if(bookingTime.isBefore(startTime) || bookingTime.isAfter(endTime))
            throw new ValidationException("Bookings must be made between 08:00 and 17:00.");

    }
}
