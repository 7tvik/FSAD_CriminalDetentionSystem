package com.kt.cdp.entity;

import com.kt.cdp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "staff")
@Getter
@Setter
public class Staff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "staff_number", nullable = false, unique = true)
    private String staffNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "rank_title")
    private String rankTitle;

    private String contact;

    @Column(nullable = false)
    private boolean active = true;
}
