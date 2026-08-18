package br.com.booking_ms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Security Test", description = "Test endpoints to validate role-based access control and public routing")
@RestController
@RequestMapping("/test")
public class SecurityTestController {

    @Operation(summary = "Test Barber access", description = "Validates access for users with BARBER role.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Access granted to Barber"), @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content)})
    @GetMapping("/barber")
    public String testBarber() {
        System.out.println("Acessado: GET /test/barber");
        return "Acesso liberado: GET /test/barber (Role: BARBER)";
    }

    @Operation(summary = "Test Customer access", description = "Validates access for users with CUSTOMER role.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Access granted to Customer"), @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content)})
    @GetMapping("/customer")
    public String testCustomer() {
        System.out.println("Acessado: GET /test/customer");
        return "Acesso liberado: GET /test/customer (Role: CUSTOMER)";
    }

    @Operation(summary = "Test Admin access", description = "Validates access for users with ADMIN role.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Access granted to Admin"), @ApiResponse(responseCode = "403", description = "Access forbidden", content = @Content)})
    @GetMapping("/admin")
    public String testAdmin() {
        System.out.println("Acessado: GET /test/admin");
        return "Acesso liberado: GET /test/admin (Role: ADMIN)";
    }

    @Operation(summary = "Test public access", description = "Validates access for unauthenticated or public requests.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Access granted to everyone")})
    @SecurityRequirements
    @GetMapping("/permitAll")
    public String testPermitAll() {
        System.out.println("Acessado: GET /test/permitAll");
        return "Acesso liberado: GET /test/permitAll (Público)";
    }
}