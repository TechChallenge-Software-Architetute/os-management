package com.os.workshop.features.serviceorder.update;

import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import lombok.Data;

@Data
public class UpdateOrderRequest {

    private OrderServiceStatusEnum status;
}
