package br.com.booking_ms.jobs;

import br.com.booking_ms.service.BookingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BookingScheduler {

    private final BookingService bookingService;

    public BookingScheduler(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "America/Sao_Paulo")
    public void bookingScheduler() {
        bookingService.bookingStatusSchedule();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        bookingService.bookingStatusSchedule();
    }
}

