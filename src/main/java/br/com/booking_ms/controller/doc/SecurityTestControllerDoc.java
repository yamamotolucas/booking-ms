package br.com.booking_ms.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Security Test", description = "Test endpoints to validate role-based access control and public routing")
public interface SecurityTestControllerDoc {

    @Operation(summary = "Test Barber access", description = "Validates access for users with BARBER role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted to Barber"),
            @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content)
    })
    String testBarber();

    @Operation(summary = "Test Customer access", description = "Validates access for users with CUSTOMER role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted to Customer"),
            @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content)
    })
    String testCustomer();

    @Operation(summary = "Test Admin access", description = "Validates access for users with ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted to Admin"),
            @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content)
    })
    String testAdmin();

    @Operation(summary = "Test public access", description = "Validates access for unauthenticated or public requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted to everyone")
    })
    @SecurityRequirements
    String testPermitAll();
}