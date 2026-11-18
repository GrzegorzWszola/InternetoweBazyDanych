package com.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.entities.EquipmentItem;

@Repository
public interface EquipmentItemRepository extends JpaRepository<EquipmentItem, Long>, JpaSpecificationExecutor<EquipmentItem> {
    boolean existsByModelId(Long modelId);
    boolean existsByInventoryNumber(String inventoryNumber);
    boolean existsByInventoryNumberAndIdNot(String inventoryNumber, Long id);
    boolean existsBySerialNumber(String serialNumber);
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);
}
