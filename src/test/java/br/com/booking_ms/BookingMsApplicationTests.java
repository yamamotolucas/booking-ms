package br.com.booking_ms;

import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class BookingMsApplicationTests {

	@MockitoBean
	private ConnectionFactory connectionFactory;

	@MockitoBean
	private RabbitTemplate rabbitTemplate;

	@MockitoBean
	private RabbitAdmin rabbitAdmin;

	@Test
	void contextLoads() {
	}

}
