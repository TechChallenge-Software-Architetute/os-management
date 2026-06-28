package com.os.workshop.features.budget;

import com.os.workshop.features.budget.findByServiceOrder.FindBudgetByServiceOrderHandler;
import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.domain.BudgetItem;
import com.os.workshop.features.budget.shared.repository.BudgetRepository;
import com.os.workshop.domain.product.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BudgetControllerE2ETest {

    private MockMvc mockMvc;

    @Mock
    private BudgetRepository budgetRepository;

    @BeforeEach
    void setUp() {
        FindBudgetByServiceOrderHandler handler = new FindBudgetByServiceOrderHandler(budgetRepository);
        BudgetController controller = new BudgetController(handler);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Budget createBudgetWithItems(UUID osId) {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setServiceOrderId(osId);
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());

        var items = new ArrayList<BudgetItem>();

        BudgetItem item1 = BudgetItem.snapshot(
                1L, "Pastilha de Freio", "BRK-001", ProductType.PART,
                new BigDecimal("89.90"), new BigDecimal("2"));
        item1.setId(1L);
        item1.setBudgetId(1L);
        items.add(item1);

        BudgetItem item2 = BudgetItem.snapshot(
                2L, "Óleo Motor 5W30", "OIL-5W30", ProductType.SUPPLY,
                new BigDecimal("49.90"), new BigDecimal("3"));
        item2.setId(2L);
        item2.setBudgetId(1L);
        items.add(item2);

        budget.setItems(items);
        budget.recalculateTotalPrice();

        return budget;
    }

    @Test
    void whenFindingBudgetForOsWithReservations_thenReturns200WithItemsAndTotal() throws Exception {
        UUID osId = UUID.randomUUID();
        Budget budget = createBudgetWithItems(osId);
        when(budgetRepository.findByServiceOrderId(osId)).thenReturn(Optional.of(budget));

        mockMvc.perform(get("/api/budgets/service-order/{osId}", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceOrderId").value(osId.toString()))
                .andExpect(jsonPath("$.totalPrice").value(329.50))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productName").value("Pastilha de Freio"))
                .andExpect(jsonPath("$.items[0].productType").value("PART"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(89.90))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].totalPrice").value(179.80))
                .andExpect(jsonPath("$.items[1].productName").value("Óleo Motor 5W30"))
                .andExpect(jsonPath("$.items[1].productType").value("SUPPLY"))
                .andExpect(jsonPath("$.items[1].unitPrice").value(49.90))
                .andExpect(jsonPath("$.items[1].quantity").value(3))
                .andExpect(jsonPath("$.items[1].totalPrice").value(149.70));
    }

    @Test
    void whenFindingBudgetForOsWithoutReservations_thenReturns404() throws Exception {
        UUID osId = UUID.randomUUID();
        when(budgetRepository.findByServiceOrderId(osId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/budgets/service-order/{osId}", osId))
                .andExpect(status().isNotFound());
    }
}
