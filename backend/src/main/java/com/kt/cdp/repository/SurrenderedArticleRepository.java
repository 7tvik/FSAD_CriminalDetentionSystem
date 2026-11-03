package com.kt.cdp.repository;

import com.kt.cdp.entity.SurrenderedArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurrenderedArticleRepository extends JpaRepository<SurrenderedArticle, Long> {
    List<SurrenderedArticle> findByPrisonerId(Long prisonerId);
}
