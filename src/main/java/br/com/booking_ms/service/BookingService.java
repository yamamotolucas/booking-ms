package br.com.booking_ms.service;

import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.repository.BarberServicesRepository;
import br.com.booking_ms.repository.BookingRepository;
import br.com.booking_ms.validation.BookingCreationValidator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BarberServicesRepository barberServicesRepository;
    private final List<BookingCreationValidator> validations;

    public BookingService(BookingRepository bookingRepository, BarberServicesRepository barberServicesRepository, List<BookingCreationValidator> validations) {
        this.bookingRepository = bookingRepository;
        this.barberServicesRepository = barberServicesRepository;
        this.validations = validations;
    }

    @Transactional
    public BookingCreateOutput create(BookingCreateInput input, String customerId) {

        LocalDateTime startTime = input.startTime();
        List<BarberServices> services = barberServicesRepository.findAllById(input.services());

        if(services.size() != input.services().size())
            throw new RuntimeException("Number of services does not match");

        int durationMinutes = 0;
        for (BarberServices service : services) {
            durationMinutes += service.getDurationMinutes();
        }

        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        Booking booking = new Booking();
        booking.setCustomerId(Long.parseLong(customerId));
        booking.setBarberId(input.barberId());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setServices(services);

        validations.forEach(validator -> validator.validate(booking));

        return new BookingCreateOutput(bookingRepository.save(booking));

    }

    public String getAvaliableDays() {
        return "TODO";
    }
}
