package br.com.booking_ms.validation;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.repository.BookingRepository;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class BookingScheduleConflictValidator implements BookingCreationValidator {

    private final BookingRepository bookingRepository;

    public BookingScheduleConflictValidator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validate(Booking booking) {

        LocalDateTime startTime = booking.getStartTime().minusMinutes(15);
        LocalDateTime endTime = booking.getEndTime();

        boolean conflict = bookingRepository.existsByDateAndStartTime(startTime, endTime);

        if (conflict)
            throw new ValidationException("This slot is unavailable.");

    }
}
