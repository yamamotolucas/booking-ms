package br.com.booking_ms.producer;

import br.com.booking_ms.dto.output.BookingCreateOutput;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingProducer {

    private final RabbitTemplate rabbitTemplate;

    public BookingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPendingBooking(BookingCreateOutput output) {
        rabbitTemplate.convertAndSend("pending.bookings", output);
    }
}
