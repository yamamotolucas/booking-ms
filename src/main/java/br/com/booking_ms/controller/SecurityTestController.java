package br.com.booking_ms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class SecurityTestController {

    @GetMapping("/barber")
    public String testBarber() {
        System.out.println("Acessado: GET /test/barber");
        return "Acesso liberado: GET /test/barber (Role: BARBER)";
    }

    @GetMapping("/customer")
    public String testCustomer() {
        System.out.println("Acessado: GET /test/customer");
        return "Acesso liberado: GET /test/customer (Role: CUSTOMER)";
    }

    @GetMapping("/admin")
    public String testAdmin() {
        System.out.println("Acessado: GET /test/admin");
        return "Acesso liberado: GET /test/admin (Role: ADMIN)";
    }

    @GetMapping("/permitAll")
    public String testPermitAll() {
        System.out.println("Acessado: GET /test/permitAll");
        return "Acesso liberado: GET /test/permitAll (Público)";
    }
}