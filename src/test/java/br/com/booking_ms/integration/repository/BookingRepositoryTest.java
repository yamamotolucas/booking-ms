package br.com.booking_ms.integration.repository;

import br.com.booking_ms.integration.factory.BarberServiceFactory;
import br.com.booking_ms.integration.factory.BookingFactory;
import br.com.booking_ms.integration.setup.MySQLSetup;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;
import br.com.booking_ms.repository.BarberServicesRepository;
import br.com.booking_ms.repository.BookingRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static br.com.booking_ms.integration.factory.BarberServiceFactory.createBarberService;
import static br.com.booking_ms.integration.factory.BookingFactory.createBooking;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class BookingRepositoryTest implements MySQLSetup {

    @Autowired
    private BarberServicesRepository barberServicesRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @AfterEach
    public void tearDown() {bookingRepository.deleteAll();}

    @Test
    @DisplayName("Should save a booking and generate its ID")
    void shouldSaveBooking() {
        Booking booking = createBooking();

        Booking saved = bookingRepository.saveAndFlush(booking);

        assertThat(saved.getBookingId()).isNotNull();
    }

    @Test
    @DisplayName("Should find a booking by its ID")
    void shouldFindBookingById() {
        Booking saved = bookingRepository.saveAndFlush(createBooking());

        var found = bookingRepository.findById(
                saved.getBookingId()
        );

        assertThat(found).isPresent();
        assertThat(found.get().getBookingId())
                .isEqualTo(saved.getBookingId());
    }

    @Test
    @DisplayName("Should return empty when booking does not exist")
    void shouldReturnEmptyForNonexistentBooking() {
        var found = bookingRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete a booking by its ID")
    void shouldDeleteBooking() {
        Booking saved = bookingRepository.saveAndFlush(createBooking());
        var id = saved.getBookingId();

        bookingRepository.deleteById(id);
        bookingRepository.flush();

        assertThat(bookingRepository.findById(id))
                .isEmpty();
    }

    @Test
    @DisplayName("Should update an existing booking")
    void shouldUpdateBooking() {
        Booking booking = createBooking();
        Booking saved = bookingRepository.saveAndFlush(booking);

        saved.setBookingStatus(BookingStatus.CONFIRMED);

        Booking updated = bookingRepository.saveAndFlush(saved);

        assertThat(updated.getBookingId()).isEqualTo(saved.getBookingId());
        assertThat(updated.getBookingStatus()).isEqualTo(saved.getBookingStatus());
    }

    @Test
    @DisplayName("Should persist booking with associated services")
    void shouldPersistBookingWithServices() {

        BarberServices savedService = barberServicesRepository.saveAndFlush(createBarberService());

        Booking booking = createBooking();
        booking.setServices(List.of(savedService));

        Booking saved = bookingRepository.saveAndFlush(booking);

        assertThat(saved.getBookingId()).isNotNull();
        assertThat(saved.getServices()).isNotEmpty();
    }
}
