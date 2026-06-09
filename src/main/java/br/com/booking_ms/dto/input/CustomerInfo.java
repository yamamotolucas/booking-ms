package br.com.booking_ms.dto.input;

public record CustomerInfo(
        Long id,
        String email,
        String name
) {
    public CustomerInfo(String customerId, String customerEmail, String customerName) {
        this(
                Long.parseLong(customerId),
                customerEmail,
                customerName
        );
    }
}
