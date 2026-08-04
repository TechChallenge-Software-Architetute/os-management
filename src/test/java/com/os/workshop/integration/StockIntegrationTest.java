package com.os.workshop.integration;

import com.os.workshop.integration.config.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StockIntegrationTest extends IntegrationTestBase {

    @Test
    void should_create_part_stock_and_perform_entry_and_exit() {
        var token = authenticateAsAdmin();

        // Step 1: Create a part (product)
        var partId = createPart(token, "BRK-PAD-001");

        // Step 2: Create stock for the part
        var stockId = createStock(token, partId, new BigDecimal("10.00"), new BigDecimal("5.00"));
        assertThat(stockId).isPositive();

        // Step 3: Verify initial stock
        var stockResponse = restTemplate.exchange("/api/stocks/product/" + partId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        assertThat(stockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new BigDecimal(stockResponse.getBody().get("quantity").toString()))
                .isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat((Boolean) stockResponse.getBody().get("lowStock")).isFalse();

        // Step 4: Stock entry (+5 units)
        var entryBody = Map.of("quantity", 5.00, "reason", "Compra fornecedor");
        var entryResponse = restTemplate.exchange("/api/stocks/product/" + partId + "/entry",
                HttpMethod.PATCH, new HttpEntity<>(entryBody, authHeaders(token)), Map.class);
        assertThat(entryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new BigDecimal(entryResponse.getBody().get("quantity").toString()))
                .isEqualByComparingTo(new BigDecimal("15.00"));

        // Step 5: Stock exit (-12 units)
        var exitBody = Map.of("quantity", 12.00, "reason", "Uso em OS");
        var exitResponse = restTemplate.exchange("/api/stocks/product/" + partId + "/exit",
                HttpMethod.PATCH, new HttpEntity<>(exitBody, authHeaders(token)), Map.class);
        assertThat(exitResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(new BigDecimal(exitResponse.getBody().get("quantity").toString()))
                .isEqualByComparingTo(new BigDecimal("3.00"));
        // 3 < minimum(5), so lowStock = true
        assertThat((Boolean) exitResponse.getBody().get("lowStock")).isTrue();

        // Step 6: Verify movements
        var movementsResponse = restTemplate.exchange("/api/stocks/product/" + partId + "/movements",
                HttpMethod.GET, new HttpEntity<>(authHeaders(token)), List.class);
        assertThat(movementsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(movementsResponse.getBody()).hasSize(2);

        // Step 7: List all stocks
        var listResponse = restTemplate.exchange("/api/stocks", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), List.class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }

    @Test
    void should_create_supply_stock_and_check_low_stock() {
        var token = authenticateAsAdmin();

        // Create a supply (product)
        var supplyId = createSupply(token, "OIL-5W30-001");

        // Create stock with quantity already below minimum
        createStock(token, supplyId, new BigDecimal("2.00"), new BigDecimal("10.00"));

        // Check low stock endpoint
        var lowStockResponse = restTemplate.exchange("/api/stocks/low", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), List.class);
        assertThat(lowStockResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lowStockResponse.getBody()).isNotEmpty();
    }
}
