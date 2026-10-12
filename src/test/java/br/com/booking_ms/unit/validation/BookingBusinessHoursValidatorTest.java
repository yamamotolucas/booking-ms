package br.com.booking_ms.unit.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.validation.BookingBusinessHoursValidator;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingBusinessHoursValidator Unit Tests")
public class BookingBusinessHoursValidatorTest {

    private final BookingBusinessHoursValidator validator = new BookingBusinessHoursValidator();
    private final LocalDate sampleDate = LocalDate.of(2026, 9, 15);

    @ParameterizedTest(name = "Should pass when booking time is {0}:{1}")
    @CsvSource({
            "8, 0",    // Limite inferior exato (08:00)
            "8, 1",    // Logo após abertura (08:01)
            "12, 30",  // Meio do expediente (12:30)
            "16, 59",  // Logo antes do fechamento (16:59)
            "17, 0"    // Limite superior exato (17:00)
    })
    @DisplayName("Should pass validation when booking time is within or on the boundaries of business hours")
    public void shouldPassValidationWhenTimeIsWithinBusinessHours(int hour, int minute) {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(sampleDate, LocalTime.of(hour, minute)));

        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(booking));
    }

    @Test
    @DisplayName("Should throw ValidationException when booking time is before 08:00")
    public void shouldThrowValidationExceptionWhenTimeIsBeforeOpening() {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(sampleDate, LocalTime.of(7, 59)));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(booking)
        );

        assertEquals("Bookings must be made between 08:00 and 17:00.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw ValidationException when booking time is after 17:00")
    public void shouldThrowValidationExceptionWhenTimeIsAfterClosing() {
        // Arrange
        Booking booking = new Booking();
        booking.setStartTime(LocalDateTime.of(sampleDate, LocalTime.of(17, 1)));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(booking)
        );

        assertEquals("Bookings must be made between 08:00 and 17:00.", exception.getMessage());
    }
}