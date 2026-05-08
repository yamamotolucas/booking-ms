package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.repository.BookingRepository;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class BookingDailyLimitValidator implements BookingCreationValidator{

    private final BookingRepository bookingRepository;

    public BookingDailyLimitValidator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validate(Booking booking) {
        Long customerId = booking.getCustomerId();
        LocalDate bookingDate = booking.getStartTime().toLocalDate();

        boolean alreadyExists = bookingRepository.existsByDateAndCustomerId(bookingDate, customerId);

        if (alreadyExists) {
            throw new ValidationException("Customer already has a booking scheduled for this date.");
        }
    }
}
