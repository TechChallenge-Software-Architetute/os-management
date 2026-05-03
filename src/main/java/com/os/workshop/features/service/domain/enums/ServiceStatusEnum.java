package com.os.workshop.features.service.domain.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE) // Generates private constructor for fields
public enum ServiceStatusEnum {
     TO_DO("TO_DO"),
     DOING("DOING"),
     DONE("DONE");

     private final String status;
}
