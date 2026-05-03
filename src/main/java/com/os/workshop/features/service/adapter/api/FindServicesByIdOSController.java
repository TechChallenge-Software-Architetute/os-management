package com.os.workshop.features.service.adapter.api;

import com.os.workshop.features.common.api.ErrorResponse;
import com.os.workshop.features.service.domain.ServiceEntity;
import com.os.workshop.features.service.usecases.FindServicesByIdOSUC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
@Tag(name = "Services", description = "Manage workshop services associated with service orders.")
public class FindServicesByIdOSController {

    private static final Logger logger = LoggerFactory.getLogger(FindServicesByIdOSController.class);

    @Autowired
    private FindServicesByIdOSUC findServicesByIdOSUC;

    @GetMapping({"/service-order/{idOS}", "/os/{idOS}"})
    @Operation(summary = "List services by service order", description = "Lists all services linked to one service order. The /os/{idOS} path is kept as a backward-compatible alias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Services listed successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ServiceEntity.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid service order identifier", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Service order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ServiceEntity>> findByIdOS(
            @Parameter(description = "Service order unique identifier.", example = "8d5d7f7f-2d6a-4f8f-9f10-444f20f87601", required = true)
            @PathVariable UUID idOS) {
        logger.info("Recebida requisicao para buscar servicos por ID OS: {}", idOS);

        try {
            var services = findServicesByIdOSUC.process(idOS);

            return ResponseEntity.ok(services);
        } catch (Exception e) {
            logger.error("Erro ao buscar servicos por ID OS. ID OS: {}. Erro: {}", idOS, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
