package com.os.workshop.features.stock;

import com.os.workshop.features.stock.confirmReservation.ConfirmReservationHandler;
import com.os.workshop.features.stock.create.CreateStockHandler;
import com.os.workshop.features.stock.create.CreateStockRequest;
import com.os.workshop.features.stock.findByProductId.FindStockByProductIdHandler;
import com.os.workshop.features.stock.findMovements.FindMovementsHandler;
import com.os.workshop.features.stock.findReservations.FindReservationsHandler;
import com.os.workshop.features.stock.list.ListStocksHandler;
import com.os.workshop.features.stock.releaseReservation.ReleaseReservationHandler;
import com.os.workshop.features.stock.entry.StockEntryHandler;
import com.os.workshop.features.stock.exit.StockExitHandler;
import com.os.workshop.features.stock.reserve.ReserveStockHandler;
import com.os.workshop.features.stock.shared.domain.Stock;
import com.os.workshop.features.stock.updateMinimum.UpdateMinimumHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StockControllerTest {

    private final CreateStockHandler createStockHandler = mock(CreateStockHandler.class);
    private final FindStockByProductIdHandler findStockByProductIdHandler = mock(FindStockByProductIdHandler.class);
    private final ListStocksHandler listStocksHandler = mock(ListStocksHandler.class);
    private final StockEntryHandler stockEntryHandler = mock(StockEntryHandler.class);
    private final StockExitHandler stockExitHandler = mock(StockExitHandler.class);
    private final UpdateMinimumHandler updateMinimumHandler = mock(UpdateMinimumHandler.class);
    private final FindMovementsHandler findMovementsHandler = mock(FindMovementsHandler.class);
    private final ReserveStockHandler reserveStockHandler = mock(ReserveStockHandler.class);
    private final ConfirmReservationHandler confirmReservationHandler = mock(ConfirmReservationHandler.class);
    private final ReleaseReservationHandler releaseReservationHandler = mock(ReleaseReservationHandler.class);
    private final FindReservationsHandler findReservationsHandler = mock(FindReservationsHandler.class);

    private final StockController controller = new StockController(
            createStockHandler, findStockByProductIdHandler, listStocksHandler,
            stockEntryHandler, stockExitHandler, updateMinimumHandler,
            findMovementsHandler, reserveStockHandler, confirmReservationHandler,
            releaseReservationHandler, findReservationsHandler);

    @Test
    void createReturns201() {
        Stock stock = new Stock(); stock.setId(1L); stock.setProductId(1L);
        stock.setQuantity(BigDecimal.TEN); stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setAvailableQuantity(BigDecimal.TEN); stock.setMinimumQuantity(BigDecimal.ONE);
        when(createStockHandler.handle(any(CreateStockRequest.class))).thenReturn(stock);
        assertEquals(HttpStatus.CREATED, controller.create(new CreateStockRequest(1L, BigDecimal.TEN, BigDecimal.ONE)).getStatusCode());
    }

    @Test
    void findByProductIdReturns200() {
        Stock stock = new Stock(); stock.setId(1L); stock.setProductId(1L);
        stock.setQuantity(BigDecimal.TEN); stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setAvailableQuantity(BigDecimal.TEN); stock.setMinimumQuantity(BigDecimal.ONE);
        when(findStockByProductIdHandler.handle(1L)).thenReturn(stock);
        assertEquals(HttpStatus.OK, controller.findByProductId(1L).getStatusCode());
    }

    @Test
    void findAllReturns200() {
        when(listStocksHandler.handle()).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.findAll().getStatusCode());
    }
}
