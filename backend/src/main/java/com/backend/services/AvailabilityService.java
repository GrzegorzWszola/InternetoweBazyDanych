package com.backend.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.api.model.AvailabilityResponse;
import com.backend.entities.EquipmentItem;
import com.backend.entities.EquipmentStatus;
import com.backend.entities.ReservationStatus;
import com.backend.repositories.EquipmentItemRepository;
import com.backend.repositories.ReservationRepository;

@Service
public class AvailabilityService {

    private final EquipmentItemRepository equipmentItemRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(EquipmentItemRepository equipmentItemRepository,
                               ReservationRepository reservationRepository) {
        this.equipmentItemRepository = equipmentItemRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(Long itemId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "From date cannot be after to date");
        }

        EquipmentItem item = equipmentItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment item not found with id: " + itemId));

        if (item.getStatus() != EquipmentStatus.AVAILABLE) {
            return new AvailabilityResponse()
                    .itemId(itemId)
                    .from(from)
                    .to(to)
                    .available(false)
                    .reason("Equipment status is " + item.getStatus());
        }

        boolean isBooked = reservationRepository.existsOverlapping(
                itemId,
                from,
                to,
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.ISSUED)
        );

        if (isBooked) {
            return new AvailabilityResponse()
                    .itemId(itemId)
                    .from(from)
                    .to(to)
                    .available(false)
                    .reason("Equipment is already reserved in this period");
        }

        return new AvailabilityResponse()
                .itemId(itemId)
                .from(from)
                .to(to)
                .available(true)
                .reason(null);
    }
}
