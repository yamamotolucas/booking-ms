package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class BookingBusinessDayValidator implements BookingCreationValidator {

    @Override
    public void validate(Booking booking) {

        LocalDate bookingDate = booking.getStartTime().toLocalDate();
        DayOfWeek dayOfWeek = bookingDate.getDayOfWeek();

        if(dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.MONDAY)
            throw new ValidationException("Bookings can only be made on business days.");

    }
}
