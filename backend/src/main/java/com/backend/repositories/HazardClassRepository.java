package com.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.entities.HazardClass;

@Repository
public interface HazardClassRepository extends JpaRepository<HazardClass, Integer> {
}
