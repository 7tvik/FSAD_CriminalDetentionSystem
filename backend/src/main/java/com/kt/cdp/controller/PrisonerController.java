package com.kt.cdp.controller;

import com.kt.cdp.common.PageResponse;
import com.kt.cdp.common.enums.PrisonerStatus;
import com.kt.cdp.dto.article.ArticleRequest;
import com.kt.cdp.dto.article.ArticleResponse;
import com.kt.cdp.dto.caze.CaseRequest;
import com.kt.cdp.dto.caze.CaseResponse;
import com.kt.cdp.dto.prisoner.PrisonerHistoryResponse;
import com.kt.cdp.dto.prisoner.PrisonerRequest;
import com.kt.cdp.dto.prisoner.PrisonerResponse;
import com.kt.cdp.service.ArticleService;
import com.kt.cdp.service.CaseService;
import com.kt.cdp.service.PrisonerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prisoners")
@Tag(name = "Prisoners", description = "Prisoner master records, history and nested cases / articles")
public class PrisonerController {

    private final PrisonerService prisonerService;
    private final CaseService caseService;
    private final ArticleService articleService;

    public PrisonerController(PrisonerService prisonerService, CaseService caseService,
                              ArticleService articleService) {
        this.prisonerService = prisonerService;
        this.caseService = caseService;
        this.articleService = articleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRISONER_READ')")
    @Operation(summary = "Search prisoners with optional filters (paged)")
    public ResponseEntity<PageResponse<PrisonerResponse>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PrisonerStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate admittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate admittedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseTo,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String crimeType,
            Pageable pageable) {
        return ResponseEntity.ok(prisonerService.search(search, status, admittedFrom, admittedTo,
                releaseFrom, releaseTo, activeOnly, crimeType, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRISONER_READ')")
    @Operation(summary = "Get a prisoner by id")
    public ResponseEntity<PrisonerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(prisonerService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRISONER_WRITE')")
    @Operation(summary = "Register a new prisoner")
    public ResponseEntity<PrisonerResponse> create(@Valid @RequestBody PrisonerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prisonerService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRISONER_WRITE')")
    @Operation(summary = "Update an existing prisoner")
    public ResponseEntity<PrisonerResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody PrisonerRequest request) {
        return ResponseEntity.ok(prisonerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRISONER_DELETE')")
    @Operation(summary = "Soft-delete a prisoner (deactivate and mark INACTIVE)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        prisonerService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('PRISONER_READ')")
    @Operation(summary = "Consolidated history: cases, articles, releases and movements")
    public ResponseEntity<PrisonerHistoryResponse> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(prisonerService.getHistory(id));
    }

    @GetMapping("/{id}/cases")
    @PreAuthorize("hasAuthority('CASE_READ')")
    @Operation(summary = "List all cases for a prisoner")
    public ResponseEntity<List<CaseResponse>> getCases(@PathVariable Long id) {
        return ResponseEntity.ok(caseService.getByPrisoner(id));
    }

    @PostMapping("/{id}/cases")
    @PreAuthorize("hasAuthority('CASE_WRITE')")
    @Operation(summary = "Register a new case for a prisoner")
    public ResponseEntity<CaseResponse> addCase(@PathVariable Long id,
                                                @Valid @RequestBody CaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseService.addCaseForPrisoner(id, request));
    }

    @GetMapping("/{id}/articles")
    @PreAuthorize("hasAuthority('ARTICLE_READ')")
    @Operation(summary = "List all surrendered articles for a prisoner")
    public ResponseEntity<List<ArticleResponse>> getArticles(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getByPrisoner(id));
    }

    @PostMapping("/{id}/articles")
    @PreAuthorize("hasAuthority('ARTICLE_WRITE')")
    @Operation(summary = "Record an article surrendered by a prisoner")
    public ResponseEntity<ArticleResponse> addArticle(@PathVariable Long id,
                                                      @Valid @RequestBody ArticleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(articleService.addForPrisoner(id, request));
    }
}
