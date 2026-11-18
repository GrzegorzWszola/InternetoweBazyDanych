package com.backend.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.api.controllers.ReservationsApi;
import com.backend.api.model.CreateReservationRequest;
import com.backend.api.model.IssueEquipmentRequest;
import com.backend.api.model.ReservationArchiveResponse;
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
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid CreateReservationRequest createReservationRequest) {
        ReservationResponse response = reservationService.createReservation(createReservationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<ReservationResponse> approveReservation(Long id) {
        return ResponseEntity.ok(reservationService.approveReservation(id));
    }

    @Override
    public ResponseEntity<ReservationResponse> cancelReservation(Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @Override
    public ResponseEntity<ReservationResponse> rejectReservation(Long id) {
        return ResponseEntity.ok(reservationService.rejectReservation(id));
    }

    @Override
    public ResponseEntity<ReservationResponse> issueEquipment(Long id,
            @Valid IssueEquipmentRequest issueEquipmentRequest) {
        return ResponseEntity.ok(reservationService.issueEquipment(id, issueEquipmentRequest));
    }

    @Override
    public ResponseEntity<ReservationResponse> returnEquipment(Long id,
            @Valid ReturnEquipmentRequest returnEquipmentRequest) {
        return ResponseEntity.ok(reservationService.returnEquipment(id, returnEquipmentRequest));
    }

    @Override
    public ResponseEntity<ReservationResponse> getReservation(Long id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @Override
    public ResponseEntity<List<ReservationArchiveResponse>> getReservationHistory(Long id) {
        return ResponseEntity.ok(reservationService.getReservationArchive(id));
    }

    @Override
    public ResponseEntity<ReservationPage> listMyReservations(
            @Min(0) @Valid Integer page,
            @Min(1) @Max(100) @Valid Integer size) {
        return ResponseEntity.ok(reservationService.listMyReservations(page, size));
    }

    @Override
    public ResponseEntity<ReservationPage> listReservations(
            @Valid ReservationStatus status,
            @Min(0) @Valid Integer page,
            @Min(1) @Max(100) @Valid Integer size) {
        return ResponseEntity.ok(reservationService.listReservations(status, page, size));
    }
}
