package com.os.workshop.features.stock.management;

import com.os.workshop.features.stock.domain.Stock;
import com.os.workshop.features.stock.domain.StockMovement;
import com.os.workshop.features.stock.domain.StockMovementType;
import com.os.workshop.features.stock.repository.StockMovementRepository;
import com.os.workshop.features.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service responsible for stock management business rules.
 * <p>
 * Each product has a single stock record tracking total quantity, reserved quantity,
 * and a minimum threshold for low-stock alerts.
 * <p>
 * The {@code availableQuantity} is always computed as {@code quantity - reservedQuantity}
 * and used for OS availability check.
 * <p>
 * Every stock change (entry, exit or reservation) is recorded as a {@link StockMovement} for full audit trail.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;

    /**
     * Creates a stock record for a product.
     * Each product can only have one stock record.
     *
     * @param request contains the product ID, initial quantity, and minimum threshold
     * @return the created stock record
     * @throws IllegalArgumentException if a stock record already exists for the product
     */
    @Transactional
    public Stock create(StockRequest request) {
        if (stockRepository.existsByProductId(request.productId())) {
            throw new IllegalArgumentException("Stock already exists for product: " + request.productId());
        }

        Stock stock = new Stock();
        stock.setProductId(request.productId());
        stock.setQuantity(request.quantity());
        stock.setMinimumQuantity(request.minimumQuantity());
        return stockRepository.save(stock);
    }

    /**
     * Retrieves the stock record for a product.
     *
     * @param productId the product UUID
     * @return the stock domain object
     * @throws IllegalArgumentException if no stock record exists for the product
     */
    @Transactional(readOnly = true)
    public Stock findByProductId(Long productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stock not found for product: " + productId));
    }

    /**
     * Returns all stock records in the system.
     *
     * @return list of all stock records
     */
    @Transactional(readOnly = true)
    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    /**
     * Returns stock records where the available quantity (total - reserved) is at or below
     * the minimum threshold. Useful for generating restock alerts.
     *
     * @return list of low-stock records
     */
    @Transactional(readOnly = true)
    public List<Stock> findLowStock() {
        return stockRepository.findAll().stream()
                .filter(Stock::isLowStock)
                .toList();
    }

    /**
     * Adds quantity to a product's stock (entry movement).
     * The added quantity increases both total and available stock.
     * A movement record of type ENTRY is created for audit.
     *
     * @param productId the product UUID
     * @param request   contains the quantity to add and an optional reason
     * @return the updated stock record
     * @throws IllegalArgumentException if the quantity is not positive or stock is not found
     */
    @Transactional
    public Stock addStock(Long productId, StockMovementRequest request) {
        Stock stock = findByProductId(productId);
        stock.addQuantity(request.quantity());

        Stock saved = stockRepository.save(stock);
        recordMovement(saved.getId(), StockMovementType.ENTRY, request.quantity(), request.reason());
        return saved;
    }

    /**
     * Removes quantity from a product's stock (exit movement).
     * Only the available quantity (total - reserved) can be removed.
     * A movement record of type EXIT is created for audit.
     *
     * @param productId the product UUID
     * @param request   contains the quantity to remove and an optional reason
     * @return the updated stock record
     * @throws IllegalArgumentException if the quantity is not positive or stock is not found
     * @throws IllegalStateException    if there is insufficient available stock
     */
    @Transactional
    public Stock removeStock(Long productId, StockMovementRequest request) {
        Stock stock = findByProductId(productId);
        stock.removeQuantity(request.quantity());

        Stock saved = stockRepository.save(stock);
        recordMovement(saved.getId(), StockMovementType.EXIT, request.quantity(), request.reason());
        return saved;
    }

    /**
     * Updates the minimum stock threshold for a product.
     * Low-stock detection uses available quantity (total - reserved) against this threshold.
     *
     * @param productId       the product UUID
     * @param minimumQuantity the new minimum threshold (must be >= 0)
     * @return the updated stock record
     * @throws IllegalArgumentException if the value is negative or stock is not found
     */
    @Transactional
    public Stock updateMinimumQuantity(Long productId, BigDecimal minimumQuantity) {
        if (minimumQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum quantity cannot be negative");
        }
        Stock stock = findByProductId(productId);
        stock.setMinimumQuantity(minimumQuantity);
        return stockRepository.save(stock);
    }

    /**
     * Returns the full movement history for a product's stock.
     * Includes entries, exits, reservations, confirmations, and releases.
     *
     * @param productId the product UUID
     * @return list of movements ordered by most recent first
     * @throws IllegalArgumentException if no stock record exists for the product
     */
    @Transactional(readOnly = true)
    public List<StockMovement> findMovements(Long productId) {
        Stock stock = findByProductId(productId);
        return movementRepository.findByStockId(stock.getId());
    }

    private void recordMovement(Long stockId, StockMovementType type, BigDecimal quantity, String reason) {
        StockMovement movement = new StockMovement();
        movement.setStockId(stockId);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);
        movementRepository.save(movement);
    }
}
