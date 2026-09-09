package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.repository.BookingRepository;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingDailyLimitValidator Unit Tests")
public class BookingDailyLimitValidatorTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingDailyLimitValidator validator;

    @Test
    @DisplayName("Should throw ValidationException when customer already has a booking on the requested date")
    public void shouldThrowValidationExceptionWhenCustomerAlreadyHasBookingOnDate() {
        // Arrange
        Long customerId = 15L;
        LocalDate bookingDate = LocalDate.of(2026, 9, 15);
        LocalDateTime bookingDateTime = bookingDate.atTime(10, 0);

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStartTime(bookingDateTime);

        when(bookingRepository.existsByDateAndCustomerId(bookingDate, customerId)).thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(booking)
        );

        assertEquals("Customer already has a booking scheduled for this date.", exception.getMessage());
        verify(bookingRepository, times(1)).existsByDateAndCustomerId(bookingDate, customerId);
    }

    @Test
    @DisplayName("Should pass validation without throwing exception when customer has no bookings on the requested date")
    public void shouldPassValidationWhenCustomerHasNoBookingOnDate() {
        // Arrange
        Long customerId = 15L;
        LocalDate bookingDate = LocalDate.of(2026, 9, 15);
        LocalDateTime bookingDateTime = bookingDate.atTime(14, 30);

        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setStartTime(bookingDateTime);

        when(bookingRepository.existsByDateAndCustomerId(bookingDate, customerId)).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(booking));
        verify(bookingRepository, times(1)).existsByDateAndCustomerId(bookingDate, customerId);
    }
}