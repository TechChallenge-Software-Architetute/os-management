package com.os.workshop.application.stock;

import com.os.workshop.application.stock.port.out.StockMovementRepository;
import com.os.workshop.application.stock.port.out.StockRepository;
import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.stock.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockUseCaseTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CreateStockUseCase createStockUseCase;

    @InjectMocks
    private ListStocksUseCase listStocksUseCase;

    @InjectMocks
    private FindStockByProductIdUseCase findStockByProductIdUseCase;

    @InjectMocks
    private StockEntryUseCase stockEntryUseCase;

    @InjectMocks
    private StockExitUseCase stockExitUseCase;

    @InjectMocks
    private ReserveStockUseCase reserveStockUseCase;

    @InjectMocks
    private ReleaseReservationUseCase releaseReservationUseCase;

    @InjectMocks
    private ConfirmReservationUseCase confirmReservationUseCase;

    private Stock createStock(Long id, Long productId, BigDecimal quantity, BigDecimal reserved, BigDecimal minimum) {
        Stock stock = new Stock();
        stock.setId(id);
        stock.setProductId(productId);
        stock.setQuantity(quantity);
        stock.setReservedQuantity(reserved);
        stock.setAvailableQuantity(quantity.subtract(reserved));
        stock.setMinimumQuantity(minimum);
        return stock;
    }

    // ==================== CreateStockUseCase ====================

    @Test
    void createStock_whenProductDoesNotExist_thenSavesStock() {
        when(stockRepository.existsByProductId(1L)).thenReturn(false);
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        Stock result = createStockUseCase.execute(1L, new BigDecimal("100"), new BigDecimal("10"));

        assertEquals(1L, result.getProductId());
        assertEquals(new BigDecimal("100"), result.getQuantity());
        assertEquals(new BigDecimal("10"), result.getMinimumQuantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void createStock_whenProductAlreadyExists_thenThrowsIllegalArgument() {
        when(stockRepository.existsByProductId(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                createStockUseCase.execute(1L, new BigDecimal("100"), new BigDecimal("10")));

        verify(stockRepository, never()).save(any());
    }

    // ==================== ListStocksUseCase ====================

    @Test
    void listStocks_returnsAllStocks() {
        Stock stock1 = createStock(1L, 1L, new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));
        Stock stock2 = createStock(2L, 2L, new BigDecimal("50"), BigDecimal.ZERO, new BigDecimal("5"));
        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));

        List<Stock> result = listStocksUseCase.execute();

        assertEquals(2, result.size());
        verify(stockRepository).findAll();
    }

    @Test
    void listStocks_whenEmpty_returnsEmptyList() {
        when(stockRepository.findAll()).thenReturn(List.of());

        List<Stock> result = listStocksUseCase.execute();

        assertTrue(result.isEmpty());
    }

    @Test
    void listLowStock_returnsOnlyLowStockItems() {
        Stock lowStock = createStock(1L, 1L, new BigDecimal("5"), BigDecimal.ZERO, new BigDecimal("10"));
        Stock normalStock = createStock(2L, 2L, new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));
        when(stockRepository.findAll()).thenReturn(List.of(lowStock, normalStock));

        List<Stock> result = listStocksUseCase.executeLowStock();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // ==================== FindStockByProductIdUseCase ====================

    @Test
    void findStockByProductId_whenExists_returnsStock() {
        Stock stock = createStock(1L, 10L, new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));
        when(stockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));

        Stock result = findStockByProductIdUseCase.execute(10L);

        assertEquals(10L, result.getProductId());
    }

    @Test
    void findStockByProductId_whenNotFound_throwsIllegalArgument() {
        when(stockRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> findStockByProductIdUseCase.execute(999L));
    }

    // ==================== StockEntryUseCase ====================

    @Test
    void stockEntry_whenStockExists_addsQuantityAndRecordsMovement() {
        Stock stock = createStock(1L, 5L, new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));
        when(stockRepository.findByProductId(5L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        Stock result = stockEntryUseCase.execute(5L, new BigDecimal("25"), "Restock");

        assertEquals(new BigDecimal("125"), result.getQuantity());
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void stockEntry_whenStockNotFound_throwsIllegalArgument() {
        when(stockRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                stockEntryUseCase.execute(999L, new BigDecimal("10"), "Restock"));

        verify(stockRepository, never()).save(any());
    }

    // ==================== StockExitUseCase ====================

    @Test
    void stockExit_whenSufficientStock_removesQuantityAndRecordsMovement() {
        Stock stock = createStock(1L, 5L, new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));
        when(stockRepository.findByProductId(5L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));

        Stock result = stockExitUseCase.execute(5L, new BigDecimal("30"), "Sale");

        assertEquals(new BigDecimal("70"), result.getQuantity());
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void stockExit_whenStockNotFound_throwsIllegalArgument() {
        when(stockRepository.findByProductId(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                stockExitUseCase.execute(999L, new BigDecimal("10"), "Sale"));
    }

    @Test
    void stockExit_whenInsufficientStock_throwsIllegalState() {
        Stock stock = createStock(1L, 5L, new BigDecimal("10"), BigDecimal.ZERO, new BigDecimal("5"));
        when(stockRepository.findByProductId(5L)).thenReturn(Optional.of(stock));

        assertThrows(IllegalStateException.class, () ->
                stockExitUseCase.execute(5L, new BigDecimal("50"), "Sale"));
    }

    // ==================== ReserveStockUseCase ====================

    @Test
    void reserveStock_whenSufficientAvailable_createsReservation() {
        UUID orderId = UUID.randomUUID();
        Stock stock = createStock(1L, 10L, new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("10"));
        when(stockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        List<ReserveStockUseCase.ReserveItem> items = List.of(
                new ReserveStockUseCase.ReserveItem(10L, new BigDecimal("20"))
        );

        List<StockReservation> result = reserveStockUseCase.execute(orderId, items);

        assertEquals(1, result.size());
        assertEquals(StockReservationStatus.ACTIVE, result.get(0).getStatus());
        assertEquals(new BigDecimal("20"), result.get(0).getQuantity());
        verify(eventPublisher).publishEvent(any(ReservationChangedEvent.class));
    }

    @Test
    void reserveStock_whenStockNotFound_throwsIllegalArgument() {
        UUID orderId = UUID.randomUUID();
        when(stockRepository.findByProductId(99L)).thenReturn(Optional.empty());

        List<ReserveStockUseCase.ReserveItem> items = List.of(
                new ReserveStockUseCase.ReserveItem(99L, new BigDecimal("10"))
        );

        assertThrows(IllegalArgumentException.class, () -> reserveStockUseCase.execute(orderId, items));
    }

    @Test
    void reserveStock_whenInsufficientAvailable_throwsIllegalState() {
        UUID orderId = UUID.randomUUID();
        Stock stock = createStock(1L, 10L, new BigDecimal("5"), BigDecimal.ZERO, new BigDecimal("2"));
        when(stockRepository.findByProductId(10L)).thenReturn(Optional.of(stock));

        List<ReserveStockUseCase.ReserveItem> items = List.of(
                new ReserveStockUseCase.ReserveItem(10L, new BigDecimal("50"))
        );

        assertThrows(IllegalStateException.class, () -> reserveStockUseCase.execute(orderId, items));
    }

    // ==================== ReleaseReservationUseCase ====================

    @Test
    void releaseReservation_whenActiveReservationsExist_releasesAll() {
        UUID orderId = UUID.randomUUID();
        Stock stock = createStock(1L, 10L, new BigDecimal("100"), new BigDecimal("20"), new BigDecimal("10"));

        StockReservation reservation = new StockReservation();
        reservation.setId(1L);
        reservation.setStockId(1L);
        reservation.setProductId(10L);
        reservation.setServiceOrderId(orderId);
        reservation.setQuantity(new BigDecimal("20"));
        reservation.setStatus(StockReservationStatus.ACTIVE);

        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        List<StockReservation> result = releaseReservationUseCase.execute(orderId);

        assertEquals(1, result.size());
        assertEquals(StockReservationStatus.RELEASED, result.get(0).getStatus());
        verify(eventPublisher).publishEvent(any(ReservationChangedEvent.class));
    }

    @Test
    void releaseReservation_whenNoActiveReservations_throwsIllegalArgument() {
        UUID orderId = UUID.randomUUID();
        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> releaseReservationUseCase.execute(orderId));
    }

    @Test
    void releaseReservation_whenStockNotFound_throwsIllegalState() {
        UUID orderId = UUID.randomUUID();

        StockReservation reservation = new StockReservation();
        reservation.setId(1L);
        reservation.setStockId(99L);
        reservation.setQuantity(new BigDecimal("10"));
        reservation.setStatus(StockReservationStatus.ACTIVE);

        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> releaseReservationUseCase.execute(orderId));
    }

    // ==================== ConfirmReservationUseCase ====================

    @Test
    void confirmReservation_whenActiveReservationsExist_confirmsAll() {
        UUID orderId = UUID.randomUUID();
        Stock stock = createStock(1L, 10L, new BigDecimal("100"), new BigDecimal("30"), new BigDecimal("10"));

        StockReservation reservation = new StockReservation();
        reservation.setId(1L);
        reservation.setStockId(1L);
        reservation.setProductId(10L);
        reservation.setServiceOrderId(orderId);
        reservation.setQuantity(new BigDecimal("30"));
        reservation.setStatus(StockReservationStatus.ACTIVE);

        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        List<StockReservation> result = confirmReservationUseCase.execute(orderId);

        assertEquals(1, result.size());
        assertEquals(StockReservationStatus.CONFIRMED, result.get(0).getStatus());
        verify(eventPublisher).publishEvent(any(ReservationChangedEvent.class));
    }

    @Test
    void confirmReservation_whenNoActiveReservations_throwsIllegalArgument() {
        UUID orderId = UUID.randomUUID();
        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> confirmReservationUseCase.execute(orderId));
    }

    @Test
    void confirmReservation_whenStockNotFound_throwsIllegalState() {
        UUID orderId = UUID.randomUUID();

        StockReservation reservation = new StockReservation();
        reservation.setId(1L);
        reservation.setStockId(99L);
        reservation.setQuantity(new BigDecimal("10"));
        reservation.setStatus(StockReservationStatus.ACTIVE);

        when(reservationRepository.findByServiceOrderIdAndStatus(orderId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
        when(stockRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> confirmReservationUseCase.execute(orderId));
    }
}
