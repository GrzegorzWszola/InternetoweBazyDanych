package com.backend.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.api.model.CreateReservationRequest;
import com.backend.api.model.IssueEquipmentRequest;
import com.backend.api.model.ReservationArchiveResponse;
import com.backend.api.model.ReservationPage;
import com.backend.api.model.ReservationResponse;
import com.backend.api.model.ReturnEquipmentRequest;
import com.backend.entities.AppUser;
import com.backend.entities.ArchiveReason;
import com.backend.entities.ClearanceLevel;
import com.backend.entities.EquipmentItem;
import com.backend.entities.EquipmentStatus;
import com.backend.entities.Reservation;
import com.backend.entities.ReservationArchive;
import com.backend.entities.ReservationStatus;
import com.backend.entities.Role;
import com.backend.repositories.AppUserRepository;
import com.backend.repositories.EquipmentItemRepository;
import com.backend.repositories.ReservationArchiveRepository;
import com.backend.repositories.ReservationRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationArchiveRepository reservationArchiveRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final AppUserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationArchiveRepository reservationArchiveRepository,
                              EquipmentItemRepository equipmentItemRepository,
                              AppUserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationArchiveRepository = reservationArchiveRepository;
        this.equipmentItemRepository = equipmentItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        AppUser currentUser = getCurrentUser();

        if (request == null || request.getItemId() == null || request.getStartDate() == null || request.getEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item ID, start date, and end date are required");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
        }

        EquipmentItem item = equipmentItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment item not found with id: " + request.getItemId()));

        if (item.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Equipment item is currently not available (status: " + item.getStatus() + ")");
        }

        // Check clearance level
        if (item.getModel() != null && item.getModel().getHazardClass() != null
                && item.getModel().getHazardClass().getRequiredLevel() != null) {
            ClearanceLevel required = item.getModel().getHazardClass().getRequiredLevel();
            ClearanceLevel userLevel = currentUser.getClearanceLevel();
            if (userLevel == null || userLevel.ordinal() < required.ordinal()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User clearance level is insufficient for this equipment");
            }
        }

        boolean isBooked = reservationRepository.existsOverlapping(
                item.getId(),
                request.getStartDate(),
                request.getEndDate(),
                List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.ISSUED)
        );
        if (isBooked) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Equipment item is already booked in the requested period");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(currentUser);
        reservation.setItem(item);
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        AppUser currentUser = getCurrentUser();
        Reservation reservation = findReservationById(id);

        if (currentUser.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel reservation with status " + reservation.getStatus());
        }

        archiveReservation(reservation, ArchiveReason.CANCELLED, "Cancelled by user or admin");
        EquipmentItem item = reservation.getItem();
        if (item != null && item.getStatus() == EquipmentStatus.IN_SERVICE) {
            item.setStatus(EquipmentStatus.AVAILABLE);
            equipmentItemRepository.save(item);
        }
        ReservationResponse response = mapToResponse(reservation);
        reservationRepository.delete(reservation);
        return response;
    }

    @Transactional
    public ReservationResponse approveReservation(Long id) {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can approve reservations");
        }

        Reservation reservation = findReservationById(id);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING reservations can be approved");
        }

        reservation.setStatus(ReservationStatus.APPROVED);
        EquipmentItem item = reservation.getItem();
        if (item != null) {
            item.setStatus(EquipmentStatus.IN_SERVICE);
            equipmentItemRepository.save(item);
        }

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Transactional
    public ReservationResponse rejectReservation(Long id) {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can reject reservations");
        }

        Reservation reservation = findReservationById(id);
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING reservations can be rejected");
        }

        archiveReservation(reservation, ArchiveReason.REJECTED, "Rejected by admin");
        EquipmentItem item = reservation.getItem();
        if (item != null && item.getStatus() == EquipmentStatus.IN_SERVICE) {
            item.setStatus(EquipmentStatus.AVAILABLE);
            equipmentItemRepository.save(item);
        }
        ReservationResponse response = mapToResponse(reservation);
        reservationRepository.delete(reservation);
        return response;
    }

    @Transactional
    public ReservationResponse issueEquipment(Long id, IssueEquipmentRequest request) {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can issue equipment");
        }

        Reservation reservation = findReservationById(id);
        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only APPROVED reservations can be issued");
        }

        reservation.setStatus(ReservationStatus.ISSUED);
        reservation.setIssuedAt(LocalDateTime.now());
        if (request != null && request.getIssueNote() != null) {
            reservation.setIssueNote(request.getIssueNote());
        }

        EquipmentItem item = reservation.getItem();
        if (item != null) {
            item.setStatus(EquipmentStatus.IN_SERVICE);
            equipmentItemRepository.save(item);
        }

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Transactional
    public ReservationResponse returnEquipment(Long id, ReturnEquipmentRequest request) {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can accept returned equipment");
        }

        Reservation reservation = findReservationById(id);
        if (reservation.getStatus() != ReservationStatus.ISSUED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only ISSUED reservations can be returned");
        }

        reservation.setStatus(ReservationStatus.RETURNED);
        reservation.setReturnedAt(LocalDateTime.now());
        if (request != null && request.getReturnNote() != null) {
            reservation.setReturnNote(request.getReturnNote());
        }

        EquipmentItem item = reservation.getItem();
        if (item != null) {
            item.setStatus(EquipmentStatus.AVAILABLE);
            equipmentItemRepository.save(item);
        }

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(Long id) {
        AppUser currentUser = getCurrentUser();
        Reservation reservation = findReservationById(id);

        if (currentUser.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this reservation");
        }

        return mapToResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationArchiveResponse> getReservationArchive(Long itemId) {
        return reservationArchiveRepository.findByItemId(itemId).stream()
                .map(this::mapToArchiveResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationPage listMyReservations(Integer page, Integer size) {
        AppUser currentUser = getCurrentUser();
        int pageIndex = (page != null && page >= 0) ? page : 0;
        int pageSize  = (size != null && size > 0)  ? size : 20;

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Reservation> reservationPage = reservationRepository.findByUserId(currentUser.getId(), pageable);
        return mapToPage(reservationPage);
    }

    @Transactional(readOnly = true)
    public ReservationPage listReservations(com.backend.api.model.ReservationStatus status, Integer page, Integer size) {
        AppUser currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can list all reservations");
        }

        int pageIndex = (page != null && page >= 0) ? page : 0;
        int pageSize  = (size != null && size > 0)  ? size : 20;

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Reservation> reservationPage;
        if (status != null) {
            ReservationStatus entityStatus = ReservationStatus.valueOf(status.getValue());
            reservationPage = reservationRepository.findByStatus(entityStatus, pageable);
        } else {
            reservationPage = reservationRepository.findAll(pageable);
        }

        return mapToPage(reservationPage);
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found with id: " + id));
    }

    private void archiveReservation(Reservation reservation, ArchiveReason reason, String reasonNote) {
        ReservationArchive archive = new ReservationArchive();
        archive.setOriginalId(reservation.getId());
        archive.setUser(reservation.getUser());
        archive.setItem(reservation.getItem());
        archive.setStartDate(reservation.getStartDate());
        archive.setEndDate(reservation.getEndDate());
        archive.setFinalStatus(reason);
        archive.setCreatedAt(reservation.getCreatedAt());
        archive.setArchivedAt(LocalDateTime.now());
        archive.setReason(reasonNote);
        reservationArchiveRepository.save(archive);
    }

    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    public ReservationResponse mapToResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        if (reservation.getUser() != null) {
            response.setUserId(reservation.getUser().getId());
            response.setUserName(reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName());
        }
        if (reservation.getItem() != null) {
            EquipmentItem item = reservation.getItem();
            response.setItemId(item.getId());
            response.setInventoryNumber(item.getInventoryNumber());
            response.setSerialNumber(item.getSerialNumber());
            if (item.getModel() != null) {
                response.setModelName(item.getModel().getName());
            }
        }
        response.setStartDate(reservation.getStartDate());
        response.setEndDate(reservation.getEndDate());
        if (reservation.getStatus() != null) {
            response.setStatus(com.backend.api.model.ReservationStatus.fromValue(reservation.getStatus().name()));
        }
        if (reservation.getCreatedAt() != null) {
            response.setCreatedAt(reservation.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        }
        if (reservation.getIssuedAt() != null) {
            response.setIssuedAt(reservation.getIssuedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        }
        if (reservation.getReturnedAt() != null) {
            response.setReturnedAt(reservation.getReturnedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        }
        response.setIssueNote(reservation.getIssueNote());
        response.setReturnNote(reservation.getReturnNote());
        return response;
    }

    private ReservationArchiveResponse mapToArchiveResponse(ReservationArchive archive) {
        ReservationArchiveResponse response = new ReservationArchiveResponse();
        response.setId(archive.getId());
        response.setOriginalId(archive.getOriginalId());
        if (archive.getUser() != null) {
            response.setUserId(archive.getUser().getId());
            response.setUserName(archive.getUser().getFirstName() + " " + archive.getUser().getLastName());
        }
        if (archive.getItem() != null) {
            EquipmentItem item = archive.getItem();
            response.setItemId(item.getId());
            response.setInventoryNumber(item.getInventoryNumber());
            if (item.getModel() != null) {
                response.setModelName(item.getModel().getName());
            }
        }
        response.setStartDate(archive.getStartDate());
        response.setEndDate(archive.getEndDate());
        if (archive.getFinalStatus() != null) {
            response.setFinalStatus(com.backend.api.model.ArchiveReason.fromValue(archive.getFinalStatus().name()));
        }
        if (archive.getCreatedAt() != null) {
            response.setCreatedAt(archive.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        }
        if (archive.getArchivedAt() != null) {
            response.setArchivedAt(archive.getArchivedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        }
        response.setReason(archive.getReason());
        return response;
    }

    private ReservationPage mapToPage(Page<Reservation> reservationPage) {
        List<ReservationResponse> content = reservationPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new ReservationPage()
                .content(content)
                .page(reservationPage.getNumber())
                .size(reservationPage.getSize())
                .totalElements(reservationPage.getTotalElements())
                .totalPages(reservationPage.getTotalPages());
    }
}
