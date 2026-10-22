package br.com.booking_ms.integration.setup;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.rabbitmq.RabbitMQContainer;


public interface RabbitMQSetup {

    RabbitMQContainer RABBITMQ_CONTAINER = new RabbitMQContainer("rabbitmq:4-management");

    @DynamicPropertySource
    static void configureRabbitMQ(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBITMQ_CONTAINER::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ_CONTAINER::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ_CONTAINER::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ_CONTAINER::getAdminPassword);
    }


    @BeforeAll
    static void postgresBeforeAll() {
        RABBITMQ_CONTAINER.start();
    }
}