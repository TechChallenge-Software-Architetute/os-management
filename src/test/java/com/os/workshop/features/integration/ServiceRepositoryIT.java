package com.os.workshop.features.integration;

import com.os.workshop.domain.service.ServiceStatusEnum;
import com.os.workshop.domain.service.Status;
import com.os.workshop.infrastructure.persistence.service.ServiceEntity;
import com.os.workshop.infrastructure.persistence.service.ServiceJpaRepository;
import com.os.workshop.infrastructure.persistence.service.ServiceTypeEntity;
import com.os.workshop.infrastructure.persistence.service.ServiceTypeJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the ServiceStatusConverter (List<Status> to JSON) round-trip through PostgreSQL,
 * and the ServiceTypeEntity mapping.
 */
class ServiceRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private ServiceJpaRepository serviceRepository;

    @Autowired
    private ServiceTypeJpaRepository serviceTypeRepository;

    @Test
    void savesServiceWithJsonStatusConverter() {
        UUID osId = UUID.randomUUID();

        ServiceEntity service = new ServiceEntity();
        service.setServiceTypeName("TROCA_OLEO");
        service.setIdOS(osId);
        service.setServiceStatus(List.of(
                new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())
        ));

        ServiceEntity saved = serviceRepository.save(service);

        assertNotNull(saved.getId());

        var found = serviceRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("TROCA_OLEO", found.get().getServiceTypeName());
        assertEquals(1, found.get().getServiceStatus().size());
        assertEquals(ServiceStatusEnum.TO_DO, found.get().getServiceStatus().get(0).getStatus());
    }

    @Test
    void findsByIdOS() {
        UUID osId = UUID.randomUUID();

        ServiceEntity service = new ServiceEntity();
        service.setServiceTypeName("ALINHAMENTO");
        service.setIdOS(osId);
        service.setServiceStatus(List.of(new Status(ServiceStatusEnum.TO_DO, LocalDateTime.now())));

        serviceRepository.save(service);

        var results = serviceRepository.findByIdOS(osId);
        assertEquals(1, results.size());
    }

    @Test
    void savesAndFindsServiceType() {
        ServiceTypeEntity type = new ServiceTypeEntity();
        type.setName("REVISAO_INT_TEST");
        type.setDescription("Revisão completa");

        ServiceTypeEntity saved = serviceTypeRepository.save(type);

        assertNotNull(saved.getId());
        assertTrue(serviceTypeRepository.findByName("REVISAO_INT_TEST").isPresent());
    }
}
