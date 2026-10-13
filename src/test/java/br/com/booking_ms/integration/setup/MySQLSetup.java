package br.com.booking_ms.integration.setup;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

public interface MySQLSetup {
    MySQLContainer MYSQL_CONTAINER = new MySQLContainer("mysql:latest");

    @DynamicPropertySource
    static void postgresDynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driverClassName", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);

        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:/db/migration");

        registry.add("spring.jpa.generate-ddl", () -> "false");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @BeforeAll
    static void beforeAll() {
        MYSQL_CONTAINER.start();
    }

}

