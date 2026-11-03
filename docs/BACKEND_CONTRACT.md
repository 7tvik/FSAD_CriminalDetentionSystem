# Backend Domain Contract — KT_CDP_019

All shared foundation classes ALREADY EXIST and compile. Domain agents must ONLY
add new files (DTOs, mappers, services, controllers) and must NOT modify foundation
files, entities, repositories, or enums. Follow these patterns exactly.

## Package root: `com.kt.cdp`
- Backend module dir: `/Users/I578448/Documents/personal projects/criminal/backend`
- Java 21, Spring Boot 3.3.4, Lombok available, jakarta.* imports, jakarta.validation.

## Existing enums (`com.kt.cdp.common.enums`)
- PrisonerStatus: ACTIVE, RELEASED, TRANSFERRED, DECEASED, INACTIVE
- CaseStatus: UNDER_INVESTIGATION, UNDER_TRIAL, CONVICTED, ACQUITTED, CLOSED
- ArticleReturnStatus: HELD, RETURNED
- ReleaseType: BAIL, SENTENCE_COMPLETE, ACQUITTAL, PAROLE, OTHER
- ReleaseStatus: UPCOMING, READY, RELEASED, CANCELLED, ON_HOLD
- InterviewStatus: PENDING, APPROVED, REJECTED, SCHEDULED, COMPLETED, CANCELLED
- PersonType: PRISONER, STAFF, OTHER
- MovementType: COURT, MEDICAL, TRANSFER, AUTHORIZED_VISIT, OTHER
- MovementStatus: SCHEDULED, OUT, RETURNED, CANCELLED  (OVERDUE is DERIVED, never stored)
- CourtHearingStatus: SCHEDULED, DEPARTED, AT_COURT, HEARING_COMPLETED, DEPARTED_COURT, RETURNED, CANCELLED
- NotificationType: RELEASE_UPCOMING, MOVEMENT_OVERDUE, INTERVIEW_PENDING, COURT_HEARING, GENERAL

## Existing entities (`com.kt.cdp.entity`) — all extend BaseEntity (Long id, createdAt, updatedAt) unless noted
- User(username,email,passwordHash,fullName,boolean active,Set<Role> roles)
- Role(Long id,name,description,Set<Permission> permissions) [NOT BaseEntity]
- Permission(Long id,name,description) [NOT BaseEntity]
- Staff(User user,staffNumber,fullName,rankTitle,contact,boolean active)
- Prisoner(prisonerNumber,firstName,lastName,LocalDate dateOfBirth,gender,address,emergencyContact,idType,idNumber,LocalDate admissionDate,PrisonerStatus status,facility,ward,cell,LocalDate expectedReleaseDate,photoUrl,boolean active)
- CaseRecord(Prisoner prisoner,caseNumber,firNumber,crimeType,crimeDescription,policeStation,court,sections,LocalDate arrestDate,CaseStatus status,investigatingOfficer,remarks)
- SurrenderedArticle(Prisoner prisoner,name,category,int quantity,description,conditionNote,LocalDate receivedDate,LocalDate returnedDate,ArticleReturnStatus returnStatus,remarks)
- ReleaseRecord(Prisoner prisoner,LocalDate expectedReleaseDate,ReleaseType releaseType,reason,ReleaseStatus status,remarks,User authorizedBy,LocalDate actualReleaseDate)
- DutyRecord(Staff staff,location,purpose,LocalDate dutyDate,LocalTime startTime,LocalTime endTime,remarks,User createdBy)
- InterviewRequest(Prisoner prisoner,visitorName,relationship,contact,idDetails,LocalDate requestedDate,LocalTime requestedTime,purpose,InterviewStatus status,User reviewedBy,LocalDateTime reviewDate,remarks)
- Movement(PersonType personType,Long personRefId,personName,MovementType movementType,destination,reason,LocalDate movementDate,LocalDateTime timeOut,LocalDateTime expectedReturn,LocalDateTime actualReturn,Staff escortOfficer,MovementStatus status,remarks)
- CourtHearing(Movement movement,Prisoner prisoner,CaseRecord caseRecord,court,LocalDate hearingDate,destination,LocalDateTime departureTime,LocalDateTime expectedReturn,LocalDateTime actualReturn,Staff escortOfficer,CourtHearingStatus status,outcome,remarks)
- Notification(Long id,Long recipientUserId,NotificationType type,title,message,entityType,Long entityId,boolean readFlag,LocalDateTime createdAt) [own id + createdAt]
- AuditLog [own id + createdAt] — DO NOT expose mutation endpoints

## Existing repositories (`com.kt.cdp.repository`) — all extend JpaRepository; searchable ones also JpaSpecificationExecutor
UserRepository, RoleRepository, PermissionRepository, StaffRepository,
PrisonerRepository (Spec; existsByPrisonerNumber, countByActiveTrue, countByStatusAndActiveTrue),
CaseRepository (Spec; findByPrisonerId, existsByCaseNumber, countByStatus),
SurrenderedArticleRepository (findByPrisonerId),
ReleaseRecordRepository (findByPrisonerId; findDiary(from,to,Pageable); countByExpectedReleaseDateBetweenAndStatusNot; findByExpectedReleaseDateAndStatusNot; findByActualReleaseDateBetween),
DutyRecordRepository (Spec; findByStaffIdAndDutyDate),
InterviewRequestRepository (Spec; countByStatus; existsByPrisonerIdAndVisitorNameIgnoreCaseAndRequestedDateAndStatusIn),
MovementRepository (Spec; countByStatus; countByMovementTypeAndMovementDate; findOverdue(now)),
CourtHearingRepository (Spec; countByHearingDate),
NotificationRepository (findVisibleToUser(userId,Pageable); existsByTypeAndEntityIdAndTitle),
AuditLogRepository (Spec)

## Foundation utilities
- `com.kt.cdp.common.PageResponse<T>` — record; use `PageResponse.of(springPage)`.
- `com.kt.cdp.common.Permissions` — String constants (PRISONER_READ, PRISONER_WRITE, PRISONER_DELETE, CASE_READ, CASE_WRITE, ARTICLE_READ, ARTICLE_WRITE, RELEASE_READ, RELEASE_WRITE, DUTY_READ, DUTY_WRITE, INTERVIEW_READ, INTERVIEW_WRITE, INTERVIEW_REVIEW, MOVEMENT_READ, MOVEMENT_WRITE, COURT_READ, COURT_WRITE, REPORT_READ, DASHBOARD_READ, NOTIFICATION_READ, USER_READ, USER_WRITE, ROLE_READ, ROLE_WRITE, AUDIT_READ).
- Exceptions: `ResourceNotFoundException` (404; `.of("Prisoner", id)`), `ConflictException` (409), `BusinessRuleException` (422). GlobalExceptionHandler already maps these.
- `com.kt.cdp.audit.AuditService` — `record(action, entityType, entityId)` and `record(action, entityType, entityId, oldValue, newValue)`.
- `com.kt.cdp.security.SecurityUtils` — `currentUserId()`, `currentUsername()`, `currentUser()` (Optional).

## Required patterns
1. Controllers: `@RestController @RequestMapping("/api/...")`, constructor injection, `@Valid @RequestBody`, `@Tag`/`@Operation` swagger annotations. Guard EVERY method with `@PreAuthorize("hasAuthority('" + Permissions.X + "')")` — use the string literal form e.g. `@PreAuthorize("hasAuthority('PRISONER_WRITE')")`.
2. Request DTOs are Java records with jakarta.validation annotations. Response DTOs are Java records.
3. Mappers are `@Component` classes (or static methods) converting entity<->DTO. Never return entities from controllers.
4. Services are `@Service`, `@Transactional` on mutations, `@Transactional(readOnly=true)` on reads. Enforce business rules & state-machine transitions here (throw BusinessRuleException on invalid transition). Call auditService.record(...) on sensitive mutations.
5. List endpoints: accept `org.springframework.data.domain.Pageable` (Spring resolves ?page&size&sort) and return `PageResponse<Dto>`.
6. Use `@RequiredArgsConstructor` (Lombok) OR explicit constructors — be consistent within a file.
7. Search endpoints: build `org.springframework.data.jpa.domain.Specification` from optional query params.

## Verify
Run `mvn -q -o compile` in the backend dir (offline works now; deps cached). Fix all errors. Do NOT run the full app (needs MySQL). Report the list of files you created and confirm compilation.
