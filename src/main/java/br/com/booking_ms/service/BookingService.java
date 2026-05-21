package br.com.booking_ms.service;

import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.output.BookingBaseOutput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.repository.BarberServicesRepository;
import br.com.booking_ms.repository.BookingRepository;
import br.com.booking_ms.validation.BookingCreationValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public int calculateDuration(List<BarberServices> services){

        int durationMinutes = 0;
        for (BarberServices service : services) {
            durationMinutes += service.getDurationMinutes();
        }

        return durationMinutes;
    }

    public List<BarberServices> validateServices(List<Long> requestedServices) {

        List<BarberServices> services = barberServicesRepository.findAllById(requestedServices);
        if(services.size() != requestedServices.size())
            throw new RuntimeException("Number of services does not match");

        return services;

    }

    @Transactional
    public BookingCreateOutput create(BookingCreateInput input, String customerId) {

        Booking booking = new Booking();

        List<BarberServices> services = validateServices(input.services());
        int durationMinutes = calculateDuration(services);

        LocalDateTime startTime = input.startTime();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        booking.setCustomerId(Long.parseLong(customerId));
        booking.setBarberId(input.barberId());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setServices(services);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        validations.forEach(validator -> validator.validate(booking));
        return new BookingCreateOutput(bookingRepository.save(booking));

    }

    public List<String> getAvaliableDays(AvailabilityInput input) {

        List<String> avaliable = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        LocalDate localDate = input.requestedDate();
        if(localDate.getDayOfWeek() == DayOfWeek.SUNDAY || localDate.getDayOfWeek() == DayOfWeek.MONDAY)
            throw new ValidationException("Bookings can only be made on business days.");


        List<BarberServices> services = validateServices(input.services());
        int durationMinutes = calculateDuration(services);

        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);

        List<Booking> existingBookings = bookingRepository.findByBarberIdAndDate(input.barberId(), localDate);

        for (LocalTime time = startTime; !time.isAfter(endTime); time = time.plusMinutes(30)) {

            LocalDateTime initialTime = LocalDateTime.of(localDate, time);
            LocalDateTime finalTime = initialTime.plusMinutes(durationMinutes);
            boolean occupied = existingBookings
                    .stream()
                    .anyMatch(b -> b.getStartTime().isBefore(finalTime) && b.getEndTime().isAfter(initialTime));

            if(!occupied)
                avaliable.add(formatter.format(initialTime));

        }

        return avaliable;
    }

    public List<BookingBaseOutput> getBookings(String customerId) {

        List<Booking> bookings = bookingRepository.findByCustomerId(Long.parseLong(customerId));
        if(bookings.isEmpty())
            throw new RuntimeException("No bookings found");

        return bookings.stream().map(BookingBaseOutput::new).collect(Collectors.toList());
    }

    @Transactional
    public BookingBaseOutput cancelBooking(String bookingId, String customerId) {

        Long idBooking = Long.parseLong(bookingId);
        Long idCustomer = Long.parseLong(customerId);

        Booking booking = bookingRepository.findByBookingIdAndCustomerId(idBooking, idCustomer)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        booking.setBookingStatus(BookingStatus.CANCELLED);
        return new BookingBaseOutput(bookingRepository.save(booking));
    }

    @Transactional
    public BookingBaseOutput confirmBooking(String bookingId, String customerId) {

        Long idBooking = Long.parseLong(bookingId);
        Long idCustomer = Long.parseLong(customerId);

        Booking booking = bookingRepository.findByBookingIdAndCustomerId(idBooking, idCustomer)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        return new BookingBaseOutput(bookingRepository.save(booking));
    }

    @Transactional
    public BookingBaseOutput completeBooking(String bookingId, String customerId) {

        Long idBooking = Long.parseLong(bookingId);
        Long idCustomer = Long.parseLong(customerId);

        Booking booking = bookingRepository.findByBookingIdAndCustomerId(idBooking, idCustomer)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        booking.setBookingStatus(BookingStatus.COMPLETED);
        return new BookingBaseOutput(bookingRepository.save(booking));
    }


}
