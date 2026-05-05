package com.os.workshop.features.serviceorder.update;

import io.swagger.v3.oas.annotations.media.Schema;

import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import lombok.Data;

@Data
@Schema(description = "Update Order request payload.")
public class UpdateOrderRequest {

    @Schema(description = "Service order status.", example = "EM_DIAGNOSTICO")
    private OrderServiceStatusEnum status;
}
