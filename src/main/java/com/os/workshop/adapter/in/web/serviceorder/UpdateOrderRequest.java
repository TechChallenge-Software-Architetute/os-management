package com.os.workshop.adapter.in.web.serviceorder;

import com.os.workshop.domain.serviceorder.OrderServiceStatusEnum;
import lombok.Data;

@Data
public class UpdateOrderRequest {
    private OrderServiceStatusEnum status;
}
