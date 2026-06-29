package br.com.booking_ms.controller;

import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.input.CustomerInfo;
import br.com.booking_ms.dto.output.BookingBaseOutput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.model.Booking;
import br.com.booking_ms.service.BookingService;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
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
            @RequestHeader("X-User-Id") String customerId,
            @RequestHeader("X-User-Email") String customerEmail,
            @RequestHeader("X-User-Name") String customerName
    ) {

        CustomerInfo customerInfo = new CustomerInfo(customerId, customerEmail, customerName);
        BookingCreateOutput booking = bookingService.create(input, customerInfo);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(booking.bookingId())
                .toUri();

        return ResponseEntity.created(uri).body(booking);

    }

    @PostMapping("/availability")
    public ResponseEntity<List<String>> getAvailability(@RequestBody @Valid AvailabilityInput input) {
        return ResponseEntity.ok().body(bookingService.getAvaliableDays(input));
    }

    @GetMapping("/myBookings")
    public ResponseEntity<Page<BookingBaseOutput>> getMyBookings(
            @RequestHeader("X-User-Id") String customerId,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) Long barberId,
            @RequestParam(required = false) Integer lastDays,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(page = 0, size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingBaseOutput> bookings = bookingService.getBookings(
                Long.parseLong(customerId), scope, statuses, barberId, lastDays, startDate, endDate, pageable
        );

        return ResponseEntity.ok().body(bookings);
    }

    @PatchMapping("/cancel/{bookingId}")
    public ResponseEntity<BookingBaseOutput> cancelBooking(@PathVariable String bookingId, @RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok().body(bookingService.cancelBooking(bookingId, customerId));
    }

    @GetMapping("/confirm/{confirmationToken}")
    public ResponseEntity<BookingBaseOutput> confirmBooking(@PathVariable String confirmationToken) {
        return ResponseEntity.ok().body(bookingService.confirmBooking(confirmationToken));
    }

    @PatchMapping("/complete/{bookingId}")
    public ResponseEntity<BookingBaseOutput> completeBooking(@PathVariable String bookingId, @RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok().body(bookingService.completeBooking(bookingId, customerId));
    }


    @GetMapping("/test-headers")
    public String testHeaders(@RequestHeader("X-User-Id") String userId,
                              @RequestHeader("X-User-Email") String email,
                              @RequestHeader("X-User-Roles") String roles,
                              @RequestHeader("X-User-Name") String name,
                              @RequestHeader("Authorization") String authorization) {
        return "User: " + userId + "| Email:" + email + "| Roles: " + roles + "| Name: " + name + "| Authorization: " + authorization;
    }
}
