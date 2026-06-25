package br.com.booking_ms.client;
import org.springframework.stereotype.Component;

@Component
public class BarberClientFallback implements BarberClient {

    @Override
    public String getBarberName(String id) {
        System.err.println("[Fallback] Barber API failed or timed out. Returning empty string.");
        return "";
    }
}