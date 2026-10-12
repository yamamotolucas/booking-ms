package br.com.booking_ms.unit.service;

import br.com.booking_ms.dto.input.BarberServicesInput;
import br.com.booking_ms.dto.output.BarberServicesOutput;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.repository.BarberServicesRepository;
import br.com.booking_ms.service.BarberServicesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BarberServicesService Unit Tests")
public class BarberServicesServiceTest {

    @Mock
    private BarberServicesRepository repository;

    @InjectMocks
    private BarberServicesService service;

    @Test
    @DisplayName("Should copy properties from input, persist entity and return BarberServicesOutput successfully")
    public void shouldCreateBarberServiceSuccessfully() {
        // Arrange
        BarberServicesInput input = new BarberServicesInput("Corte Degradê", new BigDecimal("45.00"), 40);

        when(repository.save(any(BarberServices.class))).thenAnswer(invocation -> {
            BarberServices saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        BarberServicesOutput output = service.create(input);

        // Assert
        assertNotNull(output);

        ArgumentCaptor<BarberServices> serviceCaptor = ArgumentCaptor.forClass(BarberServices.class);
        verify(repository, times(1)).save(serviceCaptor.capture());
        BarberServices captured = serviceCaptor.getValue();

        assertEquals("Corte Degradê", captured.getName());
        assertEquals(new BigDecimal("45.00"), captured.getPrice());
        assertEquals(40, captured.getDurationMinutes());
    }

    @Test
    @DisplayName("Should return list of BarberServicesOutput when services exist")
    public void shouldReturnAllServicesSuccessfully() {
        // Arrange
        BarberServices service1 = new BarberServices();
        service1.setId(1L);
        service1.setName("Corte Social");
        service1.setPrice(new BigDecimal("35.00"));
        service1.setDurationMinutes(30);

        BarberServices service2 = new BarberServices();
        service2.setId(2L);
        service2.setName("Barba Completa");
        service2.setPrice(new BigDecimal("30.00"));
        service2.setDurationMinutes(25);

        when(repository.findAll()).thenReturn(List.of(service1, service2));

        // Act
        List<BarberServicesOutput> result = service.findAllServices();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no services are registered")
    public void shouldReturnEmptyListWhenNoServicesExist() {
        // Arrange
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<BarberServicesOutput> result = service.findAllServices();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, times(1)).findAll();
    }
}