//package com.os.workshop.stock.reservation;
//
//import com.os.workshop.stock.domain.Stock;
//import com.os.workshop.stock.domain.StockMovement;
//import com.os.workshop.stock.domain.StockReservation;
//import com.os.workshop.stock.domain.StockReservationStatus;
//import com.os.workshop.stock.repository.StockMovementRepository;
//import com.os.workshop.stock.repository.StockRepository;
//import com.os.workshop.stock.repository.StockReservationRepository;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class StockReservationServiceTest {
//
//    @Mock
//    private StockRepository stockRepository;
//
//    @Mock
//    private StockReservationRepository reservationRepository;
//
//    @Mock
//    private StockMovementRepository movementRepository;
//
//    @InjectMocks
//    private StockReservationService reservationService;
//
//    private Stock createStock(Long productId) {
//        Stock stock = new Stock();
//        stock.setId(productId + 100L);
//        stock.setProductId(productId);
//        stock.setQuantity(new BigDecimal("100"));
//        stock.setReservedQuantity(BigDecimal.ZERO);
//        stock.setMinimumQuantity(new BigDecimal("10"));
//        stock.recalculateAvailableQuantity();
//        return stock;
//    }
//
//    @Test
//    void whenReservingWithSufficientStock_thenAllItemsAreReserved() {
//        Long productId1 = 1L;
//        Long productId2 = 2L;
//        UUID osId = UUID.randomUUID();
//
//        Stock stock1 = createStock(productId1);
//        Stock stock2 = createStock(productId2);
//
//        when(stockRepository.findByProductId(productId1)).thenReturn(Optional.of(stock1));
//        when(stockRepository.findByProductId(productId2)).thenReturn(Optional.of(stock2));
//        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
//        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));
//
//        StockReservationRequest request = new StockReservationRequest(osId, List.of(
//                new StockReservationItemRequest(productId1, new BigDecimal("10")),
//                new StockReservationItemRequest(productId2, new BigDecimal("20"))
//        ));
//
//        List<StockReservation> result = reservationService.reserveForServiceOrder(request);
//
//        assertEquals(2, result.size());
//        assertEquals(new BigDecimal("10"), stock1.getReservedQuantity());
//        assertEquals(new BigDecimal("20"), stock2.getReservedQuantity());
//        verify(movementRepository, times(2)).save(any(StockMovement.class));
//    }
//
//    @Test
//    void whenReservingWithInsufficientStock_thenFailsAtomicallyAndNothingIsReserved() {
//        Long productId1 = 4L;
//        Long productId2 = 5L;
//
//        Stock stock1 = createStock(productId1);
//        Stock stock2 = createStock(productId2);
//        stock2.setQuantity(new BigDecimal("5"));
//        stock2.recalculateAvailableQuantity();
//
//        when(stockRepository.findByProductId(productId1)).thenReturn(Optional.of(stock1));
//        when(stockRepository.findByProductId(productId2)).thenReturn(Optional.of(stock2));
//
//        StockReservationRequest request = new StockReservationRequest(6L, List.of(
//                new StockReservationItemRequest(productId1, new BigDecimal("10")),
//                new StockReservationItemRequest(productId2, new BigDecimal("20"))
//        ));
//
//        assertThrows(IllegalStateException.class, () -> reservationService.reserveForServiceOrder(request));
//        assertEquals(BigDecimal.ZERO, stock1.getReservedQuantity());
//    }
//
//    @Test
//    void whenConfirmingReservations_thenStockIsDeductedAndStatusIsConfirmed() {
//        Long osId = 7L;
//        Stock stock = createStock(8L);
//        stock.reserve(new BigDecimal("30"));
//
//        StockReservation reservation = new StockReservation();
//        reservation.setId(9L);
//        reservation.setStockId(stock.getId());
//        reservation.setQuantity(new BigDecimal("30"));
//        reservation.setStatus(StockReservationStatus.ACTIVE);
//
//        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
//                .thenReturn(List.of(reservation));
//        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
//        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
//        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));
//
//        List<StockReservation> result = reservationService.confirmReservations(osId);
//
//        assertEquals(1, result.size());
//        assertEquals(StockReservationStatus.CONFIRMED, result.get(0).getStatus());
//        assertEquals(new BigDecimal("70"), stock.getQuantity());
//        assertEquals(BigDecimal.ZERO, stock.getReservedQuantity());
//    }
//
//    @Test
//    void whenReleasingReservations_thenStockBecomesAvailableAndStatusIsReleased() {
//        Long osId = 10L;
//        Stock stock = createStock(11L);
//        stock.reserve(new BigDecimal("25"));
//
//        StockReservation reservation = new StockReservation();
//        reservation.setId(12L);
//        reservation.setStockId(stock.getId());
//        reservation.setQuantity(new BigDecimal("25"));
//        reservation.setStatus(StockReservationStatus.ACTIVE);
//
//        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
//                .thenReturn(List.of(reservation));
//        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
//        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
//        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));
//
//        List<StockReservation> result = reservationService.releaseReservations(osId);
//
//        assertEquals(StockReservationStatus.RELEASED, result.get(0).getStatus());
//        assertEquals(new BigDecimal("100"), stock.getAvailableQuantity());
//        assertEquals(BigDecimal.ZERO, stock.getReservedQuantity());
//    }
//
//    @Test
//    void whenConfirmingWithNoActiveReservations_thenThrowsIllegalArgument() {
//        Long osId = 13L;
//        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
//                .thenReturn(List.of());
//
//        assertThrows(IllegalArgumentException.class, () -> reservationService.confirmReservations(osId));
//    }
//}
