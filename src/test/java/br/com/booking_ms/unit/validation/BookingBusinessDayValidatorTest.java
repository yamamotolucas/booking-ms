package br.com.booking_ms.unit.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.validation.BookingBusinessDayValidator;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingBusinessDayValidator Unit Tests")
public class BookingBusinessDayValidatorTest {

    private final BookingBusinessDayValidator validator = new BookingBusinessDayValidator();

    @Test
    @DisplayName("Should throw ValidationException when booking is scheduled on Sunday")
    public void shouldThrowValidationExceptionWhenBookingIsOnSunday() {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2026, 9, 13, 10, 0));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(booking)
        );

        assertEquals("Bookings can only be made on business days.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when booking is scheduled on Monday")
    public void shouldThrowValidationExceptionWhenBookingIsOnMonday() {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2026, 9, 14, 14, 30));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(booking)
        );

        assertEquals("Bookings can only be made on business days.", exception.getMessage());
    }

    @ParameterizedTest(name = "Should pass validation for business day: 2026-09-{0}")
    @ValueSource(ints = {15, 16, 17, 18, 19})
    @DisplayName("Should not throw exception when booking is on valid business days (Tuesday to Saturday)")
    public void shouldPassValidationOnBusinessDays(int dayOfMonth) {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(2026, 9, dayOfMonth, 11, 0));

        // Act & Assert: não deve disparar nenhuma exceção
        assertDoesNotThrow(() -> validator.validate(booking));
    }
}