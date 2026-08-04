package com.os.workshop.domain.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkshopService {

    private UUID id;
    private String serviceTypeName;
    private UUID idOS;
    private List<Status> serviceStatus;
}
