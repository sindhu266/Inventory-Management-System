package com.sindhu.Inventory.Management.System.repository;

import com.sindhu.Inventory.Management.System.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
}