package br.com.booking_ms.amqp;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BookingAMQPConfig {

    @Bean
    public Queue createQueue() {return QueueBuilder.durable("pending.bookings").build();}

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {return new JacksonJsonMessageConverter();}

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory, JacksonJsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
