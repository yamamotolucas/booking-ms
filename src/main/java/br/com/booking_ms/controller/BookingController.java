package br.com.booking_ms.controller;

import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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


    @GetMapping("/test-headers")
    public String testHeaders(@RequestHeader("X-User-Id") String userId,
                              @RequestHeader("X-User-Email") String email,
                              @RequestHeader("X-User-Roles") String roles) {
        return "User: " + userId + " Email:" + email + " Roles: " + roles;
    }
}
