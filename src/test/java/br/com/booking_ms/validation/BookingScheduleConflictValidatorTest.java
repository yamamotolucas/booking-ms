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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingScheduleConflictValidator Unit Tests")
public class BookingScheduleConflictValidatorTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingScheduleConflictValidator validator;

    @Test
    @DisplayName("Should throw ValidationException when a schedule conflict exists")
    public void shouldThrowValidationExceptionWhenConflictExists() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 15, 14, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 15, 14, 45);
        LocalDateTime expectedLookupStartTime = startTime.minusMinutes(15);

        Booking booking = new Booking();
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);

        when(bookingRepository.existsByDateAndStartTime(expectedLookupStartTime, endTime))
                .thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validate(booking)
        );

        assertEquals("This slot is unavailable.", exception.getMessage());
        verify(bookingRepository, times(1)).existsByDateAndStartTime(expectedLookupStartTime, endTime);
    }

    @Test
    @DisplayName("Should pass validation without exception when no conflict is found")
    public void shouldPassValidationWhenNoConflictExists() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 15, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 15, 10, 30);
        LocalDateTime expectedLookupStartTime = startTime.minusMinutes(15);

        Booking booking = new Booking();
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);

        when(bookingRepository.existsByDateAndStartTime(expectedLookupStartTime, endTime))
                .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(booking));
        verify(bookingRepository, times(1)).existsByDateAndStartTime(expectedLookupStartTime, endTime);
    }
}