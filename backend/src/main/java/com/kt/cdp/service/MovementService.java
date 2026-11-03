package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.common.enums.MovementType;
import com.kt.cdp.dto.movement.MovementRequest;
import com.kt.cdp.dto.movement.MovementResponse;
import com.kt.cdp.entity.Movement;
import com.kt.cdp.entity.Staff;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.mapper.MovementMapper;
import com.kt.cdp.repository.MovementRepository;
import com.kt.cdp.repository.StaffRepository;
import com.kt.cdp.spec.MovementSpecifications;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages the in-out (movement) register including its state machine:
 * <pre>
 *   SCHEDULED --&gt; OUT --&gt; RETURNED
 *   SCHEDULED | OUT --&gt; CANCELLED
 * </pre>
 * On create the status defaults to SCHEDULED, or OUT when a {@code timeOut} is
 * already supplied (i.e. the person left at creation time). OVERDUE is derived
 * in the mapper and never stored.
 */
@Service
public class MovementService {

    private final MovementRepository movementRepository;
    private final StaffRepository staffRepository;
    private final MovementMapper movementMapper;
    private final AuditService auditService;

    public MovementService(MovementRepository movementRepository, StaffRepository staffRepository,
                           MovementMapper movementMapper, AuditService auditService) {
        this.movementRepository = movementRepository;
        this.staffRepository = staffRepository;
        this.movementMapper = movementMapper;
        this.auditService = auditService;
    }

    @Transactional
    public MovementResponse create(MovementRequest request) {
        Movement movement = new Movement();
        apply(movement, request);
        // Default to SCHEDULED; if the person already left (timeOut set) start OUT.
        movement.setStatus(request.timeOut() != null ? MovementStatus.OUT : MovementStatus.SCHEDULED);
        movementRepository.save(movement);

        auditService.record("CREATE", "Movement", movement.getId());
        return movementMapper.toResponse(movement);
    }

    @Transactional
    public MovementResponse update(Long id, MovementRequest request) {
        Movement movement = load(id);
        // Field edits only; status changes go through the dedicated transitions.
        apply(movement, request);
        movementRepository.save(movement);

        auditService.record("UPDATE", "Movement", movement.getId());
        return movementMapper.toResponse(movement);
    }

    @Transactional(readOnly = true)
    public MovementResponse getById(Long id) {
        return movementMapper.toResponse(load(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<MovementResponse> search(Long personRefId, MovementType movementType, MovementStatus status,
                                                 LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Specification<Movement> spec = Specification.where(MovementSpecifications.personRefId(personRefId))
                .and(MovementSpecifications.movementType(movementType))
                .and(MovementSpecifications.status(status))
                .and(MovementSpecifications.dateFrom(dateFrom))
                .and(MovementSpecifications.dateTo(dateTo));
        return PageResponse.of(movementRepository.findAll(spec, pageable).map(movementMapper::toResponse));
    }

    /** OUT -&gt; RETURNED. Records the actual return time (provided or now). */
    @Transactional
    public MovementResponse markReturned(Long id, LocalDateTime actualReturn) {
        Movement movement = load(id);
        if (movement.getStatus() != MovementStatus.OUT) {
            throw new BusinessRuleException(
                    "Only a movement that is OUT can be marked RETURNED (current: " + movement.getStatus() + ")");
        }
        movement.setActualReturn(actualReturn != null ? actualReturn : LocalDateTime.now());
        movement.setStatus(MovementStatus.RETURNED);
        movementRepository.save(movement);

        auditService.record("RETURN", "Movement", movement.getId());
        return movementMapper.toResponse(movement);
    }

    /** SCHEDULED | OUT -&gt; CANCELLED. */
    @Transactional
    public MovementResponse cancel(Long id) {
        Movement movement = load(id);
        if (movement.getStatus() != MovementStatus.SCHEDULED && movement.getStatus() != MovementStatus.OUT) {
            throw new BusinessRuleException(
                    "Only SCHEDULED or OUT movements can be cancelled (current: " + movement.getStatus() + ")");
        }
        movement.setStatus(MovementStatus.CANCELLED);
        movementRepository.save(movement);

        auditService.record("CANCEL", "Movement", movement.getId());
        return movementMapper.toResponse(movement);
    }

    @Transactional(readOnly = true)
    public List<MovementResponse> getOverdue() {
        return movementRepository.findOverdue(LocalDateTime.now()).stream()
                .map(movementMapper::toResponse)
                .toList();
    }

    private Movement load(Long id) {
        return movementRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Movement", id));
    }

    private void apply(Movement movement, MovementRequest request) {
        movement.setPersonType(request.personType());
        movement.setPersonRefId(request.personRefId());
        movement.setPersonName(request.personName());
        movement.setMovementType(request.movementType());
        movement.setDestination(request.destination());
        movement.setReason(request.reason());
        movement.setMovementDate(request.movementDate());
        movement.setTimeOut(request.timeOut());
        movement.setExpectedReturn(request.expectedReturn());
        movement.setRemarks(request.remarks());
        movement.setEscortOfficer(resolveEscort(request.escortOfficerId()));
    }

    private Staff resolveEscort(Long escortOfficerId) {
        if (escortOfficerId == null) {
            return null;
        }
        return staffRepository.findById(escortOfficerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Staff", escortOfficerId));
    }
}
