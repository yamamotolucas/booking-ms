package br.com.booking_ms.controller;

import br.com.booking_ms.dto.input.BarberServicesInput;
import br.com.booking_ms.dto.output.BarberServicesOutput;
import br.com.booking_ms.service.BarberServicesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("v1/barber/services")
public class BarberServicesController {

    private final BarberServicesService service;

    public BarberServicesController(BarberServicesService service) {
        this.service = service;
    }

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

    @GetMapping("/list")
    public ResponseEntity<List<BarberServicesOutput>> findAllServices() {
        return ResponseEntity.ok().body(service.findAllServices());
    }
}
