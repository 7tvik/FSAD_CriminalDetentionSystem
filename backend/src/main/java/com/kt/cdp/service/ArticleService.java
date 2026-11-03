package com.kt.cdp.service;

import com.kt.cdp.audit.AuditService;
import com.kt.cdp.common.enums.ArticleReturnStatus;
import com.kt.cdp.dto.article.ArticleMapper;
import com.kt.cdp.dto.article.ArticleRequest;
import com.kt.cdp.dto.article.ArticleResponse;
import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.SurrenderedArticle;
import com.kt.cdp.exception.BusinessRuleException;
import com.kt.cdp.exception.ResourceNotFoundException;
import com.kt.cdp.repository.PrisonerRepository;
import com.kt.cdp.repository.SurrenderedArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Manages articles surrendered by prisoners and held in custody until returned. */
@Service
public class ArticleService {

    private final SurrenderedArticleRepository articleRepository;
    private final PrisonerRepository prisonerRepository;
    private final ArticleMapper articleMapper;
    private final AuditService auditService;

    public ArticleService(SurrenderedArticleRepository articleRepository, PrisonerRepository prisonerRepository,
                          ArticleMapper articleMapper, AuditService auditService) {
        this.articleRepository = articleRepository;
        this.prisonerRepository = prisonerRepository;
        this.articleMapper = articleMapper;
        this.auditService = auditService;
    }

    /** Records an article surrendered by a prisoner. Return status defaults to HELD. */
    @Transactional
    public ArticleResponse addForPrisoner(Long prisonerId, ArticleRequest request) {
        Prisoner prisoner = prisonerRepository.findById(prisonerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Prisoner", prisonerId));
        SurrenderedArticle article = articleMapper.toEntity(request);
        article.setPrisoner(prisoner);
        article.setReturnStatus(ArticleReturnStatus.HELD);
        SurrenderedArticle saved = articleRepository.save(article);
        auditService.record("CREATE", "SurrenderedArticle", saved.getId());
        return articleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getByPrisoner(Long prisonerId) {
        return articleRepository.findByPrisonerId(prisonerId).stream()
                .map(articleMapper::toResponse)
                .toList();
    }

    /** Marks a held article as returned. Rejects a second return. */
    @Transactional
    public ArticleResponse markReturned(Long articleId) {
        SurrenderedArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", articleId));
        if (article.getReturnStatus() == ArticleReturnStatus.RETURNED) {
            throw new BusinessRuleException("Article has already been returned");
        }
        article.setReturnStatus(ArticleReturnStatus.RETURNED);
        article.setReturnedDate(LocalDate.now());
        SurrenderedArticle saved = articleRepository.save(article);
        auditService.record("RETURN", "SurrenderedArticle", saved.getId());
        return articleMapper.toResponse(saved);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleRequest request) {
        SurrenderedArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Article", articleId));
        articleMapper.updateEntity(article, request);
        SurrenderedArticle saved = articleRepository.save(article);
        auditService.record("UPDATE", "SurrenderedArticle", saved.getId());
        return articleMapper.toResponse(saved);
    }
}
