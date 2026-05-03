package com.os.workshop.features.serviceorder.domain;

import lombok.Data;

@Data
public class UpdateOrderRequest {

    private OrderServiceStatusEnum status;
}
