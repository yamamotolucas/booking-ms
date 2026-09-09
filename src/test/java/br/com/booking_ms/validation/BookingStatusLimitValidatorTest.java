package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.repository.BookingRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingStatusLimitValidator Unit Tests")
public class BookingStatusLimitValidatorTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingStatusLimitValidator validator;

    private final List<BookingStatus> expectedStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    @Test
    @DisplayName("Should throw ValidationException when customer has an active PENDING booking in the future")
    public void shouldThrowValidationExceptionWhenCustomerHasPendingBooking() {
        // Arrange
        Long customerId = 10L;
        Booking newBooking = new Booking();
        newBooking.setCustomerId(customerId);

        Booking activePendingBooking = new Booking();
        activePendingBooking.setBookingStatus(BookingStatus.PENDING);
        activePendingBooking.setStartTime(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findFirstByCustomerIdAndBookingStatusInAndStartTimeAfter(
                eq(customerId),
                eq(expectedStatuses),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(activePendingBooking));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(newBooking)
        );

        assertEquals("Customer already has a pending booking. Please confirm or cancel the other pending booking.", exception.getMessage());
        verify(bookingRepository, times(1))
                .findFirstByCustomerIdAndBookingStatusInAndStartTimeAfter(eq(customerId), eq(expectedStatuses), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when customer has an active CONFIRMED booking in the future")
    public void shouldThrowValidationExceptionWhenCustomerHasConfirmedBooking() {
        // Arrange
        Long customerId = 10L;
        Booking newBooking = new Booking();
        newBooking.setCustomerId(customerId);

        Booking activeConfirmedBooking = new Booking();
        activeConfirmedBooking.setBookingStatus(BookingStatus.CONFIRMED);
        activeConfirmedBooking.setStartTime(LocalDateTime.now().plusDays(1));

        when(bookingRepository.findFirstByCustomerIdAndBookingStatusInAndStartTimeAfter(
                eq(customerId),
                eq(expectedStatuses),
                any(LocalDateTime.class)
        )).thenReturn(Optional.of(activeConfirmedBooking));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(newBooking)
        );

        assertEquals("Customer already has a confirmed booking.", exception.getMessage());
        verify(bookingRepository, times(1))
                .findFirstByCustomerIdAndBookingStatusInAndStartTimeAfter(eq(customerId), eq(expectedStatuses), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should pass validation without exception when customer has no active bookings")
    public void shouldPassValidationWhenNoActiveBookingExists() {
        // Arrange
        Long customerId = 10L;
        Booking newBooking = new Booking();
        newBooking.setCustomerId(customerId);

        when(bookingRepository.findFirstByCustomerIdAndBookingStatusInAndStartTimeAfter(
                eq(customerId),
                eq(expectedStatuses),
                any(LocalDateTime.class)
        )).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(newBooking));
        verify(bookingRepository, times(1))
                .findFirstByCustomerIdAndBookingStatusInAndStartTimeAfter(eq(customerId), eq(expectedStatuses), any(LocalDateTime.class));
    }
}