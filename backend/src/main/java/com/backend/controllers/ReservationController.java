package com.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.services.ReservationService;

import com.backend.api.controllers.ReservationsApi;
import com.backend.api.model.ConditionNoteRequest;
import com.backend.api.model.CreateReservationRequest;
import com.backend.api.model.ReservationPage;
import com.backend.api.model.ReservationResponse;
import com.backend.api.model.ReservationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController 
public class ReservationController implements ReservationsApi {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Override
    public ResponseEntity<ReservationResponse> approveReservation(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> cancelReservation(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cancelReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid CreateReservationRequest createReservationRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> issueEquipment(Integer id,
            @Valid ConditionNoteRequest conditionNoteRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'issueEquipment'");
    }

    @Override
    public ResponseEntity<ReservationPage> listMyReservations(@Min(0) @Valid Integer page,
            @Min(1) @Max(100) @Valid Integer size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listMyReservations'");
    }

    @Override
    public ResponseEntity<ReservationPage> listReservations(@Valid ReservationStatus status,
            @Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listReservations'");
    }

    @Override
    public ResponseEntity<ReservationResponse> rejectReservation(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> returnEquipment(Integer id,
            @Valid ConditionNoteRequest conditionNoteRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'returnEquipment'");
    }
    
}
