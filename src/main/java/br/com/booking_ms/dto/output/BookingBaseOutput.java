package br.com.booking_ms.dto.output;

import br.com.booking_ms.dto.ServicesDto;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record BookingBaseOutput(

        Long barberId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<ServicesDto> services,
        BookingStatus bookingStatus

) {
    public BookingBaseOutput(Booking booking) {
        this (

                booking.getBarberId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getServices()
                        .stream()
                        .map(m -> new ServicesDto(
                                m.getName(),
                                m.getPrice()
                        ))
                        .collect(Collectors.toList()),
                booking.getBookingStatus()
        );
    }
}
