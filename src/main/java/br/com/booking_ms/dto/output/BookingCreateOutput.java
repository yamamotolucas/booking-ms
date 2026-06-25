package br.com.booking_ms.dto.output;


import br.com.booking_ms.dto.ServicesDto;
import br.com.booking_ms.dto.input.CustomerInfo;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record BookingCreateOutput(

        Long bookingId,
        String customerName,
        String customerEmail,
        String barberName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<ServicesDto> services,
        BookingStatus bookingStatus,
        String confirmationToken

) {
    public BookingCreateOutput(Booking booking, CustomerInfo customer) {
        this (
                booking.getBookingId(),
                customer.name(),
                customer.email(),
                booking.getBarberName(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getServices()
                        .stream()
                        .map(m -> new ServicesDto(
                                m.getName(),
                                m.getPrice()
                        ))
                        .collect(Collectors.toList()),
                booking.getBookingStatus(),
                booking.getConfirmationToken()
        );
    }
}