package com.kt.cdp.entity;

import com.kt.cdp.common.BaseEntity;
import com.kt.cdp.common.enums.CaseStatus;
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

@Entity
@Table(name = "cases")
@Getter
@Setter
public class CaseRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prisoner_id", nullable = false)
    private Prisoner prisoner;

    @Column(name = "case_number", nullable = false, unique = true)
    private String caseNumber;

    @Column(name = "fir_number")
    private String firNumber;

    @Column(name = "crime_type", nullable = false)
    private String crimeType;

    @Column(name = "crime_description")
    private String crimeDescription;

    @Column(name = "police_station")
    private String policeStation;

    private String court;

    private String sections;

    @Column(name = "arrest_date")
    private LocalDate arrestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status = CaseStatus.UNDER_INVESTIGATION;

    @Column(name = "investigating_officer")
    private String investigatingOfficer;

    private String remarks;
}
