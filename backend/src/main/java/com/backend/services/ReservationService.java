package com.backend.services;

import org.springframework.stereotype.Service;

import com.backend.repositories.ReservationRepository;

@Service 
public class ReservationService {
    
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
}
