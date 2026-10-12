package br.com.booking_ms.unit.service;

import br.com.booking_ms.amqp.producer.BookingProducer;
import br.com.booking_ms.client.BarberClient;
import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.input.CustomerInfo;
import br.com.booking_ms.dto.output.BookingBaseOutput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.repository.BarberServicesRepository;
import br.com.booking_ms.repository.BookingRepository;
import br.com.booking_ms.service.BookingService;
import br.com.booking_ms.validation.BookingCreationValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BarberServicesRepository barberServicesRepository;

    @Mock
    private BookingCreationValidator validator1;

    @Mock
    private BookingCreationValidator validator2;

    @Mock
    private BookingProducer bookingProducer;

    @Mock
    private BarberClient barberClient;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        List<BookingCreationValidator> validators = List.of(validator1, validator2);
        bookingService = new BookingService(
                bookingRepository,
                barberServicesRepository,
                validators,
                bookingProducer,
                barberClient
        );
    }

    private BarberServices createFakeService(Long id, int durationMinutes) {
        BarberServices service = new BarberServices();
        service.setId(id);
        service.setDurationMinutes(durationMinutes);
        return service;
    }

    @Test
    @DisplayName("Should return correct total duration in minutes when multiple services are provided")
    public void shouldCalculateDurationCorrectly() {
        // Arrange
        List<BarberServices> services = List.of(
                createFakeService(1L, 30),
                createFakeService(2L, 45)
        );

        // Act
        int totalDuration = bookingService.calculateDuration(services);

        // Assert
        assertEquals(75, totalDuration);
    }

    @Test
    @DisplayName("Should return services list when all requested service IDs exist")
    public void shouldReturnServicesWhenAllIdsExist() {
        // Arrange
        List<Long> requestedIds = List.of(1L, 2L);
        List<BarberServices> existingServices = List.of(
                createFakeService(1L, 30),
                createFakeService(2L, 30)
        );

        when(barberServicesRepository.findAllById(requestedIds)).thenReturn(existingServices);

        // Act
        List<BarberServices> result = bookingService.validateServices(requestedIds);

        // Assert
        assertEquals(2, result.size());
        verify(barberServicesRepository, times(1)).findAllById(requestedIds);
    }

    @Test
    @DisplayName("Should throw RuntimeException when requested services count does not match database result")
    public void shouldThrowExceptionWhenServiceCountMismatch() {
        // Arrange
        List<Long> requestedIds = List.of(1L, 2L, 3L);
        List<BarberServices> foundServices = List.of(createFakeService(1L, 30));

        when(barberServicesRepository.findAllById(requestedIds)).thenReturn(foundServices);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> bookingService.validateServices(requestedIds)
        );

        assertEquals("Number of services does not match", exception.getMessage());
        verify(barberServicesRepository, times(1)).findAllById(requestedIds);
    }

    @Test
    @DisplayName("Should create booking successfully, execute all validators, fetch barber name and send message")
    public void shouldCreateBookingSuccessfully() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 15, 10, 0);
        BookingCreateInput input = new BookingCreateInput(1L, List.of(10L, 20L), startTime);
        CustomerInfo customer = new CustomerInfo(99L, "carlos@email.com", "Carlos Cliente");

        List<BarberServices> services = List.of(
                createFakeService(10L, 30),
                createFakeService(20L, 30)
        );

        when(barberServicesRepository.findAllById(input.services())).thenReturn(services);
        when(barberClient.getBarberName("1")).thenReturn("João Barbeiro");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(500L);
            return b;
        });

        // Act
        BookingCreateOutput output = bookingService.create(input, customer);

        // Assert
        assertNotNull(output);

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());
        Booking captured = bookingCaptor.getValue();

        assertEquals(99L, captured.getCustomerId());
        assertEquals(1L, captured.getBarberId());
        assertEquals("Carlos Cliente", captured.getCustomerName());
        assertEquals("João Barbeiro", captured.getBarberName());
        assertEquals(startTime, captured.getStartTime());
        assertEquals(startTime.plusMinutes(60), captured.getEndTime()); // 30 + 30 min
        assertEquals(BookingStatus.PENDING, captured.getBookingStatus());
        assertNotNull(captured.getConfirmationToken());

        verify(validator1, times(1)).validate(captured);
        verify(validator2, times(1)).validate(captured);

        verify(bookingProducer, times(1)).sendPendingBooking(any(BookingCreateOutput.class));
    }

    @Test
    @DisplayName("Should not break creation flow if bookingProducer throws an exception")
    public void shouldNotFailWhenBrokerMessageFails() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2026, 9, 15, 10, 0);
        BookingCreateInput input = new BookingCreateInput(1L, List.of(10L), startTime);
        CustomerInfo customer = new CustomerInfo(99L, "carlos@email.com", "Carlos Cliente");
        List<BarberServices> services = List.of(createFakeService(10L, 30));

        when(barberServicesRepository.findAllById(input.services())).thenReturn(services);
        when(barberClient.getBarberName("1")).thenReturn("João Barbeiro");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        doThrow(new RuntimeException("RabbitMQ connection down")).when(bookingProducer).sendPendingBooking(any());

        // Act & Assert
        assertDoesNotThrow(() -> bookingService.create(input, customer));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when requested date is Sunday or Monday")
    public void shouldThrowValidationExceptionWhenRequestedDateIsWeekendOrMonday() {
        // Arrange
        LocalDate sunday = LocalDate.of(2026, 9, 13);
        AvailabilityInput input = new AvailabilityInput(1L, List.of(1L), sunday);

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.getAvaliableDays(input)
        );

        assertEquals("Bookings can only be made on business days.", exception.getMessage());
        verifyNoInteractions(barberServicesRepository, bookingRepository);
    }

    @Test
    @DisplayName("Should return available slots excluding occupied times on a valid business day")
    public void shouldReturnAvailableSlotsExcludingOccupiedTimes() {
        // Arrange
        LocalDate tuesday = LocalDate.of(2026, 9, 15);
        AvailabilityInput input = new AvailabilityInput(1L, List.of(1L), tuesday);

        List<BarberServices> services = List.of(createFakeService(1L, 30));
        when(barberServicesRepository.findAllById(input.services())).thenReturn(services);

        Booking occupiedBooking = new Booking();
        occupiedBooking.setStartTime(LocalDateTime.of(tuesday, LocalTime.of(8, 0)));
        occupiedBooking.setEndTime(LocalDateTime.of(tuesday, LocalTime.of(8, 30)));

        when(bookingRepository.findByBarberIdAndDate(1L, tuesday)).thenReturn(List.of(occupiedBooking));

        // Act
        List<String> availableSlots = bookingService.getAvaliableDays(input);

        // Assert
        assertNotNull(availableSlots);
        assertFalse(availableSlots.contains("15-09-2026 08:00:00"));
        assertTrue(availableSlots.contains("15-09-2026 08:30:00"));
    }

    @Test
    @DisplayName("Should cancel booking successfully and set status to CANCELLED")
    public void shouldCancelBookingSuccessfully() {
        // Arrange
        Booking booking = new Booking();
        booking.setBookingId(10L);
        booking.setCustomerId(99L);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByBookingIdAndCustomerId(10L, 99L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        BookingBaseOutput output = bookingService.cancelBooking("10", "99");

        // Assert
        assertNotNull(output);
        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when booking is not found for cancellation")
    public void shouldThrowExceptionWhenBookingNotFoundForCancellation() {
        // Arrange
        when(bookingRepository.findByBookingIdAndCustomerId(10L, 99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> bookingService.cancelBooking("10", "99"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when confirming already confirmed booking")
    public void shouldThrowExceptionWhenBookingIsAlreadyConfirmed() {
        // Arrange
        Booking booking = new Booking();
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findByConfirmationToken("token123")).thenReturn(Optional.of(booking));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.confirmBooking("token123")
        );

        assertEquals("Booking is already confirmed!", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when trying to confirm a booking whose start time is in the past")
    public void shouldThrowExceptionWhenBookingIsInThePast() {
        // Arrange
        Booking booking = new Booking();
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setStartTime(LocalDateTime.now().minusHours(2));
        when(bookingRepository.findByConfirmationToken("token123")).thenReturn(Optional.of(booking));

        // Act & Assert
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.confirmBooking("token123")
        );

        assertEquals("You cannot confirm this booking!", exception.getMessage());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should confirm booking successfully when status is PENDING and time is in the future")
    public void shouldConfirmBookingSuccessfully() {
        // Arrange
        Booking booking = new Booking();
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setStartTime(LocalDateTime.now().plusDays(1));
        when(bookingRepository.findByConfirmationToken("token123")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        BookingBaseOutput output = bookingService.confirmBooking("token123");

        // Assert
        assertNotNull(output);
        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Should complete booking successfully and update status to COMPLETED")
    public void shouldCompleteBookingSuccessfully() {
        // Arrange
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setBarberId(5L);
        booking.setBookingStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByBookingIdAndBarberId(1L, 5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        BookingBaseOutput output = bookingService.completeBooking("1", "5");

        // Assert
        assertNotNull(output);
        assertEquals(BookingStatus.COMPLETED, booking.getBookingStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when completeBooking does not find the booking")
    public void shouldThrowExceptionWhenCompleteBookingNotFound() {
        // Arrange
        when(bookingRepository.findByBookingIdAndBarberId(1L, 5L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> bookingService.completeBooking("1", "5"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should execute scheduled repository status update with valid dates")
    public void shouldCallBookingStatusSchedule() {
        // Act
        bookingService.bookingStatusSchedule();

        // Assert
        verify(bookingRepository, times(1)).bookingStatusSchedule(any(LocalDateTime.class), any(LocalDateTime.class));
    }
}