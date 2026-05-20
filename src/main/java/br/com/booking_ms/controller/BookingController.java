package br.com.booking_ms.controller;

import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.output.BookingBaseOutput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.service.BookingService;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingCreateOutput> create(
            @RequestBody @Valid BookingCreateInput input,
            @RequestHeader("X-User-Id") String customerId
    ) {

        BookingCreateOutput booking = bookingService.create(input, customerId);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(booking)
                .toUri();

        return ResponseEntity.created(uri).body(booking);

    }

    @GetMapping("/availability")
    public ResponseEntity<List<String>> getAvailability(@RequestBody @Valid AvailabilityInput input) {
        return ResponseEntity.ok().body(bookingService.getAvaliableDays(input));
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingBaseOutput>> getMyBookings(@RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok().body(bookingService.getBookings(customerId));
    }

    @PatchMapping("/cancel/{bookingId}")
    public ResponseEntity<BookingBaseOutput> cancelBooking(@PathVariable String bookingId, @RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok().body(bookingService.cancelBooking(bookingId, customerId));
    }

    @GetMapping("/test-headers")
    public String testHeaders(@RequestHeader("X-User-Id") String userId,
                              @RequestHeader("X-User-Email") String email,
                              @RequestHeader("X-User-Roles") String roles) {
        return "User: " + userId + " Email:" + email + " Roles: " + roles;
    }
}
