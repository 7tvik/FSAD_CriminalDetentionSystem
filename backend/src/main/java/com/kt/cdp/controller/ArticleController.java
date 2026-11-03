package com.kt.cdp.controller;

import com.kt.cdp.dto.article.ArticleRequest;
import com.kt.cdp.dto.article.ArticleResponse;
import com.kt.cdp.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@Tag(name = "Articles", description = "Surrendered article updates and returns")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAuthority('ARTICLE_WRITE')")
    @Operation(summary = "Mark a held article as returned")
    public ResponseEntity<ArticleResponse> markReturned(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.markReturned(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ARTICLE_WRITE')")
    @Operation(summary = "Update an existing surrendered article")
    public ResponseEntity<ArticleResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.ok(articleService.update(id, request));
    }
}
