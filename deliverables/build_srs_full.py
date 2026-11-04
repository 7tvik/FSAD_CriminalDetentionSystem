#!/usr/bin/env python3
"""Build a full IEEE-830-style SRS for KT_CDP_019 Criminal Detention Portal,
mirroring the structure of the DBSA E-Commerce reference SRS."""
import os
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.section import WD_SECTION
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

HERE = os.path.dirname(os.path.abspath(__file__))
DIAG = os.path.join(HERE, "diagrams")
SCHEMA = os.path.join(HERE, "..", "backend", "src", "main", "resources", "db", "migration", "V1__schema.sql")
NAVY = RGBColor(0x1F, 0x4E, 0x79)
STEEL = RGBColor(0x2E, 0x5E, 0x8C)
GREY = RGBColor(0x80, 0x80, 0x80)
CODEBG = "F2F4F7"

doc = Document()


def style_base():
    n = doc.styles["Normal"]
    n.font.name = "Calibri"; n.font.size = Pt(10.5)
    for i, sz in [(1, 17), (2, 13.5), (3, 11.5)]:
        h = doc.styles[f"Heading {i}"]
        h.font.name = "Calibri"; h.font.size = Pt(sz)
        h.font.color.rgb = NAVY; h.font.bold = True


def set_cell_bg(cell, hexc):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd"); shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto"); shd.set(qn("w:fill"), hexc)
    tcPr.append(shd)


def h1(t): doc.add_heading(t, 1)
def h2(t): doc.add_heading(t, 2)
def h3(t): doc.add_heading(t, 3)
def para(t): return doc.add_paragraph(t)
def bullet(t): return doc.add_paragraph(t, style="List Bullet")


def table(headers, rows, widths=None, fs=9):
    t = doc.add_table(rows=1, cols=len(headers))
    t.style = "Light Grid Accent 1"; t.alignment = WD_TABLE_ALIGNMENT.CENTER
    for i, hn in enumerate(headers):
        c = t.rows[0].cells[i]; c.text = ""
        r = c.paragraphs[0].add_run(hn); r.font.bold = True
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF); r.font.size = Pt(fs + 0.5)
        set_cell_bg(c, "1F4E79")
    for row in rows:
        cells = t.add_row().cells
        for i, v in enumerate(row):
            cells[i].text = ""
            r = cells[i].paragraphs[0].add_run(str(v)); r.font.size = Pt(fs)
    if widths:
        for i, w in enumerate(widths):
            for row in t.rows:
                row.cells[i].width = Inches(w)
    doc.add_paragraph()
    return t


def code_block(text, fs=8):
    t = doc.add_table(rows=1, cols=1); t.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = t.rows[0].cells[0]; set_cell_bg(cell, CODEBG)
    cell.width = Inches(6.5); cell.text = ""
    first = True
    for line in text.rstrip("\n").split("\n"):
        p = cell.paragraphs[0] if first else cell.add_paragraph()
        first = False
        p.paragraph_format.space_after = Pt(0); p.paragraph_format.space_before = Pt(0)
        r = p.add_run(line if line else " ")
        r.font.name = "Consolas"; r.font.size = Pt(fs); r.font.color.rgb = RGBColor(0x22, 0x2A, 0x33)
    doc.add_paragraph()


def figure(path, caption, width=6.2):
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run().add_picture(path, width=Inches(width))
    c = doc.add_paragraph(); c.alignment = WD_ALIGN_PARAGRAPH.CENTER
    rc = c.add_run(caption); rc.font.italic = True; rc.font.size = Pt(9); rc.font.color.rgb = GREY
    doc.add_paragraph()


style_base()

# ============================ COVER ============================
for _ in range(2): doc.add_paragraph()
for line, sz, col, bold, it in [
    ("BIRLA INSTITUTE OF TECHNOLOGY & SCIENCE, PILANI", 14, NAVY, True, False),
    ("WORK INTEGRATED LEARNING PROGRAMMES", 12, STEEL, True, False),
    ("Full Stack Application Development (FSAD)", 12, STEEL, False, True),
]:
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(line); r.font.size = Pt(sz); r.font.color.rgb = col; r.font.bold = bold; r.font.italic = it
for _ in range(2): doc.add_paragraph()
p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run("SOFTWARE REQUIREMENTS SPECIFICATION"); r.font.size = Pt(22); r.font.bold = True; r.font.color.rgb = NAVY
p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run("Criminal Detention Portal (KT_CDP_019)"); r.font.size = Pt(18); r.font.bold = True
p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run("A Prison-Administration System — Registers & Reports for the Effective Management of Prisons")
r.font.size = Pt(12); r.font.italic = True; r.font.color.rgb = STEEL
for _ in range(2): doc.add_paragraph()
t = doc.add_table(rows=0, cols=2); t.alignment = WD_TABLE_ALIGNMENT.CENTER; t.style = "Light List Accent 1"
for k, v in [
    ("Course Title", "Full Stack Application Development (FSAD)"),
    ("Project Title", "Criminal Detention Portal"),
    ("Project Code", "KT_CDP_019"),
    ("Document", "Software Requirements Specification (SRS)"),
    ("Version", "1.0"),
    ("Group / Students", "<Group No. — Student Names & IDs>"),
    ("Date", "<Submission Date>"),
]:
    cells = t.add_row().cells
    cells[0].text = k; cells[0].paragraphs[0].runs[0].font.bold = True
    cells[1].text = v
doc.add_paragraph()
p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run("Academic / demonstration project. All data is fictional. The system keeps registers and "
              "produces reports; it makes no automated decisions about guilt, sentencing, or risk.")
r.font.italic = True; r.font.size = Pt(9); r.font.color.rgb = GREY
doc.add_page_break()

# ============================ 1. INTRODUCTION ============================
h1("1. Introduction")
h2("1.1 Purpose")
para("This Software Requirements Specification (SRS) defines the functional and non-functional "
     "requirements of the Criminal Detention Portal (CDP) — an academic full-stack prison-administration "
     "system. It is the authoritative statement of what the system does and the constraints it operates "
     "under, and is intended for evaluators, developers, and testers. The document follows the IEEE 830 / "
     "ISO-IEC-IEEE 29148 requirement-specification structure.")
h2("1.2 Scope")
para("The CDP digitises the day-to-day registers a detention facility keeps and the reports derived from "
     "them, adding role-based access control, an operational dashboard, notifications, and an append-only "
     "audit trail. It is delivered as a three-tier web application: a React + TypeScript SPA, a Spring Boot "
     "(Java 21) backend, and a MySQL 8 database.")
h3("1.2.1 In Scope (implemented in this release)")
for b in [
    "Case Register — prisoners with personal/admission/status details, their cases (FIR, sections, court), and surrendered articles.",
    "Release management and an automated Release Diary (day / week / custom range) with CSV export.",
    "Duty Register with time-order and overlap validation.",
    "Visitor Interview Requests with a review lifecycle.",
    "In-Out (movement) Register, including a dedicated Court-movement register with hearing outcomes.",
    "Administration: users, 5 roles, and 26 fine-grained permissions enforced on the backend.",
    "Reports: day-wise / month-wise / upcoming releases and prisoner- & case-status statistics.",
    "Operational dashboard (KPI cards + charts), in-app notifications, and an append-only audit trail.",
    "Search and server-side pagination across every register; stateless JWT authentication.",
]:
    bullet(b)
h3("1.2.2 Out of Scope / Future Enhancements")
for b in [
    "Biometric capture, document/image storage as binary objects, and external case-management integrations.",
    "Automated risk scoring or any decision-making about guilt, sentencing, or parole.",
    "Mobile-native applications and offline operation.",
    "Multi-facility federation and real-time messaging between facilities.",
]:
    bullet(b)
h2("1.3 Definitions, Acronyms, and Abbreviations")
table(["Term", "Meaning"], [
    ("SRS", "Software Requirements Specification."),
    ("RBAC", "Role-Based Access Control — permissions grouped into roles."),
    ("JWT", "JSON Web Token — stateless bearer token (HS256) used for authentication."),
    ("BCrypt", "Adaptive password-hashing function used to store credentials."),
    ("SPA", "Single-Page Application (the React frontend)."),
    ("DTO", "Data Transfer Object — API request/response record."),
    ("JPA", "Jakarta Persistence API — ORM abstraction (Hibernate implementation)."),
    ("FIR", "First Information Report — police record of a reported offence."),
    ("CRUD", "Create, Read, Update, Delete."),
    ("CSV", "Comma-Separated Values export format."),
    ("In-Out Register", "Record of movements of prisoners/staff in and out of the facility."),
    ("Derived state", "A value computed at query time (e.g. 'overdue'), never stored."),
], widths=[1.6, 4.8])
h2("1.4 References")
for b in [
    "IEEE 830-1998 — Recommended Practice for Software Requirements Specifications.",
    "ISO/IEC/IEEE 29148:2018 — Systems and Software Engineering — Requirements Engineering.",
    "Spring Boot 3.3 and Spring Security reference documentation.",
    "React 18 and TypeScript documentation; MySQL 8 and Flyway documentation.",
    "Project design documents: ARCHITECTURE.md, DATABASE.md, API.md, REQUIREMENTS.md, TESTING.md.",
    "OpenAPI / Swagger contract exposed by the running backend at /swagger-ui/index.html.",
]:
    bullet(b)
doc.add_page_break()

# ============================ 2. OVERALL DESCRIPTION ============================
h1("2. Overall Description")
h2("2.1 Product Perspective")
para("The CDP is a self-contained, three-tier web application built as a modular monolith — a single "
     "Spring Boot deployable organised internally into feature modules (prisoners, cases, releases, duties, "
     "interviews, movements, court, admin, audit). The React SPA communicates with the backend over "
     "REST/JSON; the backend persists to MySQL through a strict Controller → Service → Repository flow. "
     "Entities never cross the controller boundary — DTOs do.")
figure(os.path.join(DIAG, "architecture.png"),
       "Figure 1 — High-level system architecture (three-tier modular monolith).", width=3.9)
h2("2.2 Product Functions")
for b in [
    "Maintain the Case Register: prisoners, their cases, and surrendered articles.",
    "Manage release records through a lifecycle and generate an automated Release Diary with CSV export.",
    "Maintain the Duty Register with overlap and time-order validation.",
    "Manage visitor Interview Requests through a review lifecycle.",
    "Maintain the In-Out Register and a dedicated Court-movement register with outcomes.",
    "Administer users, roles, and 26 permissions; allow self-service password change.",
    "Produce day/month/upcoming release reports and prisoner- & case-status statistics.",
    "Present an operational dashboard, generate notifications, and record an append-only audit trail.",
    "Provide search and server-side pagination across every register.",
]:
    bullet(b)
h2("2.3 User Roles")
table(["User Role", "Description"], [
    ("Administrator", "Full control: user & role management plus all registers, reports, and audit."),
    ("Prison Officer", "Day-to-day operations: registers, movements, releases, interviews, duties."),
    ("Police Official", "Investigation & court liaison: cases and court movements."),
    ("Government Official", "Oversight: read-only access plus reports and dashboard."),
    ("Auditor", "Compliance: read-only access plus the audit log."),
], widths=[1.8, 4.6])
h2("2.4 Constraints")
for b in [
    "Authorization must be enforced on the backend via @PreAuthorize; hiding UI controls is UX only.",
    "No secret may be hard-coded — DB credentials and the JWT secret come from environment variables.",
    "Passwords are persisted only as BCrypt hashes and are never logged.",
    "The audit trail is append-only — there is no update or delete path.",
    "Derived states (overdue movements, not-returned court hearings, the diary) are computed, never stored.",
    "Standard, portable technologies: Java 21 / Spring Boot, MySQL 8 (JDBC), React + TypeScript.",
]:
    bullet(b)
h2("2.5 Assumptions and Dependencies")
for b in [
    "A reachable MySQL 8 instance and a configured JWT secret are available at runtime.",
    "Each case, article, release, interview, and court hearing belongs to exactly one prisoner.",
    "A movement may optionally be escorted by one staff member; a court hearing specializes at most one movement (1:1).",
    "A user may optionally be linked to one staff record; roles and permissions are many-to-many.",
    "Demo users and seed data are provisioned at first startup (Flyway V2 + DataInitializer) for evaluation.",
    "The system depends on the JVM (Java 21), Node tooling for the SPA build, and Docker for the database.",
]:
    bullet(b)
h2("2.6 Data Model & Normalization Strategy")
para("The relational schema is designed to eliminate redundancy and update anomalies. Enumerated values "
     "are stored as human-readable VARCHAR strings (mapped to Java enums via JPA). The schema is normalised "
     "to Third Normal Form / BCNF throughout; many-to-many relationships are resolved with dedicated "
     "junction tables.")
table(["Normal Form", "Rule Applied", "How Achieved in This Schema"], [
    ("1NF", "Atomic values; no repeating groups.", "Every column holds one indivisible value; no multi-valued columns."),
    ("2NF", "No partial dependency on a composite key.", "Junction tables user_roles / role_permissions have composite PKs whose only attributes are the keys."),
    ("3NF", "No transitive dependencies.", "Non-key attributes depend only on their table's PK; lookups (roles, permissions, staff) are separate tables referenced by FK."),
    ("BCNF", "Every determinant is a candidate key.", "Unique business keys (prisoner_number, case_number, username, staff_number) are enforced with UNIQUE constraints."),
], widths=[1.2, 2.4, 2.8])
doc.add_page_break()

# ============================ 3. SPECIFIC REQUIREMENTS ============================
h1("3. Specific Requirements")
h2("3.1 Functional Requirements")
frs = [
    ("FR1", "The system shall register a prisoner with personal, admission, facility, and status details, assigning a unique prisoner number."),
    ("FR2", "The system shall record one or more cases per prisoner (case number, FIR, crime type, sections, court, investigating officer, status)."),
    ("FR3", "The system shall record surrendered articles taken into custody, each with quantity, received date, and a return status (HELD / RETURNED)."),
    ("FR4", "The system shall provide search, attribute filtering, and server-side pagination across the prisoner register."),
    ("FR5", "The system shall soft-delete a prisoner (status INACTIVE, active=false) rather than removing the row, preserving history."),
    ("FR6", "The system shall present a per-prisoner history/timeline aggregating cases, releases, interviews, and movements."),
    ("FR7", "The system shall manage release records through a lifecycle (UPCOMING / READY / ON_HOLD / RELEASED / CANCELLED)."),
    ("FR8", "The system shall generate an upcoming-release diary for a day, a week, or a custom date range, derived from expected release dates."),
    ("FR9", "The system shall export the release diary and the daily release report as downloadable CSV files."),
    ("FR10", "The system shall record staff duty assignments and reject non-positive durations and overlapping duties for the same staff member."),
    ("FR11", "The system shall capture visitor interview requests and enforce the review lifecycle PENDING → APPROVED/REJECTED → SCHEDULED → COMPLETED (+CANCELLED)."),
    ("FR12", "The system shall reject illegal interview transitions and duplicate active requests for the same prisoner."),
    ("FR13", "The system shall record movements in/out of the facility, created as OUT (if time-out set) or SCHEDULED."),
    ("FR14", "The system shall support return and cancel transitions on a movement and derive 'overdue' movements at query time."),
    ("FR15", "The system shall record court movements with a forward-only lifecycle (SCHEDULED → … → RETURNED) and hearing outcome, deriving 'not-returned' hearings."),
    ("FR16", "The system shall support user management: create, update, and activate/deactivate accounts."),
    ("FR17", "The system shall define 5 roles composed of 26 permissions and enforce every permission on the backend."),
    ("FR18", "The system shall allow a user to change their own password under a password policy (min length + letter + digit)."),
    ("FR19", "The system shall produce day-wise, month-wise, and upcoming-release reports."),
    ("FR20", "The system shall produce prisoner-status and case-status statistics for visualization."),
    ("FR21", "The system shall present an operational dashboard with KPI cards and charts."),
    ("FR22", "The system shall generate in-app notifications on demand for releases, overdue movements, and pending interviews."),
    ("FR23", "The system shall write an append-only audit record (actor, action, entity, before/after, IP) for every state-changing action."),
    ("FR24", "The system shall authenticate via POST /api/auth/login returning a signed JWT and re-authenticate every subsequent request."),
]
table(["ID", "Requirement Description"], frs, widths=[0.7, 5.7])
h2("3.2 Non-Functional Requirements")
table(["ID", "Category", "Requirement"], [
    ("NFR1", "Performance", "Every register list endpoint shall be server-side paginated (page, size, sort)."),
    ("NFR2", "Performance", "Columns used in search/sort/joins shall be indexed to keep queries efficient."),
    ("NFR3", "Security", "Authentication shall be stateless using HS256 JWT; there shall be no server session."),
    ("NFR4", "Security", "Passwords shall be stored only as BCrypt hashes and never logged."),
    ("NFR5", "Security", "Authorization shall be enforced server-side via @PreAuthorize and @EnableMethodSecurity."),
    ("NFR6", "Security", "Secrets (JWT key, DB credentials) shall be supplied via environment variables, never committed."),
    ("NFR7", "Security", "Error responses shall be sanitized JSON; stack traces shall never be exposed to clients."),
    ("NFR8", "Reliability", "Audit writes shall run in a REQUIRES_NEW transaction so they survive a rolled-back request."),
    ("NFR9", "Usability", "Every register shall follow one consistent list-page pattern with clear validation messages."),
    ("NFR10", "Maintainability", "The code shall be layered (Controller/Service/Repository) with DTOs at the boundary and TypeScript strict mode on the client."),
    ("NFR11", "Portability", "The database shall run in Docker; the backend shall be JVM-portable and environment-configured."),
    ("NFR12", "Auditability", "The audit log shall be append-only, with no update or delete endpoints."),
], widths=[0.7, 1.4, 4.3])
h2("3.3 Domain Requirements")
para("Business rules specific to the detention-administration domain that the system must uphold:")
for b in [
    "DR1 — Release state transitions are restricted to the legal state machine; an illegal transition is rejected with HTTP 422.",
    "DR2 — Reaching RELEASED sets the actual release date and flips the prisoner's status to RELEASED.",
    "DR3 — A duty's end time must be strictly after its start time.",
    "DR4 — Two duties for the same staff member may not overlap in time on the same date.",
    "DR5 — An interview follows a forward-only lifecycle; a prisoner may not have two active (non-terminal) requests that conflict.",
    "DR6 — A movement may be marked returned only from the OUT state; 'overdue' = OUT and past expected return (derived, never stored).",
    "DR7 — A court hearing advances forward-only SCHEDULED → DEPARTED → AT_COURT → HEARING_COMPLETED → DEPARTED_COURT → RETURNED (or SCHEDULED → CANCELLED); 'not-returned' is derived.",
    "DR8 — Prisoners are soft-deleted; audit and historical references remain intact.",
    "DR9 — Audit records are immutable: there is no API path that updates or deletes an audit row.",
    "DR10 — Derived states (overdue, not-returned, release diary) are computed at query time and never persisted.",
]:
    bullet(b)
h2("3.4 Front-End Requirements and Specifications")
para("The SPA is a responsive, role-filtered single-page application. Every register follows one standard "
     "list-page pattern (search, filters, pagination, detail/drawer, create/edit forms). Controls the user "
     "lacks permission for are hidden in the UI and additionally enforced on the server. Required screens:")
table(["Screen", "Purpose", "Key Backend Calls"], [
    ("Login", "Authenticate and obtain a JWT.", "POST /api/auth/login"),
    ("Dashboard", "KPI cards + charts (releases, movements, interviews).", "GET /api/dashboard"),
    ("Case Register", "List/search prisoners; open a prisoner.", "GET /api/prisoners, GET /api/prisoners/{id}"),
    ("Prisoner Detail", "Cases + Articles tabs; history timeline.", "GET /api/prisoners/{id}/cases, /articles, /history"),
    ("Releases & Diary", "Manage releases; day/week/range diary + CSV.", "GET/POST /api/releases, GET /api/releases/diary[/export]"),
    ("Duty Register", "Record and view duty assignments.", "GET/POST /api/duties, GET /api/staff"),
    ("In-Out / Court", "Movements and court hearings with outcomes.", "GET/POST /api/movements, /api/court-movements"),
    ("Interviews", "Request lifecycle actions.", "GET/POST /api/interviews, PUT …/approve|schedule|complete"),
    ("Reports", "Daily/monthly/upcoming + statistics.", "GET /api/reports/releases/*, /statistics/*"),
    ("Administration", "Users, roles & permissions, audit log.", "GET/POST /api/users, GET /api/roles, /api/audit-logs"),
], widths=[1.4, 2.6, 2.4])
para("Front-end non-functional expectations: client-side validation is a convenience only — authoritative "
     "validation is server-side; the axios interceptor clears state and redirects to login on HTTP 401; the "
     "build runs under TypeScript strict mode.")
figure(os.path.join(DIAG, "ui-flow.png"), "Figure 2 — Navigation / UI flow across the planned screens.", width=6.3)
doc.add_page_break()

# ============================ 4. DATABASE DESIGN ============================
h1("4. Database Design")
para("Engine: MySQL 8 (InnoDB). Schema management: Flyway (V1 schema, V2 seed). The schema comprises 16 "
     "application tables plus Flyway's flyway_schema_history, for 17 tables in total.")
h2("4.1 Entity and Attribute Definitions")
para("Primary keys are marked PK, foreign keys FK, unique keys U.")
table(["Entity", "Key Attributes"], [
    ("roles", "id (PK), name (U), description"),
    ("permissions", "id (PK), name (U), description"),
    ("users", "id (PK), username (U), email (U), password_hash, full_name, active"),
    ("user_roles", "user_id (PK,FK), role_id (PK,FK)"),
    ("role_permissions", "role_id (PK,FK), permission_id (PK,FK)"),
    ("staff", "id (PK), user_id (FK), staff_number (U), full_name, rank_title, active"),
    ("prisoners", "id (PK), prisoner_number (U), first_name, last_name, admission_date, status, expected_release_date, active"),
    ("cases", "id (PK), prisoner_id (FK), case_number (U), fir_number, crime_type, court, sections, status"),
    ("surrendered_articles", "id (PK), prisoner_id (FK), name, quantity, received_date, return_status"),
    ("release_records", "id (PK), prisoner_id (FK), expected_release_date, release_type, status, authorized_by (FK), actual_release_date"),
    ("duty_register", "id (PK), staff_id (FK), created_by (FK), location, duty_date, start_time, end_time"),
    ("interview_requests", "id (PK), prisoner_id (FK), reviewed_by (FK), visitor_name, requested_date, status"),
    ("movements", "id (PK), escort_officer (FK), person_type, movement_type, time_out, expected_return, actual_return, status"),
    ("court_hearings", "id (PK), movement_id (FK,U), prisoner_id (FK), case_id (FK), escort_officer (FK), court, hearing_date, status, outcome"),
    ("notifications", "id (PK), recipient_user_id (FK), type, title, message, read_flag"),
    ("audit_logs", "id (PK), user_id, action, entity_type, entity_id, old_value, new_value, ip_address"),
], widths=[1.9, 4.5], fs=8.5)
h2("4.2 Relational Schema (DDL)")
para("MySQL 8 DDL (from Flyway migration V1__schema.sql). Tables are created in dependency order; "
     "enumerated values are stored as VARCHAR.")
try:
    with open(SCHEMA, encoding="utf-8") as f:
        ddl = f.read()
    code_block(ddl, fs=7)
except Exception as e:
    para(f"[schema file not found: {e}]")
h2("4.3 Indexes (Performance Optimisation)")
para("B-tree indexes are declared on every foreign key and on the columns each register's search and "
     "report screens filter/sort by (status, dates, business keys) — matching the JpaSpecificationExecutor "
     "queries and satisfying NFR1/NFR2. Examples:")
code_block(
    "KEY idx_prisoner_status (status), idx_prisoner_last_name (last_name),\n"
    "    idx_prisoner_admission (admission_date), idx_prisoner_expected_release (expected_release_date)\n"
    "KEY idx_case_prisoner (prisoner_id), idx_case_crime_type (crime_type), idx_case_status (status)\n"
    "KEY idx_release_expected_date (expected_release_date), idx_release_status (status)\n"
    "KEY idx_movement_status (status), idx_movement_date (movement_date)\n"
    "UNIQUE KEY uq_court_movement (movement_id)   -- enforces the 1:1 movement↔hearing link", fs=8)
h2("4.4 Normalisation Process")
para("See §2.6 for the normalization strategy. The schema is in 3NF/BCNF: no partial or transitive "
     "dependencies remain, many-to-many relations (user↔role, role↔permission) are isolated into junction "
     "tables, and every unique business key is constrained. Lossless decomposition is guaranteed by "
     "foreign-key references back to each parent's primary key.")
h2("4.5 Data Dictionary")
para("Column-level dictionary. PK = Primary Key, FK = Foreign Key, U = Unique, NN = NOT NULL.")

DD = {
    "roles": [("id","BIGINT","PK, NN","Surrogate role identifier."),
              ("name","VARCHAR(50)","NN, U","Role name (e.g. ADMINISTRATOR)."),
              ("description","VARCHAR(255)","","Human-readable description.")],
    "permissions": [("id","BIGINT","PK, NN","Surrogate permission identifier."),
                    ("name","VARCHAR(80)","NN, U","Permission authority (e.g. PRISONER_WRITE)."),
                    ("description","VARCHAR(255)","","Human-readable description.")],
    "users": [("id","BIGINT","PK, NN","Surrogate user identifier."),
              ("username","VARCHAR(60)","NN, U","Login name."),
              ("email","VARCHAR(120)","NN, U","Contact email."),
              ("password_hash","VARCHAR(255)","NN","BCrypt hash; never plaintext."),
              ("full_name","VARCHAR(120)","NN","Display name."),
              ("active","BOOLEAN","NN","Account enabled flag."),
              ("created_at / updated_at","DATETIME","NN","Audit timestamps.")],
    "staff": [("id","BIGINT","PK, NN","Surrogate staff identifier."),
              ("user_id","BIGINT","FK → users","Optional linked login account."),
              ("staff_number","VARCHAR(40)","NN, U","Business staff number."),
              ("full_name","VARCHAR(120)","NN","Staff name."),
              ("rank_title","VARCHAR(60)","","Rank / designation."),
              ("active","BOOLEAN","NN","Active flag.")],
    "prisoners": [("id","BIGINT","PK, NN","Surrogate prisoner identifier."),
              ("prisoner_number","VARCHAR(40)","NN, U","Business prisoner number."),
              ("first_name / last_name","VARCHAR(80)","NN","Name components."),
              ("admission_date","DATE","NN","Date admitted to the facility."),
              ("status","VARCHAR(30)","NN","ACTIVE / RELEASED / TRANSFERRED / DECEASED / INACTIVE."),
              ("expected_release_date","DATE","","Planned release date (feeds the diary)."),
              ("active","BOOLEAN","NN","Soft-delete flag.")],
    "cases": [("id","BIGINT","PK, NN","Surrogate case identifier."),
              ("prisoner_id","BIGINT","NN, FK → prisoners","Owning prisoner."),
              ("case_number","VARCHAR(60)","NN, U","Business case number."),
              ("crime_type","VARCHAR(80)","NN","Category of offence."),
              ("court / sections","VARCHAR","","Court and statutory sections."),
              ("status","VARCHAR(30)","NN","UNDER_INVESTIGATION / UNDER_TRIAL / CONVICTED / ACQUITTED / CLOSED.")],
    "surrendered_articles": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("prisoner_id","BIGINT","NN, FK → prisoners","Owning prisoner."),
              ("name","VARCHAR(120)","NN","Article name."),
              ("quantity","INT","NN","Count (default 1)."),
              ("received_date","DATE","NN","Date taken into custody."),
              ("return_status","VARCHAR(30)","NN","HELD / RETURNED.")],
    "release_records": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("prisoner_id","BIGINT","NN, FK → prisoners","Prisoner being released."),
              ("expected_release_date","DATE","NN","Planned release date."),
              ("release_type","VARCHAR(40)","NN","BAIL / SENTENCE_COMPLETED / etc."),
              ("status","VARCHAR(30)","NN","UPCOMING / READY / ON_HOLD / RELEASED / CANCELLED."),
              ("authorized_by","BIGINT","FK → users","Authorizing user."),
              ("actual_release_date","DATE","","Set when RELEASED.")],
    "duty_register": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("staff_id","BIGINT","NN, FK → staff","Assigned staff member."),
              ("location / purpose","VARCHAR","NN","Where and why."),
              ("duty_date","DATE","NN","Date of duty."),
              ("start_time / end_time","TIME","NN","Shift window (end > start)."),
              ("created_by","BIGINT","FK → users","Recording user.")],
    "interview_requests": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("prisoner_id","BIGINT","NN, FK → prisoners","Prisoner to be visited."),
              ("visitor_name","VARCHAR(120)","NN","Requesting visitor."),
              ("requested_date","DATE","NN","Requested interview date."),
              ("status","VARCHAR(30)","NN","PENDING / APPROVED / REJECTED / SCHEDULED / COMPLETED / CANCELLED."),
              ("reviewed_by","BIGINT","FK → users","Reviewing user.")],
    "movements": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("person_type","VARCHAR(20)","NN","PRISONER / STAFF."),
              ("movement_type","VARCHAR(30)","NN","Category of movement."),
              ("time_out / expected_return / actual_return","DATETIME","","Movement timing (overdue derived)."),
              ("escort_officer","BIGINT","FK → staff","Optional escorting staff."),
              ("status","VARCHAR(30)","NN","SCHEDULED / OUT / RETURNED / CANCELLED.")],
    "court_hearings": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("movement_id","BIGINT","U, FK → movements","Specialized movement (1:1, optional)."),
              ("prisoner_id","BIGINT","NN, FK → prisoners","Prisoner produced in court."),
              ("case_id","BIGINT","FK → cases","Related case."),
              ("court / hearing_date","VARCHAR / DATE","NN","Court and date."),
              ("status / outcome","VARCHAR","NN / —","Forward-only status; recorded outcome.")],
    "notifications": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("recipient_user_id","BIGINT","FK → users","Target user (null = broadcast)."),
              ("type / title / message","VARCHAR","NN","Alert content."),
              ("read_flag","BOOLEAN","NN","Read/unread state."),
              ("created_at","DATETIME","NN","Creation timestamp.")],
    "audit_logs": [("id","BIGINT","PK, NN","Surrogate identifier."),
              ("user_id / username_snapshot","BIGINT / VARCHAR","","Actor (snapshotted)."),
              ("action / entity_type / entity_id","VARCHAR","NN / NN / —","What happened and to which entity."),
              ("old_value / new_value","TEXT","","Before/after serialized state."),
              ("ip_address","VARCHAR(60)","","Originating IP."),
              ("created_at","DATETIME","NN","Append timestamp.")],
}
for tname, cols in DD.items():
    p = doc.add_paragraph(); r = p.add_run(f"Table: {tname}"); r.font.bold = True; r.font.color.rgb = STEEL; r.font.size = Pt(10)
    table(["Column", "Data Type", "Constraints", "Description"], cols, widths=[1.7, 1.3, 1.3, 2.1], fs=8)
para("Note: the junction tables user_roles and role_permissions each consist solely of a composite "
     "primary key of two foreign keys (user_id+role_id and role_id+permission_id respectively).")
h2("4.6 ER Model")
para("The entity-relationship model (crow's-foot notation) is shown in Figure 3, followed by a textual "
     "relationship summary.")
figure(os.path.join(DIAG, "er.png"), "Figure 3 — Entity-Relationship model of the Criminal Detention Portal database.", width=6.5)
table(["Relationship", "Entities", "Cardinality", "Notes"], [
    ("has role", "users — roles", "M : N", "Via user_roles junction."),
    ("grants", "roles — permissions", "M : N", "Via role_permissions junction."),
    ("may link", "users — staff", "1 : 0..1", "A staff record may reference a login account."),
    ("charged in", "prisoners — cases", "1 : N", "A prisoner may have many cases."),
    ("surrenders", "prisoners — surrendered_articles", "1 : N", "Articles taken into custody."),
    ("released by", "prisoners — release_records", "1 : N", "Release lifecycle records."),
    ("visited for", "prisoners — interview_requests", "1 : N", "Visitor interview requests."),
    ("produced for", "prisoners — court_hearings", "1 : N", "Court appearances."),
    ("heard", "cases — court_hearings", "1 : N", "Hearings optionally reference a case."),
    ("assigned", "staff — duty_register", "1 : N", "Duty roster."),
    ("escorts", "staff — movements / court_hearings", "1 : N", "Optional escorting officer."),
    ("specializes", "movements — court_hearings", "1 : 0..1", "A court hearing specializes at most one movement (UNIQUE)."),
    ("authorizes", "users — release_records", "1 : N", "Authorizing user."),
    ("receives", "users — notifications", "1 : N", "Recipient (nullable = broadcast)."),
], widths=[1.3, 2.3, 1.1, 1.7], fs=8.5)
h2("4.7 Class Diagram (Implemented Domain Model)")
para("The domain classes (JPA entities), their key attributes, and associations with multiplicities are "
     "shown in Figure 4. Business operations and lifecycle state machines are implemented in the "
     "corresponding service classes (PrisonerService, ReleaseService, MovementService, …).")
figure(os.path.join(DIAG, "class.png"), "Figure 4 — Domain class diagram (entities, attributes, multiplicities).", width=6.6)
doc.add_page_break()

# ============================ 5. REPORTS / QUERIES / VIEWS ============================
h1("5. Reports, Queries and Derived Views")
para("Reports are read-only aggregations served as DTOs; the release diary is additionally exportable as "
     "CSV. Derived alerts are computed at query time and never stored.")
h2("5.1 Release Reports (daily / monthly / upcoming)")
para("Each report selects release records whose expected release date falls in the requested window and "
     "joins the owning prisoner for display.")
code_block(
    "-- Upcoming releases within a rolling window (conceptual SQL)\n"
    "SELECT r.id, p.prisoner_number, CONCAT(p.first_name,' ',p.last_name) AS name,\n"
    "       r.expected_release_date, r.release_type, r.status\n"
    "FROM release_records r JOIN prisoners p ON p.id = r.prisoner_id\n"
    "WHERE r.status <> 'CANCELLED'\n"
    "  AND r.expected_release_date BETWEEN :from AND :to\n"
    "ORDER BY r.expected_release_date;", fs=8)
h2("5.2 Status Statistics (prisoner / case)")
para("Aggregations grouped by status drive the dashboard and the Reports → Statistics charts.")
code_block(
    "SELECT status, COUNT(*) AS value FROM prisoners GROUP BY status;\n"
    "SELECT status, COUNT(*) AS value FROM cases     GROUP BY status;", fs=8)
h2("5.3 Derived (Computed) Queries")
code_block(
    "-- Overdue movements: still OUT and past expected return\n"
    "SELECT * FROM movements WHERE status = 'OUT' AND expected_return < NOW();\n\n"
    "-- Not-returned court hearings: departed but not yet RETURNED past expected return\n"
    "SELECT * FROM court_hearings\n"
    "WHERE status <> 'RETURNED' AND status <> 'CANCELLED' AND expected_return < NOW();", fs=8)
h2("5.4 Analytical Reporting Queries")
table(["ID", "Description", "SQL Outline"], [
    ("Q1", "Releases due today", "… WHERE expected_release_date = CURDATE() AND status <> 'CANCELLED'"),
    ("Q2", "Releases in a month", "… WHERE YEAR(expected_release_date)=:y AND MONTH(...)=:m"),
    ("Q3", "Prisoners by status", "SELECT status, COUNT(*) FROM prisoners GROUP BY status"),
    ("Q4", "Cases by status", "SELECT status, COUNT(*) FROM cases GROUP BY status"),
    ("Q5", "Overdue movements", "SELECT COUNT(*) FROM movements WHERE status='OUT' AND expected_return<NOW()"),
    ("Q6", "Pending interviews", "SELECT COUNT(*) FROM interview_requests WHERE status='PENDING'"),
], widths=[0.6, 2.3, 3.5], fs=8.5)
doc.add_page_break()

# ============================ 6. SYSTEM FEATURES ============================
h1("6. System Features")
h2("6.1 Data Integrity")
for b in [
    "Primary-key constraints on every table prevent duplicate records.",
    "Foreign-key constraints enforce referential integrity across all registers.",
    "NOT NULL constraints protect business-critical columns (status, dates, business keys).",
    "UNIQUE constraints prevent duplicate usernames, emails, prisoner/case/staff numbers, and the 1:1 movement↔hearing link.",
    "Enumerated values are validated in the service layer before persistence.",
]:
    bullet(b)
h2("6.2 Normalisation Summary")
para("The schema is in 3NF/BCNF throughout, with many-to-many relations isolated into junction tables. "
     "This removes update, insert, and delete anomalies while keeping queries index-friendly.")
h2("6.3 Reporting Capabilities")
for b in [
    "Release diary for a day, week, or custom range (CSV export).",
    "Day-wise / month-wise / upcoming release reports.",
    "Prisoner-status and case-status statistics.",
    "Operational dashboard KPIs: active prisoners, upcoming releases, overdue movements, pending interviews.",
    "Derived alerts: overdue movements and not-returned court hearings.",
]:
    bullet(b)
h2("6.4 Search, Pagination & Effective Joins")
for b in [
    "Dynamic filters are built with JPA Specifications (JpaSpecificationExecutor).",
    "All list endpoints return a uniform PageResponse<T> envelope (content, page, size, totalElements, totalPages, first, last).",
    "Foreign-key and filter columns are indexed, so joins and filtered scans stay efficient.",
    "DTO projections keep entities inside the service layer and payloads lean.",
]:
    bullet(b)
doc.add_page_break()

# ============================ 7. USE CASE MODELS ============================
h1("7. Use Case Models")
para("Actors: Administrator, Prison Officer, Police Official, Government Official, Auditor. The use-case "
     "diagram (Figure 5) and the summary table below cover the implemented functionality.")
figure(os.path.join(DIAG, "usecase.png"), "Figure 5 — Use case diagram (actors × system use cases).", width=5.2)
table(["Use Case", "Primary Actor(s)", "Description"], [
    ("UC1 Manage Case Register", "Administrator, Prison Officer", "Register prisoners, record cases and surrendered articles."),
    ("UC2 Manage Releases & Diary", "Administrator, Prison Officer", "Drive release lifecycle; generate/export the release diary."),
    ("UC3 Manage Duty Register", "Administrator, Prison Officer", "Record staff duties with validation."),
    ("UC4 Review Interview Requests", "Administrator, Prison Officer", "Approve/reject/schedule/complete visitor interviews."),
    ("UC5 Record In-Out Movements", "Administrator, Prison Officer", "Log movements in/out; mark return; view overdue."),
    ("UC6 Manage Court Movements", "Administrator, Police Official", "Drive court-hearing lifecycle and record outcomes."),
    ("UC7 View Reports & Dashboard", "All roles", "View KPI dashboard, reports, and statistics."),
    ("UC8 Manage Users & Roles", "Administrator", "Create/update users; view roles and permissions."),
    ("UC9 View Audit Log", "Administrator, Auditor", "Inspect the append-only audit trail."),
    ("UC10 Receive Notifications", "All roles", "Receive and read in-app alerts."),
], widths=[2.0, 2.0, 2.4], fs=8.5)
doc.add_page_break()

# ============================ 8. BACKEND SPECS ============================
h1("8. Backend Specifications, Connectivity and Application")
h2("8.1 Application & Server Specification")
table(["Item", "Specification"], [
    ("Runtime", "Java 21 (LTS); Spring Boot 3.3.4 — single executable service."),
    ("Web", "Spring Web (embedded Tomcat) on port 8080; context path '/'."),
    ("Persistence", "Spring Data JPA / Hibernate; MySQL Connector/J (JDBC)."),
    ("Database", "MySQL 8 (InnoDB); Flyway migrations (V1 schema, V2 seed)."),
    ("Security", "Spring Security; stateless JWT (HS256, jjwt 0.12.6); BCrypt."),
    ("API Docs", "springdoc-openapi 2.6.0 — Swagger UI at /swagger-ui/index.html."),
    ("Character encoding", "UTF-8; timestamps in UTC."),
    ("Build", "Maven; frontend built with Vite / npm."),
], widths=[1.6, 4.8])
h2("8.2 Connectivity")
para("The application connects to MySQL over JDBC using the HikariCP connection pool. The URL and "
     "credentials come from environment variables; nothing is hard-coded.")
code_block(
    "spring.datasource.url =\n"
    "  jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC\n"
    "DB_USERNAME / DB_PASSWORD / JWT_SECRET  -> supplied via environment (.env, git-ignored)", fs=8)
h2("8.3 REST API Endpoints (implemented)")
para("All endpoints are under /api. Write and read operations are guarded by method-level permissions; "
     "login and register are public. Representative endpoints:")
table(["Method & Path", "Auth (permission)", "Purpose"], [
    ("POST /api/auth/login", "public", "Authenticate; return a signed JWT + roles/permissions."),
    ("POST /api/auth/register", "public", "Create an account (BCrypt-hashed password)."),
    ("POST /api/auth/change-password", "authenticated", "Change own password under policy."),
    ("GET /api/prisoners", "PRISONER_READ", "Search + paginate the case register."),
    ("POST /api/prisoners", "PRISONER_WRITE", "Register a prisoner."),
    ("DELETE /api/prisoners/{id}", "PRISONER_DELETE", "Soft-delete a prisoner."),
    ("GET/POST /api/prisoners/{id}/cases", "CASE_READ / CASE_WRITE", "List/add cases for a prisoner."),
    ("GET/POST /api/prisoners/{id}/articles", "ARTICLE_READ / ARTICLE_WRITE", "List/add surrendered articles."),
    ("GET/POST /api/releases", "RELEASE_READ / RELEASE_WRITE", "List/create release records."),
    ("PUT /api/releases/{id}/status", "RELEASE_WRITE", "Advance the release lifecycle."),
    ("GET /api/releases/diary[/export]", "RELEASE_READ", "Release diary; CSV export."),
    ("GET/POST /api/duties", "DUTY_READ / DUTY_WRITE", "Duty register (overlap-validated)."),
    ("GET/POST /api/interviews", "INTERVIEW_READ / INTERVIEW_WRITE", "Interview requests."),
    ("PUT /api/interviews/{id}/{action}", "INTERVIEW_REVIEW", "approve/reject/schedule/complete/cancel."),
    ("GET/POST /api/movements", "MOVEMENT_READ / MOVEMENT_WRITE", "In-Out register."),
    ("GET /api/movements/overdue", "MOVEMENT_READ", "Derived overdue movements."),
    ("GET/POST /api/court-movements", "COURT_READ / COURT_WRITE", "Court hearings."),
    ("GET /api/court-movements/not-returned", "COURT_READ", "Derived not-returned hearings."),
    ("GET /api/reports/releases/{daily|monthly|upcoming}", "REPORT_READ", "Release reports."),
    ("GET /api/reports/statistics/{prisoners|cases}", "REPORT_READ", "Status statistics."),
    ("GET /api/dashboard", "DASHBOARD_READ", "KPI aggregates."),
    ("GET/POST /api/notifications[/generate]", "NOTIFICATION_READ", "List / generate alerts."),
    ("GET /api/users; POST/PUT …", "USER_READ / USER_WRITE", "User management."),
    ("GET /api/roles[/permissions]", "ROLE_READ", "Roles & permission catalogue."),
    ("GET /api/audit-logs", "AUDIT_READ", "Read the append-only audit trail."),
], widths=[2.7, 1.9, 1.8], fs=8)
h2("8.4 Deployment and CI/CD (concept)")
para("Pipeline: git push → CI (build, run JUnit/Mockito + Spring slice tests on H2) → build the Spring Boot "
     "jar and the Vite SPA bundle → deploy the backend alongside a MySQL container; Flyway applies V1/V2 and "
     "a DataInitializer seeds demo users on first start. The SPA is served statically and proxies /api to "
     "the backend.")
h2("8.5 Security and Authorisation")
for b in [
    "Authentication: stateless JWT (HS256, jjwt). POST /api/auth/login verifies BCrypt credentials and returns a signed token; a OncePerRequestFilter validates it on every request.",
    "Authorisation: 26 permissions as Spring authorities; endpoints guarded by @PreAuthorize with @EnableMethodSecurity. The backend is the source of truth — hidden UI controls are UX only.",
    "Secrets: JWT key and DB credentials come from environment variables; the real .env is git-ignored.",
    "Passwords: stored only as BCrypt hashes and never logged; password policy enforced on register/change.",
    "Input validation: jakarta.validation on request DTOs → HTTP 400 on invalid input.",
    "Error hygiene: a single @RestControllerAdvice maps exceptions to sanitized JSON (400/401/403/404/409/422); stack traces are never exposed.",
    "Audit: every state-changing action is appended to audit_logs in a REQUIRES_NEW transaction; the log has no update/delete path.",
    "Transport: CORS restricted to the configured SPA origin; HTTPS in production.",
]:
    bullet(b)
h2("8.6 System Architecture")
figure(os.path.join(DIAG, "architecture.png"), "Figure 6 — System architecture: SPA → Spring Security → layered backend → MySQL.", width=3.9)
figure(os.path.join(DIAG, "sequence-auth.png"), "Figure 7 — Authentication & authorised-request sequence (login → JWT → guarded call).", width=6.3)
doc.add_page_break()

# ============================ 9. EXTERNAL INTERFACES ============================
h1("9. External Interface Requirements")
table(["Interface Type", "Description"], [
    ("User Interface", "Responsive React + TypeScript SPA (role-filtered sidebar); Swagger UI for API exploration."),
    ("Software Interface", "REST/JSON API under /api documented via OpenAPI; JDBC to MySQL 8."),
    ("Hardware Interface", "Standard workstation or cloud VM (2 vCPU, 4 GB RAM) for the backend + DB."),
    ("Communication", "HTTP/HTTPS; Authorization: Bearer <JWT>; CORS limited to the SPA origin."),
    ("Database Interface", "MySQL 8 over JDBC with HikariCP; schema versioned by Flyway."),
], widths=[1.8, 4.6])
doc.add_page_break()

# ============================ 10. V&V ============================
h1("10. Validation and Verification")
para("The backend ships with JUnit 5 + Mockito unit tests and Spring slice/integration tests (against an "
     "in-memory H2 database in the test profile), plus manual smoke tests for report and CSV flows.")
h2("10.1 Requirements Traceability Matrix")
table(["Req", "Implementation (controller / schema)", "Verified by"], [
    ("FR1–FR6", "PrisonerController, CaseController, ArticleController; prisoners/cases/surrendered_articles", "PrisonerServiceTest; SecurityAuthorizationIT (PRISONER_WRITE)"),
    ("FR7–FR9", "ReleaseController, ReleaseDiaryController; release_records", "Release lifecycle checks; diary/CSV smoke"),
    ("FR10", "DutyController; duty_register", "DutyServiceTest (time order + overlap rejection)"),
    ("FR11–FR12", "InterviewController; interview_requests state machine", "InterviewServiceTest (transitions + duplicate conflict)"),
    ("FR13–FR15", "MovementController, CourtMovementController; movements/court_hearings", "MovementServiceTest, CourtMovementServiceTest"),
    ("FR16–FR18", "UserController, RoleController, AuthController; RBAC", "SecurityAuthorizationIT (403 vs 201), AuthControllerIT"),
    ("FR19–FR21", "ReportController, DashboardController", "Aggregation smoke tests"),
    ("FR22", "NotificationController /generate", "Manual smoke (alerts recomputed on demand)"),
    ("FR23", "AuditService (REQUIRES_NEW); AuditLogController", "API-surface review (no update/delete path)"),
    ("FR24", "AuthController /login; JwtService, JwtAuthFilter", "JwtServiceTest, AuthControllerIT"),
    ("NFR3–NFR7", "Spring Security, @RestControllerAdvice, env-var config", "SecurityAuthorizationIT; error-handling tests; config review"),
], widths=[1.1, 3.3, 2.0], fs=8.5)
doc.add_page_break()

# ============================ 11. INSTALL & OPERATE ============================
h1("11. Installation and Operation")
h2("11.1 Run with Docker Compose (database)")
code_block("cp .env.example .env            # set DB creds + a 32+ byte JWT_SECRET\n"
           "docker compose up -d            # starts MySQL 8 (auto-migrated + seeded by the backend)", fs=8)
h2("11.2 Run the backend and frontend")
code_block("cd backend && set -a && . ../.env && set +a && mvn spring-boot:run   # API on :8080\n"
           "cd frontend && cp .env.example .env.local && npm install && npm run dev  # SPA on :5173", fs=8)
h2("11.3 Run the tests")
code_block("cd backend && mvn test          # JUnit + Spring slice/integration tests on in-memory H2", fs=8)
h2("11.4 Demo Accounts")
table(["Username", "Password", "Role"], [
    ("admin", "Admin@123", "Administrator"),
    ("officer", "Officer@123", "Prison Officer"),
    ("police", "Police@123", "Police Official"),
    ("govt", "Govt@123", "Government Official"),
    ("auditor", "Auditor@123", "Auditor"),
], widths=[1.6, 1.8, 3.0], fs=9)
h2("11.5 Backup & Recovery")
code_block("mysqldump -h 127.0.0.1 -P 3306 -u cdp_user -p cdp > backup.sql   # logical backup\n"
           "mysql    -h 127.0.0.1 -P 3306 -u cdp_user -p cdp < backup.sql   # restore", fs=8)
doc.add_page_break()

# ============================ 12. APPENDIX ============================
h1("12. Appendix")
h2("12.1 Possible Future Enhancements")
for b in [
    "Biometric / photograph capture and secure document attachment storage.",
    "Full-text search across cases and remarks.",
    "Scheduled jobs that push release and overdue-movement alerts automatically.",
    "Configurable report templates and PDF export in addition to CSV.",
    "Multi-facility support with facility-scoped roles.",
    "Fine-grained field-level audit diffing and an audit-export endpoint.",
]:
    bullet(b)
h2("12.2 Document History")
table(["Version", "Date", "Description", "Author"], [
    ("0.1", "<Date>", "Initial skeleton from project design docs.", "<Name, ID>"),
    ("1.0", "<Date>", "Full SRS aligned to the IEEE-830 reference structure.", "<Name, ID>"),
], widths=[0.9, 1.1, 3.2, 1.2], fs=9)
h2("12.3 Compliance")
para("This SRS follows IEEE 830-1998 and ISO/IEC/IEEE 29148:2018: a structured requirement hierarchy with "
     "unique IDs (FR/NFR/DR), explicit scope and constraints, a complete data model and data dictionary, "
     "use-case and architecture models, and a requirements-traceability matrix linking every functional "
     "requirement to its implementation and verifying test.")

out = os.path.join(HERE, "SRS_KT_CDP_019.docx")
doc.save(out)
print("SRS (full) ->", out)
print("paragraphs:", len(doc.paragraphs), "| tables:", len(doc.tables), "| images:", len(doc.inline_shapes))
