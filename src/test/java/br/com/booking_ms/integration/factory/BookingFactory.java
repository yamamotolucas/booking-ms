package br.com.booking_ms.integration.factory;

import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingFactory {

    public static Booking createBooking() {
        Booking booking = new Booking();

        booking.setCustomerId(1L);
        booking.setBarberId(2L);

        booking.setStartTime(LocalDateTime.now().plusDays(1));
        booking.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setConfirmationToken(UUID.randomUUID().toString());

        booking.setCustomerName("João Silva");
        booking.setBarberName("Carlos Santos");

        booking.setServices(List.of());

        return booking;
    }
}

