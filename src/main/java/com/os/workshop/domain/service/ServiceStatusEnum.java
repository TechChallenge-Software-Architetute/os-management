package com.os.workshop.domain.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ServiceStatusEnum {
    TO_DO("TO_DO"),
    DOING("DOING"),
    DONE("DONE");

    private final String status;
}
