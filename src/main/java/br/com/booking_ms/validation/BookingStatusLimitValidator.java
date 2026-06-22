package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.repository.BookingRepository;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingStatusLimitValidator implements BookingCreationValidator {

    private final BookingRepository bookingRepository;

    public BookingStatusLimitValidator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validate(Booking booking) {

        bookingRepository
        .findFirstByCustomerIdAndBookingStatusIn(booking.getCustomerId(), List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED))
        .ifPresent(activeBooking -> {
            if (activeBooking.getBookingStatus() == BookingStatus.PENDING) {
                throw new ValidationException("Customer already has a pending booking. Please confirm or cancel the other pending booking.");
            }

            if (activeBooking.getBookingStatus() == BookingStatus.CONFIRMED) {
                throw new ValidationException("Customer already has a confirmed booking.");
            }
        });
    }
}
