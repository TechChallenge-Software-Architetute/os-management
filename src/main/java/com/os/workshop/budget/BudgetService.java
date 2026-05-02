package com.os.workshop.budget;

import com.os.workshop.budget.domain.Budget;
import com.os.workshop.budget.domain.BudgetItem;
import com.os.workshop.budget.repository.BudgetRepository;
import com.os.workshop.product.domain.Part;
import com.os.workshop.product.domain.Product;
import com.os.workshop.product.domain.Supply;
import com.os.workshop.product.repository.PartRepository;
import com.os.workshop.product.repository.SupplyRepository;
import com.os.workshop.stock.domain.StockReservation;
import com.os.workshop.stock.domain.StockReservationStatus;
import com.os.workshop.stock.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for budget calculation and management.
 * <p>
 * A budget is automatically derived from the active stock reservations of a service order.
 * Each budget item is a snapshot of the product's price at the time of calculation.
 * When reservations change, the budget is fully rebuilt from the current active reservations.
 */
@Service
@RequiredArgsConstructor
public class BudgetService {

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
    public Budget recalculate(UUID serviceOrderId) {
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

    /**
     * Retrieves the budget for a service order, if it exists.
     *
     * @param serviceOrderId the UUID of the service order
     * @return the budget, or empty if no budget exists yet
     */
    @Transactional(readOnly = true)
    public Optional<Budget> findByServiceOrderId(UUID serviceOrderId) {
        return budgetRepository.findByServiceOrderId(serviceOrderId);
    }

    private Product findProduct(Long productId) {
        Optional<Part> part = partRepository.findById(productId);
        if (part.isPresent()) {
            return part.get();
        }
        return supplyRepository.findById(productId).orElse(null);
    }
}
