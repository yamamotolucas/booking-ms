package br.com.booking_ms.service;

import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.input.CustomerInfo;
import br.com.booking_ms.dto.output.BookingBaseOutput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.producer.BookingProducer;
import br.com.booking_ms.repository.BarberServicesRepository;
import br.com.booking_ms.repository.BookingRepository;
import br.com.booking_ms.specifications.BookingSpecification;
import br.com.booking_ms.validation.BookingCreationValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BarberServicesRepository barberServicesRepository;
    private final List<BookingCreationValidator> validations;
    private final BookingProducer bookingProducer;

    public BookingService(BookingRepository bookingRepository, BarberServicesRepository barberServicesRepository, List<BookingCreationValidator> validations, BookingProducer bookingProducer) {
        this.bookingRepository = bookingRepository;
        this.barberServicesRepository = barberServicesRepository;
        this.validations = validations;
        this.bookingProducer = bookingProducer;
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
    public BookingCreateOutput create(BookingCreateInput input, CustomerInfo customer) {
        Booking booking = new Booking();

        List<BarberServices> services = validateServices(input.services());
        int durationMinutes = calculateDuration(services);

        LocalDateTime startTime = input.startTime();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);

        booking.setCustomerId(customer.id());
        booking.setBarberId(input.barberId());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setServices(services);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setConfirmationToken(UUID.randomUUID().toString());

        validations.forEach(validator -> validator.validate(booking));

        Booking savedBooking = bookingRepository.save(booking);
        BookingCreateOutput output = new BookingCreateOutput(savedBooking, customer);

        try {
            bookingProducer.sendPendingBooking(output);
        } catch (Exception e) {
            System.err.println("❌" + e.getMessage());
        }

        return output;

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

    public Page<BookingBaseOutput> getBookings(
            Long customerId,
            String scope,
            List<String> statuses,
            Long barberId, Integer lastDays,
            LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable
    ) {

        Specification<Booking> spec = Specification.where(BookingSpecification.hasCustomerId(customerId));
        LocalDateTime now = LocalDateTime.now();

        if ("UPCOMING".equalsIgnoreCase(scope)) {
            spec = spec.and(BookingSpecification.startTimeGreaterThanOrEqualTo(now));
        } else if ("PAST".equalsIgnoreCase(scope)) {
            spec = spec.and(BookingSpecification.startTimeLessThanOrEqualTo(now));
        }

        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and(BookingSpecification.hasStatusIn(statuses));
        }

        if (barberId != null) {
            spec = spec.and(BookingSpecification.hasBarberId(barberId));
        }

        if (lastDays != null) {
            LocalDateTime startOfPeriod = now.minusDays(lastDays);
            spec = spec.and(BookingSpecification.startTimeGreaterThanOrEqualTo(startOfPeriod));
        }

        if (startDate != null) {
            spec = spec.and(BookingSpecification.startTimeGreaterThanOrEqualTo(startDate));
        }
        if (endDate != null) {
            spec = spec.and(BookingSpecification.startTimeLessThanOrEqualTo(endDate));
        }

        return bookingRepository.findAll(spec, pageable).map(BookingBaseOutput::new);
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
    public BookingBaseOutput confirmBooking(String confirmationToken) {

        Booking booking = bookingRepository.findByConfirmationToken(confirmationToken)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        if(booking.getBookingStatus() == BookingStatus.CONFIRMED)
            throw new RuntimeException("Booking is already confirmed!");

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
