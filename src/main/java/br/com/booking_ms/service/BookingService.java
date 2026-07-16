package br.com.booking_ms.service;

import br.com.booking_ms.client.BarberClient;
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
import feign.FeignException;
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

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BarberServicesRepository barberServicesRepository;
    private final List<BookingCreationValidator> validations;
    private final BookingProducer bookingProducer;
    private final BarberClient barberClient;

    public BookingService
    (
            BookingRepository bookingRepository,
            BarberServicesRepository barberServicesRepository,
            List<BookingCreationValidator> validations,
            BookingProducer bookingProducer,
            BarberClient barberClient
    ) {
        this.bookingRepository = bookingRepository;
        this.barberServicesRepository = barberServicesRepository;
        this.validations = validations;
        this.bookingProducer = bookingProducer;
        this.barberClient = barberClient;
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
        booking.setCustomerName(customer.name());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setServices(services);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setConfirmationToken(UUID.randomUUID().toString());

        validations.forEach(validator -> validator.validate(booking));

        booking.setBarberName(barberClient.getBarberName(input.barberId().toString()));

        Booking savedBooking = bookingRepository.save(booking);
        BookingCreateOutput output = new BookingCreateOutput(savedBooking, customer);
        sendMessage(output);

        return output;

    }

    public void sendMessage(BookingCreateOutput output) {

        try {
            bookingProducer.sendPendingBooking(output);
        } catch (Exception e) {
            System.err.println("❌" + e.getMessage());
        }

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
            Long userId,
            String scope,
            List<String> statuses,
            Integer lastDays,
            LocalDateTime startDate, LocalDateTime endDate,
            boolean isBarber,
            Pageable pageable
    ) {
        LocalDateTime now = LocalDateTime.now();
        Specification<Booking> spec = Specification.unrestricted();

        if (!isBarber)
            spec = spec.and(BookingSpecification.hasCustomerId(userId));
        else
            spec = spec.and(BookingSpecification.hasBarberId(userId));

        if ("UPCOMING".equalsIgnoreCase(scope)) {
            spec = spec.and(BookingSpecification.startTimeGreaterThanOrEqualTo(now));
        } else if ("PAST".equalsIgnoreCase(scope)) {
            spec = spec.and(BookingSpecification.isPastOrInactive(now));
        }

        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and(BookingSpecification.hasStatusIn(statuses));
        }

        if (lastDays != null) {
            LocalDateTime startOfPeriod = now.minusDays(lastDays);
            spec = spec.and(BookingSpecification.lastDaysFilter(startOfPeriod));
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
            throw new ValidationException("Booking is already confirmed!");

        if(booking.getStartTime().isBefore(LocalDateTime.now()) || booking.getBookingStatus() != BookingStatus.PENDING)
            throw new ValidationException("You cannot confirm this booking!");

        booking.setBookingStatus(BookingStatus.CONFIRMED);
        return new BookingBaseOutput(bookingRepository.save(booking));
    }

    @Transactional
    public BookingBaseOutput completeBooking(String bookingId, String barberId) {

        Long idBooking = Long.parseLong(bookingId);
        Long idBarber = Long.parseLong(barberId);

        Booking booking = bookingRepository.findByBookingIdAndBarberId(idBooking, idBarber)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));

        booking.setBookingStatus(BookingStatus.COMPLETED);
        return new BookingBaseOutput(bookingRepository.save(booking));
    }


}
