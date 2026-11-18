package com.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.api.model.EquipmentPage;
import com.backend.api.model.EquipmentRequest;
import com.backend.api.model.EquipmentResponse;
import com.backend.entities.AppUser;
import com.backend.entities.Category;
import com.backend.entities.Department;
import com.backend.entities.EquipmentItem;
import com.backend.entities.EquipmentModel;
import com.backend.entities.EquipmentStatus;
import com.backend.entities.HazardClass;
import com.backend.entities.Role;
import com.backend.repositories.AppUserRepository;
import com.backend.repositories.CategoryRepository;
import com.backend.repositories.DepartmentRepository;
import com.backend.repositories.EquipmentItemRepository;
import com.backend.repositories.EquipmentModelRepository;
import com.backend.repositories.HazardClassRepository;
import com.backend.repositories.ReservationRepository;

@Service
public class EquipmentService {

    private final EquipmentModelRepository equipmentModelRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final HazardClassRepository hazardClassRepository;
    private final ReservationRepository reservationRepository;
    private final AppUserRepository userRepository;

    public EquipmentService(EquipmentModelRepository equipmentModelRepository,
                            EquipmentItemRepository equipmentItemRepository,
                            CategoryRepository categoryRepository,
                            DepartmentRepository departmentRepository,
                            HazardClassRepository hazardClassRepository,
                            ReservationRepository reservationRepository,
                            AppUserRepository userRepository) {
        this.equipmentModelRepository = equipmentModelRepository;
        this.equipmentItemRepository = equipmentItemRepository;
        this.categoryRepository = categoryRepository;
        this.departmentRepository = departmentRepository;
        this.hazardClassRepository = hazardClassRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
    }

    // ---- Equipment Items (physical units) ----

    @Transactional(readOnly = true)
    public EquipmentPage listEquipment(Long categoryId, Long departmentId, Long hazardClassId,
                                       String q, Integer page, Integer size) {
        int pageIndex = (page != null && page >= 0) ? page : 0;
        int pageSize  = (size != null && size > 0)  ? size : 20;

        Specification<EquipmentItem> spec = (root, query, cb) -> cb.conjunction();

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("model").get("category").get("id"), categoryId));
        }
        if (departmentId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("model").get("department").get("id"), departmentId));
        }
        if (hazardClassId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("model").get("hazardClass").get("id"), hazardClassId));
        }
        if (q != null && !q.trim().isEmpty()) {
            String pattern = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("model").get("name")), pattern),
                    cb.like(cb.lower(root.get("inventoryNumber")), pattern),
                    cb.like(cb.lower(root.get("serialNumber")), pattern)
            ));
        }

        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<EquipmentItem> itemPage = equipmentItemRepository.findAll(spec, pageable);

        List<EquipmentResponse> content = itemPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new EquipmentPage()
                .content(content)
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipment(Long id) {
        EquipmentItem item = equipmentItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment item not found with id: " + id));
        return mapToResponse(item);
    }

    @Transactional
    public EquipmentResponse createEquipment(EquipmentRequest request) {
        checkAdminRole();

        if (request == null || request.getCategoryId() == null || request.getDepartmentId() == null ||
                request.getName() == null || request.getInventoryNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required fields are missing");
        }

        if (equipmentItemRepository.existsByInventoryNumber(request.getInventoryNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory number already exists: " + request.getInventoryNumber());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found with id: " + request.getCategoryId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found with id: " + request.getDepartmentId()));

        HazardClass hazardClass = null;
        if (request.getHazardClassId() != null) {
            hazardClass = hazardClassRepository.findById(request.getHazardClassId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hazard class not found with id: " + request.getHazardClassId()));
        }

        // Create or reuse EquipmentModel
        EquipmentModel model = new EquipmentModel();
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setCategory(category);
        model.setDepartment(department);
        model.setHazardClass(hazardClass);
        model.setSpecs(request.getSpecs());
        model = equipmentModelRepository.save(model);

        // Create EquipmentItem
        EquipmentItem item = new EquipmentItem();
        item.setModel(model);
        item.setInventoryNumber(request.getInventoryNumber());
        // serialNumber defaults to inventoryNumber when not separately provided via basic API
        item.setSerialNumber(request.getInventoryNumber());
        if (request.getStatus() != null) {
            item.setStatus(EquipmentStatus.valueOf(request.getStatus().getValue()));
        } else {
            item.setStatus(EquipmentStatus.AVAILABLE);
        }

        EquipmentItem saved = equipmentItemRepository.save(item);
        return mapToResponse(saved);
    }

    @Transactional
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest request) {
        checkAdminRole();

        if (request == null || request.getCategoryId() == null || request.getDepartmentId() == null ||
                request.getName() == null || request.getInventoryNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Required fields are missing");
        }

        EquipmentItem item = equipmentItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment item not found with id: " + id));

        if (equipmentItemRepository.existsByInventoryNumberAndIdNot(request.getInventoryNumber(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inventory number already exists: " + request.getInventoryNumber());
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found with id: " + request.getCategoryId()));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found with id: " + request.getDepartmentId()));

        HazardClass hazardClass = null;
        if (request.getHazardClassId() != null) {
            hazardClass = hazardClassRepository.findById(request.getHazardClassId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hazard class not found with id: " + request.getHazardClassId()));
        }

        EquipmentModel model = item.getModel();
        model.setName(request.getName());
        model.setDescription(request.getDescription());
        model.setCategory(category);
        model.setDepartment(department);
        model.setHazardClass(hazardClass);
        model.setSpecs(request.getSpecs());
        equipmentModelRepository.save(model);

        item.setInventoryNumber(request.getInventoryNumber());
        if (request.getStatus() != null) {
            item.setStatus(EquipmentStatus.valueOf(request.getStatus().getValue()));
        }

        EquipmentItem updated = equipmentItemRepository.save(item);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        checkAdminRole();

        if (!equipmentItemRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment item not found with id: " + id);
        }

        if (reservationRepository.existsByItemId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Equipment item is referenced by reservations and cannot be deleted");
        }

        equipmentItemRepository.deleteById(id);
    }

    private void checkAdminRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        AppUser user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins are allowed to perform this operation");
        }
    }

    public EquipmentResponse mapToResponse(EquipmentItem item) {
        EquipmentResponse response = new EquipmentResponse();
        response.setId(item.getId());
        EquipmentModel model = item.getModel();
        if (model != null) {
            if (model.getCategory() != null) {
                response.setCategoryId(model.getCategory().getId());
                response.setCategoryName(model.getCategory().getName());
            }
            if (model.getDepartment() != null) {
                response.setDepartmentId(model.getDepartment().getId());
                response.setDepartmentName(model.getDepartment().getName());
            }
            if (model.getHazardClass() != null) {
                response.setHazardClassId(model.getHazardClass().getId());
                response.setHazardClassName(model.getHazardClass().getName());
            }
            response.setName(model.getName());
            response.setDescription(model.getDescription());
            response.setSpecs(model.getSpecs());
        }
        response.setInventoryNumber(item.getInventoryNumber());
        if (item.getStatus() != null) {
            response.setStatus(com.backend.api.model.EquipmentStatus.fromValue(item.getStatus().name()));
        }
        return response;
    }
}
