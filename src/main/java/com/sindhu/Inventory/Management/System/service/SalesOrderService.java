package com.sindhu.Inventory.Management.System.service;

import com.sindhu.Inventory.Management.System.entity.ChangeType;
import com.sindhu.Inventory.Management.System.entity.Products;
import com.sindhu.Inventory.Management.System.entity.SalesOrder;
import com.sindhu.Inventory.Management.System.exception.ResourceNotFoundException;
import com.sindhu.Inventory.Management.System.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductService productService;
    private final StockLogService stockLogService;

    @Transactional
    public SalesOrder saveOrder(SalesOrder order) {
        // Get the product
        Products product = order.getProduct();

        // Check if sufficient stock is available
        if (product.getQuantity() < order.getQuantity()) {
            throw new IllegalStateException("Insufficient stock. Available: " + product.getQuantity() +
                    ", Requested: " + order.getQuantity());
        }

        // Store the previous quantity before modification
        int previousQuantity = product.getQuantity();

        // Decrease product quantity
        product.setQuantity(product.getQuantity() - order.getQuantity());
        product.setLastUpdated(LocalDateTime.now());

        // Pass the previous quantity to the save method
        productService.saveProduct(product, previousQuantity);

        // Create stock log entry
        stockLogService.createLog(product.getId(), ChangeType.REMOVE, order.getQuantity());

        // Save the sales order
        return salesOrderRepository.save(order);
    }

    public List<SalesOrder> getAllOrders() {
        return salesOrderRepository.findAll();
    }

    public SalesOrder findById(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order", id));
    }

    @Transactional
    public void deleteOrder(Long id) {
        SalesOrder order = findById(id);

        // Store the previous quantity before modification
        Products product = order.getProduct();
        int previousQuantity = product.getQuantity();

        // Restore product quantity when order is deleted
        product.setQuantity(product.getQuantity() + order.getQuantity());
        product.setLastUpdated(LocalDateTime.now());
        productService.saveProduct(product, previousQuantity);

        // Create stock log entry for restoration
        stockLogService.createLog(product.getId(), ChangeType.ADD, order.getQuantity());

        // Delete the order
        salesOrderRepository.deleteById(id);
    }
}