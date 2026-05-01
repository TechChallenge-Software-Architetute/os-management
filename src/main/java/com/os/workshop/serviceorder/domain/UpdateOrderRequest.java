package com.os.workshop.serviceorder.domain;

import lombok.Data;

@Data
public class UpdateOrderRequest {

    private OrderServiceStatusEnum status;
}
