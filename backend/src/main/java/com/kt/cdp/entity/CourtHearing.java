package com.kt.cdp.entity;

import com.kt.cdp.common.BaseEntity;
import com.kt.cdp.common.enums.CourtHearingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "court_hearings")
@Getter
@Setter
public class CourtHearing extends BaseEntity {

    /** Optional link to a general movement record (movement_type = COURT). */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movement_id")
    private Movement movement;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prisoner_id", nullable = false)
    private Prisoner prisoner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private CaseRecord caseRecord;

    @Column(nullable = false)
    private String court;

    @Column(name = "hearing_date", nullable = false)
    private LocalDate hearingDate;

    private String destination;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "expected_return")
    private LocalDateTime expectedReturn;

    @Column(name = "actual_return")
    private LocalDateTime actualReturn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escort_officer")
    private Staff escortOfficer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtHearingStatus status = CourtHearingStatus.SCHEDULED;

    private String outcome;

    private String remarks;
}
