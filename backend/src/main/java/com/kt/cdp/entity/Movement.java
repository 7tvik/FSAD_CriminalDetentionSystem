package com.kt.cdp.entity;

import com.kt.cdp.common.BaseEntity;
import com.kt.cdp.common.enums.MovementStatus;
import com.kt.cdp.common.enums.MovementType;
import com.kt.cdp.common.enums.PersonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "movements")
@Getter
@Setter
public class Movement extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false)
    private PersonType personType;

    @Column(name = "person_ref_id")
    private Long personRefId;

    @Column(name = "person_name", nullable = false)
    private String personName;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    private String destination;

    private String reason;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "time_out")
    private LocalDateTime timeOut;

    @Column(name = "expected_return")
    private LocalDateTime expectedReturn;

    @Column(name = "actual_return")
    private LocalDateTime actualReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_officer")
    private Staff escortOfficer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementStatus status = MovementStatus.SCHEDULED;

    private String remarks;
}
