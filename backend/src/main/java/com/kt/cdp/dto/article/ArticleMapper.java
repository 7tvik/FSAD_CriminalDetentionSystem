package com.kt.cdp.dto.article;

import com.kt.cdp.entity.Prisoner;
import com.kt.cdp.entity.SurrenderedArticle;
import org.springframework.stereotype.Component;

/** Converts between {@link SurrenderedArticle} entities and article DTOs. */
@Component
public class ArticleMapper {

    /**
     * Builds a new article entity from the request. The owning prisoner is set by the service;
     * the return status defaults to HELD (via the entity default).
     */
    public SurrenderedArticle toEntity(ArticleRequest request) {
        SurrenderedArticle article = new SurrenderedArticle();
        apply(article, request);
        return article;
    }

    /** Copies mutable fields from the request onto an existing article, preserving return state. */
    public void updateEntity(SurrenderedArticle existing, ArticleRequest request) {
        apply(existing, request);
    }

    private void apply(SurrenderedArticle article, ArticleRequest request) {
        article.setName(request.name());
        article.setCategory(request.category());
        article.setQuantity(request.quantity());
        article.setDescription(request.description());
        article.setConditionNote(request.conditionNote());
        article.setReceivedDate(request.receivedDate());
        article.setRemarks(request.remarks());
    }

    public ArticleResponse toResponse(SurrenderedArticle article) {
        Prisoner prisoner = article.getPrisoner();
        Long prisonerId = prisoner != null ? prisoner.getId() : null;
        String prisonerName = prisoner != null
                ? prisoner.getFirstName() + " " + prisoner.getLastName()
                : null;
        String prisonerNumber = prisoner != null ? prisoner.getPrisonerNumber() : null;
        return new ArticleResponse(
                article.getId(),
                prisonerId,
                prisonerName,
                prisonerNumber,
                article.getName(),
                article.getCategory(),
                article.getQuantity(),
                article.getDescription(),
                article.getConditionNote(),
                article.getReceivedDate(),
                article.getReturnedDate(),
                article.getReturnStatus(),
                article.getRemarks(),
                article.getCreatedAt()
        );
    }
}
