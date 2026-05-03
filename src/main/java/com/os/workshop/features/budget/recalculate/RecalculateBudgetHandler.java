package com.os.workshop.features.budget.recalculate;

import com.os.workshop.features.budget.shared.domain.Budget;
import com.os.workshop.features.budget.shared.domain.BudgetItem;
import com.os.workshop.features.budget.shared.repository.BudgetRepository;
import com.os.workshop.features.product.domain.Part;
import com.os.workshop.features.product.domain.Product;
import com.os.workshop.features.product.repository.PartRepository;
import com.os.workshop.features.product.repository.SupplyRepository;
import com.os.workshop.features.stock.domain.StockReservation;
import com.os.workshop.features.stock.domain.StockReservationStatus;
import com.os.workshop.features.stock.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Recalculates the budget for a service order based on its active reservations.
 * <p>
 * A budget is automatically derived from the active stock reservations of a service order.
 * Each budget item is a snapshot of the product's price at the time of calculation.
 * When reservations change, the budget is fully rebuilt from the current active reservations.
 */
@Service
@RequiredArgsConstructor
public class RecalculateBudgetHandler {

    private final BudgetRepository budgetRepository;
    private final StockReservationRepository reservationRepository;
    private final PartRepository partRepository;
    private final SupplyRepository supplyRepository;

    /**
     * Recalculates the budget for a service order based on its active reservations.
     * If no active reservations exist, the budget is cleared (total = 0, no items).
     * If a budget already exists, its items are wiped and rebuilt.
     *
     * @param serviceOrderId the UUID of the service order
     * @return the recalculated budget
     */
    @Transactional
    public Budget handle(UUID serviceOrderId) {
        List<StockReservation> activeReservations = reservationRepository
                .findByServiceOrderIdAndStatus(serviceOrderId, StockReservationStatus.ACTIVE);

        // Find or create budget
        Budget budget = budgetRepository.findByServiceOrderId(serviceOrderId)
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setServiceOrderId(serviceOrderId);
                    return newBudget;
                });

        // Clear existing items if budget already existed
        if (budget.getId() != null) {
            budgetRepository.deleteItemsByBudgetId(budget.getId());
        }

        // Build items from active reservations
        List<BudgetItem> items = new ArrayList<>();
        for (StockReservation reservation : activeReservations) {
            Product product = findProduct(reservation.getProductId());
            if (product != null) {
                BudgetItem item = BudgetItem.snapshot(
                        product.getId(),
                        product.getName(),
                        product.getSku(),
                        product.getType(),
                        product.getSalePrice(),
                        reservation.getQuantity()
                );
                items.add(item);
            }
        }

        budget.setItems(items);
        budget.recalculateTotalPrice();

        return budgetRepository.save(budget);
    }

    private Product findProduct(Long productId) {
        Optional<Part> part = partRepository.findById(productId);
        if (part.isPresent()) {
            return part.get();
        }
        return supplyRepository.findById(productId).orElse(null);
    }
}
