package com.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.entities.ReservationHistory;

@Repository
public interface ReservationHistoryRepository extends JpaRepository<ReservationHistory, Integer> {
    List<ReservationHistory> findByReservationIdOrderByChangedAtAsc(Integer reservationId);
}
