package com.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.backend.entities.EquipmentModel;

@Repository
public interface EquipmentModelRepository extends JpaRepository<EquipmentModel, Long>, JpaSpecificationExecutor<EquipmentModel> {
    boolean existsByCategoryId(Long categoryId);
    boolean existsByDepartmentId(Long departmentId);
    boolean existsByHazardClassId(Long hazardClassId);
}
