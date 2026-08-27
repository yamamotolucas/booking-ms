package br.com.booking_ms.controller;

import br.com.booking_ms.dto.input.BarberServicesInput;
import br.com.booking_ms.dto.output.BarberServicesOutput;
import br.com.booking_ms.service.BarberServicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Barber Services", description = "Endpoints for managing and listing services offered by barbers")
@RestController
@RequestMapping("v1/barber/services")
public class BarberServicesController {

    private final BarberServicesService service;

    public BarberServicesController(BarberServicesService service) {
        this.service = service;
    }

    @Operation(summary = "Create a barber service", description = "Creates a new service offering for a barber and returns the created service details. **Required role:** `ADMIN`")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Barber service created successfully"), @ApiResponse(responseCode = "400", description = "Invalid payload or service already exists", content = @Content)})
    @PostMapping("/create")
    public ResponseEntity<BarberServicesOutput> create(@RequestBody @Valid BarberServicesInput input) {

        BarberServicesOutput output = service.create(input);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(output.id())
                .toUri();

        return ResponseEntity.created(uri).body(output);
    }

    @Operation(summary = "List all barber services", description = "Retrieves a complete list of all available barber services.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "List of barber services retrieved successfully")})
    @SecurityRequirements
    @GetMapping("/list")
    public ResponseEntity<List<BarberServicesOutput>> findAllServices() {
        return ResponseEntity.ok().body(service.findAllServices());
    }
}