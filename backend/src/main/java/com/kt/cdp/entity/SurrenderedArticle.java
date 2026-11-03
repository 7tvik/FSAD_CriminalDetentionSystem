package com.kt.cdp.entity;

import com.kt.cdp.common.BaseEntity;
import com.kt.cdp.common.enums.ArticleReturnStatus;
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
@Table(name = "surrendered_articles")
@Getter
@Setter
public class SurrenderedArticle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prisoner_id", nullable = false)
    private Prisoner prisoner;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(nullable = false)
    private int quantity = 1;

    private String description;

    @Column(name = "condition_note")
    private String conditionNote;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", nullable = false)
    private ArticleReturnStatus returnStatus = ArticleReturnStatus.HELD;

    private String remarks;
}
