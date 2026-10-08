package br.com.booking_ms.controller.doc;

import br.com.booking_ms.dto.input.BarberServicesInput;
import br.com.booking_ms.dto.output.BarberServicesOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Barber Services", description = "Endpoints for managing and listing services offered by barbers")
public interface BarberServicesControllerDoc {

    @Operation(summary = "Create a barber service", description = "Creates a new service offering for a barber and returns the created service details. **Required role:** `ADMIN`")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Barber service created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or service already exists", content = @Content)
    })
    ResponseEntity<BarberServicesOutput> create(BarberServicesInput input);

    @Operation(summary = "List all barber services", description = "Retrieves a complete list of all available barber services.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of barber services retrieved successfully")
    })
    @SecurityRequirements
    ResponseEntity<List<BarberServicesOutput>> findAllServices();
}