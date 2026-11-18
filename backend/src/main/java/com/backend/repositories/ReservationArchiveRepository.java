package com.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.entities.ReservationArchive;

@Repository
public interface ReservationArchiveRepository extends JpaRepository<ReservationArchive, Long> {
    List<ReservationArchive> findByUserId(Long userId);
    List<ReservationArchive> findByItemId(Long itemId);
}
