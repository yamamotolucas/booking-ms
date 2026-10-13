package br.com.booking_ms.integration.repository;

import br.com.booking_ms.integration.setup.MySQLSetup;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.repository.BarberServicesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static br.com.booking_ms.integration.factory.BarberServiceFactory.createBarberService;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class BarberServicesRepositoryTest implements MySQLSetup {

    @Autowired
    private BarberServicesRepository barberServicesRepository;

    @AfterEach
    public void tearDown() {barberServicesRepository.deleteAll();}

    @Test
    @DisplayName("Should save a barber service and generate its ID")
    void shouldSaveBarberService() {
        BarberServices service = createBarberService();

        BarberServices saved = barberServicesRepository.saveAndFlush(service);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should find a barber service by its ID")
    void shouldFindBarberServiceById() {
        BarberServices service = createBarberService();
        BarberServices saved = barberServicesRepository.saveAndFlush(service);

        var found = barberServicesRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(saved.getName());
        assertThat(found.get().getPrice()).isEqualByComparingTo(saved.getPrice());
        assertThat(found.get().getDurationMinutes())
                .isEqualTo(saved.getDurationMinutes());
    }

    @Test
    @DisplayName("Should return empty when the barber service does not exist")
    void shouldReturnEmptyWhenBarberServiceDoesNotExist() {
        var found = barberServicesRepository.findById(-1L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should update a barber service")
    void shouldUpdateBarberService() {
        BarberServices service = createBarberService();
        BarberServices saved = barberServicesRepository.saveAndFlush(service);

        saved.setName("Beard trim");

        BarberServices updated = barberServicesRepository.saveAndFlush(saved);

        assertThat(updated.getName()).isEqualTo("Beard trim");
    }

    @Test
    @DisplayName("Should delete a barber service")
    void shouldDeleteBarberService() {
        BarberServices service = createBarberService();
        BarberServices saved = barberServicesRepository.saveAndFlush(service);

        barberServicesRepository.delete(saved);
        barberServicesRepository.flush();

        assertThat(barberServicesRepository.findById(saved.getId())).isEmpty();
    }
}