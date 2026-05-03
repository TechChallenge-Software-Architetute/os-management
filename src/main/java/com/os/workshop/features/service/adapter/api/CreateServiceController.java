package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.domain.requests.CreateServiceRequest;
import com.os.workshop.features.service.usecases.CreateServiceUC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/services")
@Tag(name = "Services", description = "Manage workshop services associated with service orders.")
public class CreateServiceController {

    private static final Logger logger = LoggerFactory.getLogger(CreateServiceController.class);

    @Autowired
    private CreateServiceUC createServiceUC;

    @PostMapping
    @Operation(summary = "Create service", description = "Creates a service and links it to an existing service order.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Service created successfully", content = @Content(schema = @Schema(implementation = ServiceEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ServiceEntity> createService(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Service data to create.", required = true, content = @Content(schema = @Schema(implementation = CreateServiceRequest.class)))
            @Valid @RequestBody CreateServiceRequest request
    ) {
        logger.info("Creating service. idOS={}", request.getIdOS());
        try {

            var createdService = createServiceUC.process(request);
            logger.info("Service created. id={}", createdService.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdService);

        } catch (Exception e) {
            logger.error("Error creating service. idOS={}. Error: {}", request.getIdOS(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
