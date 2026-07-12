package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.repository.BookingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingStatusLimitValidator implements BookingCreationValidator {

    private final BookingRepository bookingRepository;
    private final LocalDateTime now = LocalDateTime.now();
    private final List<BookingStatus> bookingStatusQuery = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    public BookingStatusLimitValidator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validate(Booking booking) {

        bookingRepository
        .findFirstByCustomerIdAndBookingStatusInAndStartTimeAfter(booking.getCustomerId(), bookingStatusQuery, now)
        .ifPresent(activeBooking -> {

            if (activeBooking.getBookingStatus() == BookingStatus.PENDING && activeBooking.getStartTime().isAfter(now))
                throw new ValidationException("Customer already has a pending booking. Please confirm or cancel the other pending booking.");

            if (activeBooking.getBookingStatus() == BookingStatus.CONFIRMED && activeBooking.getStartTime().isAfter(now))
                throw new ValidationException("Customer already has a confirmed booking.");

        });
    }
}
