package com.os.workshop.stock.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.workshop.product.exception.GlobalExceptionHandler;
import com.os.workshop.stock.domain.Stock;
import com.os.workshop.stock.domain.StockReservation;
import com.os.workshop.stock.domain.StockReservationStatus;
import com.os.workshop.stock.repository.StockMovementRepository;
import com.os.workshop.stock.repository.StockRepository;
import com.os.workshop.stock.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockReservationControllerE2ETest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockReservationRepository reservationRepository;

    @Mock
    private StockMovementRepository movementRepository;

    @BeforeEach
    void setUp() {
        StockReservationService reservationService = new StockReservationService(
                stockRepository, reservationRepository, movementRepository);
        StockReservationController controller = new StockReservationController(reservationService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Stock createStock(Long productId) {
        Stock stock = new Stock();
        stock.setId(productId + 100L);
        stock.setProductId(productId);
        stock.setQuantity(new BigDecimal("100"));
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setMinimumQuantity(new BigDecimal("10"));
        stock.recalculateAvailableQuantity();
        stock.setCreatedAt(LocalDateTime.now());
        stock.setUpdatedAt(LocalDateTime.now());
        return stock;
    }

    @Test
    void whenReservingStockForServiceOrder_thenReturns201WithActiveStatus() throws Exception {
        Long productId = 1L;
        Long osId = 2L;
        Stock stock = createStock(productId);

        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> {
            StockReservation r = i.getArgument(0);
            r.setId(3L);
            r.setCreatedAt(LocalDateTime.now());
            return r;
        });

        StockReservationRequest request = new StockReservationRequest(osId, List.of(
                new StockReservationItemRequest(productId, new BigDecimal("10"))
        ));

        mockMvc.perform(post("/api/stocks/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void whenConfirmingReservations_thenReturns200WithConfirmedStatus() throws Exception {
        Long osId = 4L;
        Stock stock = createStock(5L);
        stock.reserve(new BigDecimal("20"));

        StockReservation reservation = new StockReservation();
        reservation.setId(6L);
        reservation.setStockId(stock.getId());
        reservation.setProductId(stock.getProductId());
        reservation.setServiceOrderId(osId);
        reservation.setQuantity(new BigDecimal("20"));
        reservation.setStatus(StockReservationStatus.ACTIVE);
        reservation.setCreatedAt(LocalDateTime.now());

        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(patch("/api/stocks/reservations/service-order/{osId}/confirm", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    void whenReleasingReservations_thenReturns200WithReleasedStatus() throws Exception {
        Long osId = 7L;
        Stock stock = createStock(8L);
        stock.reserve(new BigDecimal("15"));

        StockReservation reservation = new StockReservation();
        reservation.setId(9L);
        reservation.setStockId(stock.getId());
        reservation.setProductId(stock.getProductId());
        reservation.setServiceOrderId(osId);
        reservation.setQuantity(new BigDecimal("15"));
        reservation.setStatus(StockReservationStatus.ACTIVE);
        reservation.setCreatedAt(LocalDateTime.now());

        when(reservationRepository.findByServiceOrderIdAndStatus(osId, StockReservationStatus.ACTIVE))
                .thenReturn(List.of(reservation));
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(reservationRepository.save(any(StockReservation.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(patch("/api/stocks/reservations/service-order/{osId}/release", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RELEASED"));
    }

    @Test
    void whenFindingReservationsByServiceOrder_thenReturns200WithList() throws Exception {
        Long osId = 10L;
        StockReservation reservation = new StockReservation();
        reservation.setId(11L);
        reservation.setStockId(12L);
        reservation.setProductId(13L);
        reservation.setServiceOrderId(osId);
        reservation.setQuantity(new BigDecimal("5"));
        reservation.setStatus(StockReservationStatus.ACTIVE);
        reservation.setCreatedAt(LocalDateTime.now());

        when(reservationRepository.findByServiceOrderId(osId)).thenReturn(List.of(reservation));

        mockMvc.perform(get("/api/stocks/reservations/service-order/{osId}", osId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
