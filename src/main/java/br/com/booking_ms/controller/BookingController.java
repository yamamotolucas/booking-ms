package br.com.booking_ms.controller;

import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.input.CustomerInfo;
import br.com.booking_ms.dto.output.BookingBaseOutput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import br.com.booking_ms.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Bookings", description = "Endpoints for creating, managing, confirming, and canceling appointments")
@RestController
@RequestMapping("/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Operation(summary = "Create a booking", description = "Creates a new appointment for the authenticated customer and returns the created booking details. **Required role:** `CUSTOMER`")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Booking created successfully"), @ApiResponse(responseCode = "400", description = "Invalid payload or time slot unavailable", content = @Content)})
    @PostMapping
    public ResponseEntity<BookingCreateOutput> create(
            @RequestBody @Valid BookingCreateInput input,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String customerId,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Email") String customerEmail,

            @Parameter(hidden = true)
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

    @Operation(summary = "Get available days", description = "Retrieves a list of available dates or slots based on provided criteria. **Required role:** `ADMIN`, `BARBER`, `CUSTOMER`")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Available slots retrieved successfully"), @ApiResponse(responseCode = "400", description = "Invalid input criteria", content = @Content)})
    @PostMapping("/availability")
    public ResponseEntity<List<String>> getAvailability(@RequestBody @Valid AvailabilityInput input) {
        return ResponseEntity.ok().body(bookingService.getAvaliableDays(input));
    }

    @Operation(summary = "Get user bookings", description = "Retrieves a paginated list of bookings associated with the authenticated user with optional filtering. **Required role:** `CUSTOMER`, `BARBER`")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"), @ApiResponse(responseCode = "400", description = "Invalid query parameters", content = @Content)})
    @GetMapping("/myBookings")
    public ResponseEntity<Page<BookingBaseOutput>> getMyBookings(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) Integer lastDays,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) boolean isBarber,
            @PageableDefault(page = 0, size = 10, sort = "startTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BookingBaseOutput> bookings = bookingService.getBookings(
                Long.parseLong(userId), scope, statuses, lastDays, startDate, endDate, isBarber, pageable
        );

        return ResponseEntity.ok().body(bookings);
    }

    @Operation(summary = "Cancel a booking", description = "Cancels an existing appointment for the authenticated customer. **Required role:** `CUSTOMER`")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Booking canceled successfully"), @ApiResponse(responseCode = "400", description = "Invalid cancellation request", content = @Content), @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)})
    @PatchMapping("/cancel/{bookingId}")
    public ResponseEntity<BookingBaseOutput> cancelBooking(
            @PathVariable String bookingId,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok().body(bookingService.cancelBooking(bookingId, customerId));
    }

    @Operation(summary = "Confirm a booking", description = "Confirms a pending booking using a confirmation token.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Booking confirmed successfully"), @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content), @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)})
    @SecurityRequirements
    @GetMapping("/confirm/{confirmationToken}")
    public ResponseEntity<BookingBaseOutput> confirmBooking(@PathVariable String confirmationToken) {
        return ResponseEntity.ok().body(bookingService.confirmBooking(confirmationToken));
    }

    @Operation(summary = "Complete a booking", description = "Marks an appointment as completed by the authenticated barber. **Required role:** `BARBER`")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Booking completed successfully"), @ApiResponse(responseCode = "400", description = "Invalid completion request", content = @Content), @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)})
    @PatchMapping("/complete/{bookingId}")
    public ResponseEntity<BookingBaseOutput> completeBooking(
            @PathVariable String bookingId,
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String barberId) {
        return ResponseEntity.ok().body(bookingService.completeBooking(bookingId, barberId));
    }

    @Operation(summary = "Test request headers", description = "Debug endpoint to verify forwarded headers from API Gateway. **Required role:** `ADMIN`")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Headers echoed successfully")})
    @GetMapping("/test-headers")
    public String testHeaders(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Email") String email,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Roles") String roles,

            @Parameter(hidden = true)
            @RequestHeader("X-User-Name") String name,

            @Parameter(hidden = true)
            @RequestHeader("Authorization") String authorization)
    {
        return "User: " + userId + "| Email:" + email + "| Roles: " + roles + "| Name: " + name + "| Authorization: " + authorization;
    }
}