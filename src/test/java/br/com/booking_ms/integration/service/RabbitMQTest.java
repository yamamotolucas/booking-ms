package br.com.booking_ms.integration.service;

import br.com.booking_ms.BookingMsApplication;
import br.com.booking_ms.integration.setup.RabbitMQSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = BookingMsApplication.class)
@Testcontainers
class RabbitMQTest implements RabbitMQSetup {

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final String QUEUE_NAME = "pending.bookings";

    @Test
    @DisplayName("Should create the pending bookings queue")
    void shouldCreateQueue() {
        Properties queueProperties =
                rabbitAdmin.getQueueProperties(QUEUE_NAME);

        assertThat(queueProperties).isNotNull();
    }

    @Test
    @DisplayName("Should publish a message and receive it from the queue")
    void shouldPublishMessageAndReceiveItFromQueue() {
        String message = "Mensagem de teste";

        rabbitTemplate.convertAndSend(
                QUEUE_NAME,
                message
        );

        Object receivedMessage =
                rabbitTemplate.receiveAndConvert(QUEUE_NAME, 5000);

        assertThat(receivedMessage).isEqualTo(message);
    }
}