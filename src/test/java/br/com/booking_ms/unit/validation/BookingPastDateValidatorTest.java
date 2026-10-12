package br.com.booking_ms.unit.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.validation.BookingPastDateValidator;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingPastDateValidator Unit Tests")
public class BookingPastDateValidatorTest {

    private final BookingPastDateValidator validator = new BookingPastDateValidator();

    @Test
    @DisplayName("Should throw ValidationException when booking start time is in the past")
    public void shouldThrowValidationExceptionWhenStartTimeIsInThePast() {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.now().minusHours(1));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(booking)
        );

        assertEquals("Cannot create a booking in the past.", exception.getMessage());
    }

    @Test
    @DisplayName("Should pass validation when booking start time is in the future")
    public void shouldPassValidationWhenStartTimeIsInTheFuture() {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.now().plusHours(2));

        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(booking));
    }
}