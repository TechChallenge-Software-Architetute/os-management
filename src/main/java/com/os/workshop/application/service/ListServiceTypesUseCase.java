package com.os.workshop.application.service;

import com.os.workshop.application.service.port.out.ServiceTypeRepository;
import com.os.workshop.domain.service.ServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListServiceTypesUseCase {

    private final ServiceTypeRepository serviceTypeRepository;

    public List<ServiceType> execute() {
        return serviceTypeRepository.findAll();
    }
}
