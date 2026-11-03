# MASTER CLAUDE CODE PROMPT — KT_CDP_019

You are a senior software architect, backend engineer, frontend engineer, database designer, security engineer, QA engineer, and technical project manager.

I want you to build a complete, production-quality academic project based on the following approved project specification.

> **IMPORTANT:** Do not treat this as a simple CRUD college project. Build a professional, modular, secure, maintainable system while ensuring that **EVERY ORIGINAL REQUIREMENT** is implemented.

---

## 1. PROJECT INFORMATION

**Project ID:** `KT_CDP_019`

**Project Name:** `Criminal Detention Portal`

### Original Project Description

> "This project is aimed at developing a Criminal Detention Portal that is a collection of registers and reports for the effective management of prisons. Besides this police and government officials can see crime/criminals reports for their purpose."

### Original Functional Requirements

1. **CASE REGISTER**
   - Details of the prisoner and his/her crime details should be captured.
   - The list of articles surrendered by the prisoner during nominal roll should be recorded.

2. **AUTOMATED RELEASE DIARY GENERATOR**
   - Display the list of prisoners to be released on a day, in the week, or any given duration of time.

3. **DUTY REGISTER**
   - Record who was where, why and when.

4. **INTERVIEW REQUESTS**
   - All interview requests by relatives of prisoners need to be recorded and tracked.

5. **IN-OUT REGISTER**
   - Track all prisoners and others who move in and out for various reasons.
   - Include provisions for recording prisoners sent to courts for hearings.

6. **ADMINISTRATOR**
   - Administrator will take care of the entire system.

7. **REPORTS**
   - Generate reports on prisoner release day-wise/monthly-wise.

### Original Modules

- Login and Registration
- Prisoner Details
  - Personal Details
  - Crime Details
- Release Generator
- In-Out Register
- Reports on Prisoner Release
  - Day-wise
  - Monthly-wise

### Allowed Technology Stack

**Backend:** Spring Boot / Spring MVC  
**Database:** MySQL  
**Frontend:** ReactJS  
**Persistence:** Spring Data JPA / Hibernate  
**Alternative:** Spring Boot/Spring MVC + ORM + JSP/Thymeleaf

---

# 2. PRIMARY TECHNOLOGY DECISION

Use the following stack unless there is a very strong technical reason not to.

### Frontend
- ReactJS
- Vite
- TypeScript
- React Router
- Axios
- Component-based architecture

### Backend
- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Spring Security
- JWT authentication
- Bean Validation
- Maven

### Database
- MySQL 8+

### API
- RESTful APIs
- JSON

### Documentation
- OpenAPI / Swagger

### Testing
- JUnit
- Mockito
- Spring Boot Test
- Integration testing
- Frontend testing where appropriate

**Do NOT switch to MongoDB.**  
**Do NOT switch the backend to Node.js.**  
**Do NOT replace Spring Boot with another framework.**  
**Do NOT unnecessarily introduce microservices.**

Use a **clean modular monolith architecture**.

---

# 3. PRIMARY OBJECTIVE

Build a complete Criminal Detention Portal that manages:

- Prisoners
- Prisoner personal information
- Crime/case information
- Surrendered articles
- Release records
- Automated release diary
- Staff duty records
- Prisoner and staff movement
- Court movements
- Interview requests
- Reports
- Dashboard
- Users
- Roles and permissions
- Notifications/alerts
- Audit logs
- Search and filtering

The system should feel like a professional administrative application rather than a basic CRUD application.

---

# 4. DEVELOPMENT RULE

Before writing significant code:

1. Analyze all requirements.
2. Convert them into functional and non-functional requirements.
3. Identify entities and relationships.
4. Design the database.
5. Design REST APIs.
6. Design frontend pages.
7. Design authorization rules.
8. Design business rules.
9. Identify edge cases.
10. Create an implementation plan.
11. Validate the plan against every original requirement.
12. Then implement.

Do not blindly start coding.

If something is ambiguous:
- Choose a sensible professional implementation.
- Document the assumption.
- Do not block development unnecessarily.
- Keep the implementation configurable where possible.

After planning, continue implementing the complete project. Do not stop at the planning stage.

---

# 5. SYSTEM ARCHITECTURE

Use this general architecture:

```text
React Frontend
       |
       | REST / JSON
       v
Spring Boot REST API
       |
       +-- Controllers
       |
       +-- Services
       |
       +-- Business Rules
       |
       +-- Security
       |
       +-- Repositories
       |
       v
Spring Data JPA / Hibernate
       |
       v
MySQL
```

Use clear separation between:

- Controller
- Service
- Repository
- Entity
- DTO
- Mapper
- Validation
- Exception Handling
- Security
- Audit
- Notification

Do not expose JPA entities directly from APIs where DTOs are more appropriate.

---

# 6. USER ROLES

Implement proper role-based access control.

Minimum roles:

1. `ADMINISTRATOR`
2. `PRISON_OFFICER`
3. `POLICE_OFFICIAL`
4. `GOVERNMENT_OFFICIAL`
5. `AUDITOR`

## Administrator

Full access to:

- Users
- Prisoners
- Cases
- Releases
- Movements
- Interview requests
- Duty register
- Reports
- Audit logs
- System configuration

## Prison Officer

Can:

- View/manage prisoners according to permissions
- Manage movements
- Manage duty records
- Process interview requests
- Manage court movements
- View release information

## Police Official

Can:

- View authorized crime/case information
- Search authorized prisoner/case records
- View relevant reports

Must not modify unauthorized prison operational data.

## Government Official

Read-only access to:

- Authorized reports
- Statistics
- Dashboard information

## Auditor

Read-only access to:

- Audit logs
- Historical records
- Compliance/reporting information

**Critical:** Enforce authorization on the **backend**. Never rely only on hiding frontend buttons.

---

# 7. AUTHENTICATION & SECURITY

Implement:

- User registration
- Login
- Logout
- JWT authentication
- BCrypt password hashing
- Role-based authorization
- User activation/deactivation
- Password change
- Authentication error handling
- Access-denied handling
- Secure password requirements

Never store plaintext passwords.

Never hardcode:

- Passwords
- JWT secrets
- Database credentials
- API secrets

Use environment variables.

Provide `.env.example` or equivalent configuration examples without real secrets.

---

# 8. MODULE 1 — USER & AUTHENTICATION

Features:

- Registration
- Login
- Logout
- User profile
- Role management
- User activation/deactivation
- Password change
- Protected routes
- Authentication errors
- Unauthorized/access-denied handling

Frontend pages:

- Login
- Registration
- Profile
- User Management
- Role/Permission Management

---

# 9. MODULE 2 — PRISONER MANAGEMENT

Create a complete prisoner management module.

Appropriate fields may include:

- Prisoner ID
- Prisoner number
- First name
- Last name
- Date of birth
- Gender
- Address
- Emergency contact
- Identification information
- Admission date
- Current status
- Facility
- Ward
- Cell
- Expected release date
- Photograph where appropriate

Features:

- Create prisoner
- View prisoner
- Update prisoner
- Search prisoner
- Filter prisoner
- View complete prisoner profile
- View prisoner history

Validation:

- Required fields
- Valid dates
- Release date cannot incorrectly precede admission date
- Unique prisoner number
- Valid status transitions

Prefer soft deletion/deactivation rather than destructive deletion where appropriate.

---

# 10. MODULE 3 — CASE / CRIME REGISTER

A prisoner may have multiple cases.

Use:

```text
Prisoner
   |
   +-- Case 1
   +-- Case 2
   +-- Case 3
```

Case fields may include:

- Case ID
- Case number
- FIR number
- Crime type
- Crime description
- Police station
- Court
- Applicable sections
- Arrest date
- Case status
- Investigating officer
- Important dates
- Remarks

Features:

- Add case
- Edit case
- View case
- Search case
- Filter case
- Case history
- Multiple cases per prisoner

Do not assume one prisoner equals one case.

---

# 11. MODULE 4 — SURRENDERED ARTICLES / PROPERTY

During prisoner intake/nominal roll, surrendered articles must be recorded.

Fields:

- Article ID
- Prisoner
- Article name
- Category
- Quantity
- Description
- Condition
- Received date
- Returned date
- Return status
- Remarks

Features:

- Add article
- View articles
- Update article
- Mark article as returned
- Maintain history

Do not silently delete property records.

---

# 12. MODULE 5 — RELEASE MANAGEMENT

Implement release information properly.

Fields may include:

- Release ID
- Prisoner
- Expected release date
- Release type
- Release reason
- Status
- Remarks
- Authorized by
- Actual release date

Suggested statuses:

- `UPCOMING`
- `READY`
- `RELEASED`
- `CANCELLED`
- `ON_HOLD`

Implement sensible status-transition validation.

---

# 13. MODULE 6 — AUTOMATED RELEASE DIARY GENERATOR

This is a **CORE REQUIREMENT**.

The system must automatically generate release lists for:

1. Specific day
2. Week
3. Custom date range

Example:

```text
From: 2026-09-15
To:   2026-09-21
```

Return all eligible prisoners whose release date falls within the selected period.

Display:

- Prisoner ID
- Prisoner name
- Case information
- Release date
- Release type
- Status

The report must be derived automatically from stored database data.

Provide:

- Daily release diary
- Weekly release diary
- Custom range release diary

Add:

- Sorting
- Filtering
- Print
- PDF export where practical
- Excel/CSV export where practical

Handle dates and time zones correctly.

---

# 14. MODULE 7 — DUTY REGISTER

Implement the requirement:

> "Who was where, why and when."

Duty record:

- Duty ID
- Staff member
- Location
- Purpose
- Date
- Start time
- End time
- Remarks
- Created by
- Created timestamp

Features:

- Add duty
- View duty
- Search duty
- Filter by staff
- Filter by date
- Filter by location
- View history

Validate overlapping or invalid duty periods where appropriate.

---

# 15. MODULE 8 — INTERVIEW REQUEST MANAGEMENT

Implement interview requests made by relatives/authorized visitors.

Fields:

- Request ID
- Prisoner
- Visitor name
- Relationship
- Contact information
- Identification details where appropriate
- Requested date
- Requested time
- Purpose
- Status
- Reviewed by
- Review date
- Remarks

Suggested statuses:

- `PENDING`
- `APPROVED`
- `REJECTED`
- `SCHEDULED`
- `COMPLETED`
- `CANCELLED`

Workflow:

```text
REQUESTED
   ↓
PENDING
   ↓
APPROVED / REJECTED
   ↓
SCHEDULED
   ↓
COMPLETED
```

Prevent invalid state transitions.

Provide filters by:

- Status
- Prisoner
- Date
- Visitor

---

# 16. MODULE 9 — IN-OUT / MOVEMENT REGISTER

Track movement of:

- Prisoners
- Staff
- Authorized visitors/others where applicable

Movement fields:

- Movement ID
- Person type
- Person reference
- Movement type
- Destination
- Reason
- Date
- Time out
- Expected return
- Actual return
- Escort officer where applicable
- Status
- Remarks

Movement types may include:

- `COURT`
- `MEDICAL`
- `TRANSFER`
- `AUTHORIZED_VISIT`
- `OTHER`

Statuses:

- `SCHEDULED`
- `OUT`
- `RETURNED`
- `CANCELLED`
- `OVERDUE`

---

# 17. COURT MOVEMENT

Court movement is explicitly required.

Implement:

```text
Scheduled
    ↓
Departed Prison
    ↓
At Court
    ↓
Hearing Completed
    ↓
Departed Court
    ↓
Returned to Prison
```

Store:

- Prisoner
- Court
- Case
- Hearing date
- Destination
- Departure time
- Expected return
- Actual return
- Escort officer
- Status
- Remarks

The system must identify prisoners who have not been marked as returned.

---

# 18. MODULE 10 — REPORTS

Minimum required reports:

1. Day-wise prisoner release report
2. Monthly prisoner release report

Also implement useful additional reports:

- Upcoming releases
- Prisoner statistics
- Case statistics
- Movement report
- Court movement report
- Interview request report
- Duty register report
- Surrendered article report

Allow:

- Filtering
- Sorting
- Date ranges
- Print
- PDF export
- Excel/CSV export where practical

Reports must use real database data.

Never hardcode report results.

---

# 19. MODULE 11 — DASHBOARD

Create a professional administrative dashboard.

KPIs:

- Total prisoners
- Active prisoners
- Releases this month
- Releases this week
- Today's releases
- Upcoming releases
- Pending interview requests
- Today's court movements
- Currently out prisoners
- Overdue movements
- Active cases

Include useful charts.

Dashboard must respect user roles.

---

# 20. MODULE 12 — SEARCH & FILTERING

Implement advanced search.

## Prisoner search

- Prisoner ID
- Name
- Case number
- Crime type
- Admission date
- Release date
- Status

## Case search

- Case number
- FIR number
- Crime type
- Court
- Case status

## Movement search

- Prisoner
- Movement type
- Date
- Status

## Interview search

- Prisoner
- Visitor
- Date
- Status

Use pagination for large datasets.

Do not unnecessarily load thousands of records into the browser.

---

# 21. MODULE 13 — NOTIFICATIONS / ALERTS

Implement useful alerts.

Examples:

- Release scheduled tomorrow
- Court hearing approaching
- Pending interview request
- Prisoner still out after expected return
- Overdue movement
- Important administrative action

Notifications should be persisted when appropriate.

Avoid excessive/noisy notifications.

---

# 22. MODULE 14 — AUDIT LOGGING

Because this system handles sensitive records, implement an audit trail.

Track important actions:

- Login
- Logout
- Create
- Update
- Status change
- Release modification
- Movement creation
- Interview approval/rejection
- User changes
- Permission changes

Audit fields:

- Audit ID
- User
- Action
- Entity
- Entity ID
- Old value where appropriate
- New value where appropriate
- Timestamp
- IP/request metadata where appropriate

Audit records should not be casually editable/deletable.

---

# 23. DATABASE DESIGN

Use **MySQL 8+**.

Design a normalized relational schema.

At minimum consider:

```text
users
roles
permissions
user_roles
role_permissions

prisoners
cases
surrendered_articles
release_records
staff
duty_register
interview_requests
movements
court_hearings
notifications
audit_logs
```

Add supporting tables only when genuinely necessary.

Use:

- Primary keys
- Foreign keys
- Unique constraints
- Indexes
- Appropriate nullable/non-nullable fields
- Created timestamps
- Updated timestamps

Maintain referential integrity.

Use appropriate:

- One-to-many
- Many-to-one
- Many-to-many

Do not over-normalize or create unnecessary tables merely to make the system appear complex.

---

# 24. DATABASE SECURITY

Use:

- Parameterized queries through JPA
- Validation
- Authorization
- Password hashing
- No secrets in source code
- No production credentials in Git

Use environment variables.

---

# 25. REST API DESIGN

Use consistent RESTful APIs.

Possible examples:

```text
POST   /api/auth/register
POST   /api/auth/login

GET    /api/prisoners
POST   /api/prisoners
GET    /api/prisoners/{id}
PUT    /api/prisoners/{id}

GET    /api/prisoners/{id}/cases
POST   /api/prisoners/{id}/cases

GET    /api/releases
GET    /api/releases/diary

GET    /api/movements
POST   /api/movements
PUT    /api/movements/{id}/return

GET    /api/interviews
POST   /api/interviews
PUT    /api/interviews/{id}/approve
PUT    /api/interviews/{id}/reject

GET    /api/duties

GET    /api/reports/releases/daily
GET    /api/reports/releases/monthly
```

You may improve the API structure if there is a strong reason.

Requirements:

- DTOs
- Correct HTTP status codes
- Consistent response structure
- Clear validation errors
- Pagination
- Filtering
- Sorting where appropriate

---

# 26. VALIDATION

Implement validation at:

1. API boundary
2. Service/business layer
3. Database where appropriate

Examples:

- Required prisoner name
- Valid dates
- Unique prisoner number
- Valid release date
- Valid case status
- Valid movement status
- Valid interview status
- Valid status transitions
- Prevent duplicate records where appropriate

Frontend must show useful validation messages.

---

# 27. GLOBAL ERROR HANDLING

Implement centralized Spring Boot exception handling.

Handle:

- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- Validation/business rule errors
- 500 Internal Server Error

Do not expose stack traces to users.

Log useful technical information server-side.

---

# 28. FRONTEND REQUIREMENTS

Build a modern professional React application.

Use:

- React
- TypeScript
- React Router
- Axios
- Reusable components
- Forms
- Validation
- Tables
- Filters
- Pagination
- Modals where appropriate
- Toast/notification feedback
- Loading states
- Empty states
- Error states
- Responsive design

Pages should include:

1. Login
2. Registration
3. Dashboard
4. Prisoners
5. Prisoner Details
6. Cases
7. Surrendered Articles
8. Releases
9. Release Diary
10. Duty Register
11. Interview Requests
12. Movement Register
13. Court Movements
14. Reports
15. Notifications
16. Users
17. Audit Logs
18. Profile

Use a consistent design system.

Do not make every page visually different.

---

# 29. UI/UX QUALITY

The application should look like a professional enterprise administrative dashboard.

Requirements:

- Clean navigation
- Sidebar
- Header
- Breadcrumbs
- Cards
- Tables
- Search
- Filters
- Pagination
- Status badges
- Confirmation dialogs
- Form validation
- Responsive layout
- Loading states
- Empty states
- Error states

Avoid excessive animations.

Do not use:

- Lorem ipsum
- Fake unfinished sections
- "Coming soon" for core requirements
- Placeholder content in final core screens

---

# 30. DATA PRIVACY & ETHICAL DESIGN

This system handles sensitive criminal justice information.

Therefore:

- Use fictional/demo data only.
- Do not use real prisoner data.
- Avoid exposing unnecessary personal information.
- Apply role-based access.
- Protect sensitive fields.
- Maintain audit trails.
- Do not implement automated decisions about guilt, sentencing, or criminal propensity.

Any intelligent functionality must be decision-support only.

---

# 31. OPTIONAL ADVANCED FEATURES

Only after all core requirements are complete, consider:

1. Smart release alerts
2. Overdue movement detection
3. Advanced analytics
4. Court hearing reminders
5. Document upload
6. OCR-based extraction from fictional/demo legal documents
7. Document summarization
8. Advanced report builder
9. QR-based visitor appointment/check-in
10. Dashboard analytics
11. Configurable approval workflows

Do not let advanced features break or delay core functionality.

**Core requirements always have priority.**

---

# 32. NO FAKE AI

Do not add AI simply for marketing.

Never create fake "AI" buttons returning hardcoded results.

If AI is implemented:

- It must perform a genuinely useful task.
- It must be explainable.
- It must use mock/demo data where necessary.
- It must not make decisions about guilt or punishment.
- The project must continue functioning if the AI component is unavailable.

---

# 33. SEED DATA

Create realistic fictional seed/demo data.

Include enough data to demonstrate:

- Multiple prisoners
- Multiple cases per prisoner
- Different crime types
- Different release dates
- Multiple movements
- Court movements
- Interview requests in different statuses
- Duty records
- Surrendered articles
- Multiple users/roles

Use completely fictional identities.

Do not use real criminal records.

Create demo accounts only for local development.

Clearly document credentials in the README.

---

# 34. TESTING REQUIREMENTS

Do not consider the project complete without testing.

## Backend

Implement:

- Unit tests
- Service tests
- Controller tests
- Repository tests where useful
- Security tests
- Validation tests
- Integration tests

Critical scenarios:

1. User login
2. Unauthorized access
3. Role-based access
4. Create prisoner
5. Duplicate prisoner number
6. Add multiple cases
7. Add surrendered article
8. Release diary generation
9. Daily release report
10. Monthly release report
11. Interview approval
12. Invalid interview state transition
13. Movement creation
14. Court movement
15. Mark prisoner returned
16. Overdue movement detection
17. Audit log creation

## Frontend

Test:

- Form validation
- Routing
- Protected routes
- Important component behavior
- Error handling

---

# 35. SECURITY TESTING

Verify:

- Passwords are hashed
- JWT validation works
- Protected endpoints cannot be accessed anonymously
- Role restrictions work
- Users cannot modify unauthorized records
- Input validation works
- SQL injection is prevented through application design
- CORS is appropriately configured
- Sensitive errors are not exposed

---

# 36. LOGGING

Implement useful application logging.

Log:

- Authentication events
- Important business operations
- Errors
- Exceptions
- Important state changes

Never log:

- Passwords
- JWT secrets
- Database passwords
- Sensitive credentials

---

# 37. API DOCUMENTATION

Configure Swagger/OpenAPI.

Document:

- Authentication
- Endpoints
- Request models
- Response models
- Validation errors
- Authorization requirements

The project should be understandable through Swagger without reading the source code.

---

# 38. PROJECT STRUCTURE

Backend should have a clean structure similar to:

```text
backend/
  src/
    main/
      java/
        .../
          config/
          security/
          controller/
          service/
          repository/
          entity/
          dto/
          mapper/
          exception/
          validation/
          audit/
          notification/
```

Frontend:

```text
frontend/
  src/
    components/
    pages/
    layouts/
    services/
    hooks/
    context/
    routes/
    types/
    utils/
    validations/
```

Improve the structure if there is a strong architectural reason.

---

# 39. CONFIGURATION

Provide:

- `application.yml` or `application.properties`
- Development configuration
- Environment variable support
- Database configuration
- JWT configuration
- CORS configuration
- `.env.example`

Never hardcode secrets.

---

# 40. DATABASE SETUP

Provide:

- Database schema/migrations
- Seed data
- Migration strategy

Prefer Flyway or Liquibase if compatible.

Database setup must be reproducible.

---

# 41. DOCKER

If practical, provide Docker support for:

- MySQL
- Backend
- Frontend

At minimum provide Docker Compose for MySQL.

Do not make Docker mandatory if it unnecessarily complicates local development.

---

# 42. README

Create a professional `README.md` containing:

1. Project overview
2. Features
3. Architecture
4. Technology stack
5. Requirements
6. Installation
7. Database setup
8. Environment variables
9. Running backend
10. Running frontend
11. Demo credentials
12. API documentation
13. Database structure
14. Testing
15. Project structure
16. Screenshots section
17. Known limitations
18. Future enhancements

---

# 43. ACADEMIC DOCUMENTATION

Create documentation suitable for academic submission.

Include:

- Abstract
- Problem statement
- Objectives
- Existing system
- Proposed system
- Scope
- Functional requirements
- Non-functional requirements
- User roles
- Use cases
- System architecture
- ER diagram description
- Database schema
- Module descriptions
- API documentation
- Security
- Testing strategy
- Future scope

Do not fabricate results or claims.

---

# 44. REQUIREMENTS TRACEABILITY MATRIX

Create `REQUIREMENTS.md`.

Use a table like:

| Requirement | Implementation | Module | API | Frontend | Test | Status |
|---|---|---|---|---|---|---|

Every original requirement must be traceable.

Example:

```text
Case Register
→ Prisoner Module + Case Module + Property Module

Release Diary
→ Release Management + Release Diary

Duty Register
→ Duty Module

Interview Requests
→ Interview Module

In-Out Register
→ Movement Module

Administrator
→ RBAC + Admin Module

Reports
→ Reporting Module
```

At the end, verify that **NO ORIGINAL REQUIREMENT IS MISSING**.

---

# 45. EDGE CASES

Handle:

- Prisoner has multiple cases
- Prisoner has no active case
- Prisoner has multiple surrendered articles
- Article already returned
- Release date changed
- Release cancelled
- Release put on hold
- Interview rejected
- Interview cancelled
- Duplicate interview request
- Prisoner sent to court
- Prisoner does not return on expected time
- Movement cancelled
- Staff has overlapping duties
- Unauthorized user attempts modification
- Invalid date range
- No releases found
- Large number of prisoners
- Deactivated user owns historical records

The application must not crash in these scenarios.

---

# 46. PERFORMANCE

Use:

- Pagination
- Database indexes
- Efficient queries
- Lazy loading where appropriate
- DTO projections where useful
- Avoid N+1 queries
- Avoid loading unnecessary relationships

Do not optimize prematurely, but do not write obviously inefficient queries.

---

# 47. DATA INTEGRITY

Use database constraints and service-layer validation.

Critical operations should use transactions where appropriate.

Examples:

- Release status updates
- Movement return
- Interview approval
- Property return

---

# 48. IMPLEMENTATION WORKFLOW

Follow this order.

## PHASE 1
Analyze requirements.

## PHASE 2
Create architecture and design.

## PHASE 3
Create database schema/migrations.

## PHASE 4
Implement backend foundation:

- Security
- Users
- Roles
- Common infrastructure
- Exception handling
- Validation

## PHASE 5
Implement:

- Prisoners
- Cases
- Surrendered Articles

## PHASE 6
Implement:

- Releases
- Release Diary

## PHASE 7
Implement:

- Duty Register
- Movement Register
- Court Movement

## PHASE 8
Implement:

- Interview Requests

## PHASE 9
Implement:

- Reports
- Dashboard
- Notifications
- Audit Logs

## PHASE 10
Build complete React frontend.

## PHASE 11
Connect frontend/backend.

## PHASE 12
Add advanced features.

## PHASE 13
Testing.

## PHASE 14
Security review.

## PHASE 15
Performance review.

## PHASE 16
Documentation.

## PHASE 17
Final requirement verification.

---

# 49. CRITICAL — VERIFY YOUR OWN WORK

After implementation, do NOT simply say "project completed."

Perform complete verification.

Check:

- Backend compiles
- Frontend compiles
- Application starts
- MySQL connection works
- Migrations work
- Seed data loads
- Login works
- JWT works
- Role authorization works
- Every major API works
- Every major frontend page works
- Reports work
- Release diary works
- Movement tracking works
- Interview workflow works
- Audit logging works
- Tests pass
- No important console errors
- No broken routes
- No missing requirements

Fix all issues found.

If you encounter an error:

1. Diagnose root cause.
2. Fix it.
3. Re-run the relevant test/build.
4. Continue until stable.

Do not claim something works unless you actually verified it.

---

# 50. NO PLACEHOLDERS

Do not leave core functionality as:

- TODO
- FIXME
- Coming soon
- Not implemented
- Placeholder API
- Fake response
- Hardcoded report data

Core functionality must be real.

Optional future features may be documented as future scope, but must not be presented as implemented.

---

# 51. CODE QUALITY

Write clean, maintainable code.

Avoid:

- Giant classes
- Giant React components
- Duplicate code
- Hardcoded business rules
- Magic numbers
- Unnecessary abstractions
- Unused dependencies
- Dead code
- Console spam
- Poor naming

Use meaningful names.

Add comments only where they explain non-obvious business logic.

---

# 52. PROJECT SCOPE RULE

Do not turn this into an uncontrolled massive enterprise system.

Priority order:

1. **100% original requirements**
2. Correct functionality
3. Security
4. Clean architecture
5. Professional UI
6. Testing
7. Advanced features

Do not sacrifice core functionality for unnecessary features.

---

# 53. FINAL DELIVERABLE

The final project must contain:

1. Fully working Spring Boot backend
2. Fully working React frontend
3. MySQL database
4. Database migrations
5. Seed/demo data
6. Authentication
7. RBAC
8. All original modules
9. Release diary generator
10. Movement/court tracking
11. Interview workflow
12. Reports
13. Dashboard
14. Notifications
15. Audit logs
16. API documentation
17. Automated tests
18. README
19. Requirements traceability
20. Academic documentation

---

# 54. FINAL PROJECT REVIEW

Create `FINAL_PROJECT_REVIEW.md` containing:

1. Implemented features
2. Original requirements mapping
3. Database tables
4. API count/list
5. Frontend pages
6. Roles and permissions
7. Security mechanisms
8. Test results
9. Known limitations
10. Future enhancements
11. How to run the project
12. Demo flow
13. Viva questions and answers

---

# 55. FINAL INSTRUCTION

Your goal is NOT merely to generate code.

Your goal is to deliver a complete, working, professional-quality:

> **Criminal Detention Portal — KT_CDP_019**

that:

- satisfies every approved requirement,
- has a strong database design,
- has proper authentication and authorization,
- has professional UI/UX,
- has real business workflows,
- has reporting and analytics,
- has auditability,
- is thoroughly tested,
- is easy to run,
- and is suitable for academic demonstration and evaluation.

Before implementation, show:

1. Proposed architecture
2. Database ER design
3. Database schema
4. Module breakdown
5. User roles
6. API specification
7. Frontend page structure
8. Business rules
9. Implementation plan
10. Requirement traceability

Then proceed with implementation without waiting for unnecessary confirmation.

If a project already exists:
- Inspect it first.
- Understand the existing implementation.
- Preserve useful working code.
- Fix issues instead of unnecessarily rewriting everything.

If no project exists:
- Initialize the project according to the architecture above.

**START NOW.**
