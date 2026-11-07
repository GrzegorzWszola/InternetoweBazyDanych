package com.backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.api.controllers.ReservationsApi;
import com.backend.api.model.CreateReservationRequest;
import com.backend.api.model.IssueEquipmentRequest;
import com.backend.api.model.ReservationHistoryResponse;
import com.backend.api.model.ReservationPage;
import com.backend.api.model.ReservationResponse;
import com.backend.api.model.ReservationStatus;
import com.backend.api.model.ReturnEquipmentRequest;
import com.backend.services.ReservationService;

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
        throw new UnsupportedOperationException("Unimplemented method 'approveReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> cancelReservation(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'cancelReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid CreateReservationRequest createReservationRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'createReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> getReservation(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'getReservation'");
    }

    @Override
    public ResponseEntity<List<ReservationHistoryResponse>> getReservationHistory(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'getReservationHistory'");
    }

    @Override
    public ResponseEntity<ReservationResponse> issueEquipment(Integer id,
            @Valid IssueEquipmentRequest issueEquipmentRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'issueEquipment'");
    }

    @Override
    public ResponseEntity<ReservationPage> listMyReservations(@Min(0) @Valid Integer page,
            @Min(1) @Max(100) @Valid Integer size) {
        throw new UnsupportedOperationException("Unimplemented method 'listMyReservations'");
    }

    @Override
    public ResponseEntity<ReservationPage> listReservations(@Valid ReservationStatus status,
            @Min(0) @Valid Integer page, @Min(1) @Max(100) @Valid Integer size) {
        throw new UnsupportedOperationException("Unimplemented method 'listReservations'");
    }

    @Override
    public ResponseEntity<ReservationResponse> rejectReservation(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'rejectReservation'");
    }

    @Override
    public ResponseEntity<ReservationResponse> returnEquipment(Integer id,
            @Valid ReturnEquipmentRequest returnEquipmentRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'returnEquipment'");
    }
}
