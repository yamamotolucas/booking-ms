package br.com.booking_ms.integration.service;

import br.com.booking_ms.BookingMsApplication;
import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.input.CustomerInfo;
import br.com.booking_ms.integration.setup.MySQLSetup;
import br.com.booking_ms.integration.setup.RabbitMQSetup;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.repository.BarberServicesRepository;
import br.com.booking_ms.repository.BookingRepository;
import br.com.booking_ms.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import jakarta.validation.ValidationException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import static br.com.booking_ms.integration.factory.BookingFactory.createBooking;
import static java.time.temporal.TemporalAdjusters.next;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Booking Service Integration Tests")
@ContextConfiguration(classes = BookingMsApplication.class)
class BookingServiceTest implements MySQLSetup, RabbitMQSetup {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BarberServicesRepository barberServicesRepository;

    private static final Long BARBER_ID = 1L;
    private static final Long SERVICE_ID = 1L;

    private CustomerInfo createCustomer() {
        return new CustomerInfo(
                1L,
                "lucas@gmail.com",
                "lucas"
        );
    }

    private LocalDateTime getFutureBusinessDateTime() {
        LocalDate date = LocalDate.now().plusDays(14);

        while (date.getDayOfWeek() == DayOfWeek.SUNDAY
                || date.getDayOfWeek() == DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }

        return LocalDateTime.of(date, LocalTime.of(10, 0));
    }

    private Booking createTestBooking(BookingStatus status, LocalDateTime startTime) {
        Booking booking = createBooking();
        booking.setBarberId(BARBER_ID);
        booking.setCustomerId(1L);
        booking.setBookingStatus(status);
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusHours(1));
        return bookingRepository.save(booking);
    }

    @BeforeEach
    void setUp() {
        BarberServices service = new BarberServices();
        service.setName("Corte de Cabelo");
        service.setDurationMinutes(30);
        service.setPrice(BigDecimal.valueOf(67));
        barberServicesRepository.save(service);
    }

    @Test
    @DisplayName("Should create a booking successfully and persist in database")
    void shouldCreateBooking() {
        BookingCreateInput input = new BookingCreateInput(
                BARBER_ID,
                List.of(SERVICE_ID),
                getFutureBusinessDateTime()
        );

        var output = bookingService.create(input, createCustomer());

        assertThat(output).isNotNull();

        Booking savedBooking = bookingRepository.findById(output.bookingId())
                .orElseThrow(() -> new AssertionError("Booking was not persisted in the database"));

        assertThat(savedBooking.getCustomerId()).isEqualTo(createCustomer().id());
        assertThat(savedBooking.getBarberId()).isEqualTo(BARBER_ID);
        assertThat(savedBooking.getBookingStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    @DisplayName("Should publish a message to RabbitMQ when a booking is created")
    void shouldPublishMessageWhenBookingIsCreated() {
        BookingCreateInput input = new BookingCreateInput(
                BARBER_ID,
                List.of(SERVICE_ID),
                getFutureBusinessDateTime()
        );

        bookingService.create(input, createCustomer());

        Object message = rabbitTemplate.receiveAndConvert(
                "pending.bookings",
                5000
        );

        assertThat(message).isNotNull();
    }

    @Test
    @DisplayName("Should reject a booking with a non-existent service")
    void shouldRejectBookingWithNonExistentService() {
        BookingCreateInput input = new BookingCreateInput(
                BARBER_ID,
                List.of(Long.MAX_VALUE),
                getFutureBusinessDateTime()
        );

        assertThatThrownBy(() ->
                bookingService.create(input, createCustomer())
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Number of services does not match");
    }

    @Test
    @DisplayName("Should reject availability requests for Sundays")
    void shouldRejectAvailabilityOnSunday() {
        LocalDate sunday = LocalDate.now()
                .with(next(DayOfWeek.SUNDAY));

        AvailabilityInput input = new AvailabilityInput(
                BARBER_ID,
                List.of(SERVICE_ID),
                sunday
        );

        assertThatThrownBy(() ->
                bookingService.getAvaliableDays(input)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("Bookings can only be made on business days.");
    }

    @Test
    @DisplayName("Should reject availability requests for Mondays")
    void shouldRejectAvailabilityOnMonday() {
        LocalDate monday = LocalDate.now()
                .with(next(DayOfWeek.MONDAY));

        AvailabilityInput input = new AvailabilityInput(
                BARBER_ID,
                List.of(SERVICE_ID),
                monday
        );

        assertThatThrownBy(() ->
                bookingService.getAvaliableDays(input)
        ).isInstanceOf(ValidationException.class)
                .hasMessage("Bookings can only be made on business days.");
    }

    @Test
    @DisplayName("Should cancel a booking successfully in the database")
    void shouldCancelBooking() {
        Booking booking = createTestBooking(
                BookingStatus.CONFIRMED,
                getFutureBusinessDateTime()
        );

        bookingService.cancelBooking(
                booking.getBookingId().toString(),
                "1"
        );

        Booking updatedBooking = bookingRepository
                .findByBookingIdAndCustomerId(
                        booking.getBookingId(),
                        1L
                )
                .orElseThrow();

        assertThat(updatedBooking.getBookingStatus())
                .isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw an exception when cancelling a non-existent booking")
    void shouldThrowExceptionWhenCancellingNonExistentBooking() {

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.cancelBooking(
                        Long.MAX_VALUE + "",
                        "1"
                )
        );

        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should confirm a pending booking successfully in the database")
    void shouldConfirmPendingBooking() {
        Booking booking = createTestBooking(
                BookingStatus.PENDING,
                getFutureBusinessDateTime()
        );

        bookingService.confirmBooking(
                booking.getConfirmationToken()
        );

        Booking updatedBooking = bookingRepository
                .findByConfirmationToken(
                        booking.getConfirmationToken()
                )
                .orElseThrow();

        assertThat(updatedBooking.getBookingStatus())
                .isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should reject confirmation when the booking is already confirmed")
    void shouldRejectAlreadyConfirmedBooking() {
        Booking booking = createTestBooking(
                BookingStatus.CONFIRMED,
                getFutureBusinessDateTime()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.confirmBooking(booking.getConfirmationToken())
        );

        assertThat(exception.getMessage()).isEqualTo("Booking is already confirmed!");

    }

    @Test
    @DisplayName("Should throw an exception when the confirmation token does not exist")
    void shouldRejectInvalidConfirmationToken() {
        assertThatThrownBy(() ->
                bookingService.confirmBooking(UUID.randomUUID().toString())
        ).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Booking not found");
    }

    @Test
    @DisplayName("Should complete a booking successfully in the database")
    void shouldCompleteBooking() {
        Booking booking = createTestBooking(
                BookingStatus.CONFIRMED,
                getFutureBusinessDateTime()
        );

        bookingService.completeBooking(
                booking.getBookingId().toString(),
                BARBER_ID.toString()
        );

        Booking updatedBooking = bookingRepository
                .findByBookingIdAndBarberId(
                        booking.getBookingId(),
                        BARBER_ID
                )
                .orElseThrow();

        assertThat(updatedBooking.getBookingStatus())
                .isEqualTo(BookingStatus.COMPLETED);
    }
}