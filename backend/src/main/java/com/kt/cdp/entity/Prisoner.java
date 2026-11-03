package com.kt.cdp.entity;

import com.kt.cdp.common.BaseEntity;
import com.kt.cdp.common.enums.PrisonerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "prisoners")
@Getter
@Setter
public class Prisoner extends BaseEntity {

    @Column(name = "prisoner_number", nullable = false, unique = true)
    private String prisonerNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    private String address;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrisonerStatus status = PrisonerStatus.ACTIVE;

    private String facility;
    private String ward;
    private String cell;

    @Column(name = "expected_release_date")
    private LocalDate expectedReleaseDate;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(nullable = false)
    private boolean active = true;
}
