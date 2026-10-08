package br.com.booking_ms.controller.doc;

import br.com.booking_ms.dto.input.AvailabilityInput;
import br.com.booking_ms.dto.input.BookingCreateInput;
import br.com.booking_ms.dto.output.BookingBaseOutput;
import br.com.booking_ms.dto.output.BookingCreateOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Bookings", description = "Endpoints for creating, managing, confirming, and canceling appointments")
public interface BookingControllerDoc {

    @Operation(summary = "Create a booking", description = "Creates a new appointment for the authenticated customer and returns the created booking details. **Required role:** `CUSTOMER`")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or time slot unavailable", content = @Content)
    })
    ResponseEntity<BookingCreateOutput> create(
            BookingCreateInput input,
            @Parameter(hidden = true) String customerId,
            @Parameter(hidden = true) String customerEmail,
            @Parameter(hidden = true) String customerName
    );

    @Operation(summary = "Get available days", description = "Retrieves a list of available dates or slots based on provided criteria. **Required role:** `ADMIN`, `BARBER`, `CUSTOMER`")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Available slots retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input criteria", content = @Content)
    })
    ResponseEntity<List<String>> getAvailability(AvailabilityInput input);

    @Operation(summary = "Get user bookings", description = "Retrieves a paginated list of bookings associated with the authenticated user with optional filtering. **Required role:** `CUSTOMER`, `BARBER`")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameters", content = @Content)
    })
    ResponseEntity<Page<BookingBaseOutput>> getMyBookings(
            @Parameter(hidden = true) String userId,
            String scope,
            List<String> statuses,
            Integer lastDays,
            LocalDateTime startDate,
            LocalDateTime endDate,
            boolean isBarber,
            Pageable pageable
    );

    @Operation(summary = "Cancel a booking", description = "Cancels an existing appointment for the authenticated customer. **Required role:** `CUSTOMER`")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking canceled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid cancellation request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)
    })
    ResponseEntity<BookingBaseOutput> cancelBooking(
            String bookingId,
            @Parameter(hidden = true) String customerId
    );

    @Operation(summary = "Confirm a booking", description = "Confirms a pending booking using a confirmation token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)
    })
    @SecurityRequirements
    ResponseEntity<BookingBaseOutput> confirmBooking(String confirmationToken);

    @Operation(summary = "Complete a booking", description = "Marks an appointment as completed by the authenticated barber. **Required role:** `BARBER`")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid completion request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content)
    })
    ResponseEntity<BookingBaseOutput> completeBooking(
            String bookingId,
            @Parameter(hidden = true) String barberId
    );

    @Operation(summary = "Test request headers", description = "Debug endpoint to verify forwarded headers from API Gateway. **Required role:** `ADMIN`")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Headers echoed successfully")
    })
    String testHeaders(
            @Parameter(hidden = true) String userId,
            @Parameter(hidden = true) String email,
            @Parameter(hidden = true) String roles,
            @Parameter(hidden = true) String name,
            @Parameter(hidden = true) String authorization
    );
}