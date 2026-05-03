package com.os.workshop.features.serviceorder.update;

import com.os.workshop.features.serviceorder.shared.domain.enums.OrderServiceStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request payload used to update a service order status.")
public class UpdateOrderRequest {

    @Schema(description = "New service order status.", example = "IN_PROGRESS")
    @NotNull
    private OrderServiceStatusEnum status;
}
