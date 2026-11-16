package com.os.workshop.adapter.in.web.product.supply;

import com.os.workshop.application.product.supply.CreateSupplyUseCase;
import com.os.workshop.application.product.supply.DeactivateSupplyUseCase;
import com.os.workshop.application.product.supply.FindSupplyByIdUseCase;
import com.os.workshop.application.product.supply.FindSupplyBySkuUseCase;
import com.os.workshop.application.product.supply.ListSuppliesUseCase;
import com.os.workshop.application.product.supply.UpdateSupplyUseCase;
import com.os.workshop.domain.product.ProductType;
import com.os.workshop.domain.product.Supply;
import com.os.workshop.domain.product.UnitOfMeasure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyControllerTest {

    private static final BigDecimal COST = new BigDecimal("25.00");
    private static final BigDecimal SALE = new BigDecimal("49.90");
    private static final BigDecimal PACKAGE = new BigDecimal("1.00");

    @Mock private CreateSupplyUseCase createSupplyUseCase;
    @Mock private FindSupplyByIdUseCase findSupplyByIdUseCase;
    @Mock private FindSupplyBySkuUseCase findSupplyBySkuUseCase;
    @Mock private ListSuppliesUseCase listSuppliesUseCase;
    @Mock private UpdateSupplyUseCase updateSupplyUseCase;
    @Mock private DeactivateSupplyUseCase deactivateSupplyUseCase;

    @InjectMocks private SupplyController controller;

    @Test
    void criaInsumo() {
        when(createSupplyUseCase.execute("Oleo 5W30", "OIL-001", UnitOfMeasure.LITER, "Lubrificantes", "Mobil",
                COST, SALE, true, PACKAGE)).thenReturn(supply());

        var response = controller.create(new CreateSupplyRequest("Oleo 5W30", "OIL-001", UnitOfMeasure.LITER,
                "Lubrificantes", "Mobil", COST, SALE, true, PACKAGE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().sku()).isEqualTo("OIL-001");
    }

    @Test
    void buscaPorIdESku() {
        when(findSupplyByIdUseCase.execute(2L)).thenReturn(supply());
        when(findSupplyBySkuUseCase.execute("OIL-001")).thenReturn(supply());

        assertThat(controller.findById(2L).getBody().id()).isEqualTo(2L);
        assertThat(controller.findBySku("OIL-001").getBody().sku()).isEqualTo("OIL-001");
    }

    @Test
    void listaInsumos() {
        when(listSuppliesUseCase.execute()).thenReturn(List.of(supply(), supply()));

        assertThat(controller.findAll().getBody()).hasSize(2);
    }

    @Test
    void atualizaInsumo() {
        when(updateSupplyUseCase.execute(2L, "Oleo 5W30", "OIL-001", UnitOfMeasure.LITER, "Lubrificantes", "Mobil",
                COST, SALE, true, PACKAGE)).thenReturn(supply());

        var response = controller.update(2L, new UpdateSupplyRequest("Oleo 5W30", "OIL-001", UnitOfMeasure.LITER,
                "Lubrificantes", "Mobil", COST, SALE, true, PACKAGE));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void desativaInsumo() {
        assertThat(controller.deactivate(2L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deactivateSupplyUseCase).execute(2L);
    }

    private Supply supply() {
        Supply supply = new Supply();
        supply.setId(2L);
        supply.setName("Oleo 5W30");
        supply.setSku("OIL-001");
        supply.setType(ProductType.SUPPLY);
        supply.setUnit(UnitOfMeasure.LITER);
        supply.setCostPrice(COST);
        supply.setSalePrice(SALE);
        supply.setActive(true);
        supply.setFractionalAllowed(true);
        supply.setPackageSize(PACKAGE);
        return supply;
    }
}
