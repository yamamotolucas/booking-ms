package br.com.booking_ms.service;

import br.com.booking_ms.dto.input.BarberServicesInput;
import br.com.booking_ms.dto.output.BarberServicesOutput;
import br.com.booking_ms.model.BarberServices;
import br.com.booking_ms.repository.BarberServicesRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BarberServicesService {

    private final BarberServicesRepository repository;

    public BarberServicesService(BarberServicesRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BarberServicesOutput create(BarberServicesInput input) {
        BarberServices service = new BarberServices();
        BeanUtils.copyProperties(input, service);
        return new BarberServicesOutput(repository.save(service));
    }

    public List<BarberServicesOutput> findAllServices() {
        List<BarberServices> services = repository.findAll();
        return services
                .stream()
                .map(BarberServicesOutput::new)
                .toList();
    }
}
