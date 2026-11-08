package com.os.workshop.application.budget;

import com.os.workshop.application.budget.port.out.BudgetRepository;
import com.os.workshop.application.product.part.port.out.PartRepository;
import com.os.workshop.application.product.supply.port.out.SupplyRepository;
import com.os.workshop.application.stock.port.out.StockReservationRepository;
import com.os.workshop.domain.budget.Budget;
import com.os.workshop.domain.budget.BudgetItem;
import com.os.workshop.domain.product.Product;
import com.os.workshop.domain.stock.StockReservation;
import com.os.workshop.domain.stock.StockReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecalculateBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final StockReservationRepository reservationRepository;
    private final PartRepository partRepository;
    private final SupplyRepository supplyRepository;

    @Transactional
    public Budget execute(UUID serviceOrderId) {
        // Entram as reservas ativas e as ja confirmadas (baixadas do estoque pelo
        // ATUALIZAR_ESTOQUE da saga). Antes so as ACTIVE contavam, e a baixa do
        // estoque zerava o orcamento que o cliente tinha aprovado.
        List<StockReservation> activeReservations = reservationRepository
                .findByServiceOrderId(serviceOrderId).stream()
                .filter(reservation -> reservation.getStatus() != StockReservationStatus.RELEASED)
                .toList();

        Budget budget = budgetRepository.findByServiceOrderId(serviceOrderId)
                .orElseGet(() -> {
                    Budget newBudget = new Budget();
                    newBudget.setServiceOrderId(serviceOrderId);
                    return newBudget;
                });

        if (budget.getId() != null) {
            budgetRepository.deleteItemsByBudgetId(budget.getId());
        }

        List<BudgetItem> items = new ArrayList<>();
        for (StockReservation reservation : activeReservations) {
            Product product = findProduct(reservation.getProductId());
            if (product != null) {
                items.add(BudgetItem.snapshot(
                        product.getId(), product.getName(), product.getSku(),
                        product.getType(), product.getSalePrice(), reservation.getQuantity()));
            }
        }

        budget.setItems(items);
        budget.recalculateTotalPrice();
        return budgetRepository.save(budget);
    }

    private Product findProduct(Long productId) {
        Optional<?> part = partRepository.findById(productId);
        if (part.isPresent()) {
            return (Product) part.get();
        }
        return supplyRepository.findById(productId).orElse(null);
    }
}
