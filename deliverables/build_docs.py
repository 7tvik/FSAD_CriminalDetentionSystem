#!/usr/bin/env python3
"""Generate SRS.docx and Project_Abstract.docx for KT_CDP_019 Criminal Detention Portal."""
import os
from docx import Document
from docx.shared import Pt, Inches, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

HERE = os.path.dirname(os.path.abspath(__file__))
DIAG = os.path.join(HERE, "diagrams")
NAVY = RGBColor(0x1F, 0x4E, 0x79)
STEEL = RGBColor(0x2E, 0x5E, 0x8C)
GREY = RGBColor(0x55, 0x5555 // 0x100, 0x55)

PROJECT = "KT_CDP_019 — Criminal Detention Portal"
SUBTITLE = "A Prison-Administration System: Registers & Reports for the Effective Management of Prisons"


def set_cell_bg(cell, hexcolor):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), hexcolor)
    tcPr.append(shd)


def style_base(doc):
    normal = doc.styles["Normal"]
    normal.font.name = "Calibri"
    normal.font.size = Pt(11)
    for i, sz in [(1, 18), (2, 14), (3, 12)]:
        h = doc.styles[f"Heading {i}"]
        h.font.name = "Calibri"
        h.font.size = Pt(sz)
        h.font.color.rgb = NAVY
        h.font.bold = True


def add_cover(doc, doc_title, meta_rows):
    for _ in range(3):
        doc.add_paragraph()
    t = doc.add_paragraph()
    t.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = t.add_run(PROJECT)
    r.font.size = Pt(26)
    r.font.bold = True
    r.font.color.rgb = NAVY
    s = doc.add_paragraph()
    s.alignment = WD_ALIGN_PARAGRAPH.CENTER
    rs = s.add_run(SUBTITLE)
    rs.font.size = Pt(13)
    rs.font.italic = True
    rs.font.color.rgb = STEEL
    doc.add_paragraph()
    dt = doc.add_paragraph()
    dt.alignment = WD_ALIGN_PARAGRAPH.CENTER
    rd = dt.add_run(doc_title)
    rd.font.size = Pt(20)
    rd.font.bold = True
    for _ in range(2):
        doc.add_paragraph()
    tbl = doc.add_table(rows=0, cols=2)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    tbl.style = "Light List Accent 1"
    for k, v in meta_rows:
        cells = tbl.add_row().cells
        cells[0].text = k
        cells[1].text = v
        cells[0].paragraphs[0].runs[0].font.bold = True
    note = doc.add_paragraph()
    note.alignment = WD_ALIGN_PARAGRAPH.CENTER
    rn = note.add_run(
        "Academic / demonstration project. All data is fictional. The system keeps registers "
        "and produces reports; it makes no automated decisions about guilt, sentencing, or risk."
    )
    rn.font.italic = True
    rn.font.size = Pt(9)
    rn.font.color.rgb = RGBColor(0x80, 0x80, 0x80)
    doc.add_page_break()


def h1(doc, text):
    doc.add_heading(text, level=1)


def h2(doc, text):
    doc.add_heading(text, level=2)


def para(doc, text):
    return doc.add_paragraph(text)


def bullet(doc, text):
    return doc.add_paragraph(text, style="List Bullet")


def numbered(doc, text):
    return doc.add_paragraph(text, style="List Number")


def table(doc, headers, rows, widths=None):
    t = doc.add_table(rows=1, cols=len(headers))
    t.style = "Light Grid Accent 1"
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    hdr = t.rows[0].cells
    for i, hname in enumerate(headers):
        hdr[i].text = ""
        run = hdr[i].paragraphs[0].add_run(hname)
        run.font.bold = True
        run.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)
        set_cell_bg(hdr[i], "1F4E79")
    for row in rows:
        cells = t.add_row().cells
        for i, val in enumerate(row):
            cells[i].text = str(val)
            for p in cells[i].paragraphs:
                for r in p.runs:
                    r.font.size = Pt(9.5)
    if widths:
        for i, w in enumerate(widths):
            for row in t.rows:
                row.cells[i].width = Inches(w)
    return t


def add_image(doc, path, caption, width=6.3):
    doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.add_run().add_picture(path, width=Inches(width))
    c = doc.add_paragraph()
    c.alignment = WD_ALIGN_PARAGRAPH.CENTER
    rc = c.add_run(caption)
    rc.font.italic = True
    rc.font.size = Pt(9)
    rc.font.color.rgb = RGBColor(0x80, 0x80, 0x80)


# ========================= SRS DOCUMENT =========================
def build_srs():
    doc = Document()
    style_base(doc)
    add_cover(
        doc,
        "Software Requirements Specification (SRS)",
        [
            ("Document", "Software Requirements Specification"),
            ("Version", "1.0"),
            ("Project Code", "KT_CDP_019"),
            ("Course", "Full Stack Application Development (FSAD)"),
            ("Status", "Baseline"),
        ],
    )

    h1(doc, "1. Introduction")
    h2(doc, "1.1 Purpose")
    para(doc,
         "This Software Requirements Specification (SRS) defines the functional and non-functional "
         "requirements of the Criminal Detention Portal (CDP), an academic prison-administration "
         "system. It is intended for evaluators, developers, and testers as the authoritative "
         "statement of what the system does and the constraints it operates under.")
    h2(doc, "1.2 Scope")
    para(doc,
         "The CDP digitises the day-to-day registers a detention facility maintains — the case "
         "register, release diary, duty roster, interview requests, and the in-out (movement) "
         "register — and layers role-based access control, an operational dashboard, in-app "
         "notifications, reports, and an append-only audit trail on top. The system is a collection "
         "of registers and reports for the effective management of prisons. It performs no "
         "automated decisions about guilt, sentencing, or risk; all data is fictional.")
    h2(doc, "1.3 Definitions, Acronyms, and Abbreviations")
    table(doc, ["Term", "Definition"], [
        ("RBAC", "Role-Based Access Control — permissions grouped into roles."),
        ("JWT", "JSON Web Token — stateless bearer token used for authentication (HS256)."),
        ("SPA", "Single-Page Application (the React frontend)."),
        ("FIR", "First Information Report — police record of a reported offence."),
        ("DTO", "Data Transfer Object — API request/response record."),
        ("CRUD", "Create, Read, Update, Delete."),
        ("In-Out Register", "Record of movements of prisoners/staff in and out of the facility."),
    ], widths=[1.4, 5.0])
    h2(doc, "1.4 References")
    bullet(doc, "Project assignment brief and requirement spec (KT_CDP_019).")
    bullet(doc, "Architecture, Database, API, and Testing design documents (project /docs).")
    bullet(doc, "OpenAPI / Swagger contract exposed by the running backend at /swagger-ui.")
    h2(doc, "1.5 Overview")
    para(doc,
         "Section 2 gives the overall product description, users, and constraints. Section 3 lists "
         "the detailed functional requirements per module. Section 4 covers external interfaces. "
         "Section 5 states non-functional and security requirements. Section 6 captures the data "
         "model, and the appendix documents the lifecycle state machines.")

    h1(doc, "2. Overall Description")
    h2(doc, "2.1 Product Perspective")
    para(doc,
         "The CDP is a self-contained, three-tier web application built as a modular monolith. A "
         "React + TypeScript SPA communicates over REST/JSON with a Spring Boot backend, which "
         "persists to a MySQL database. The backend is organised into feature modules (prisoners, "
         "cases, releases, duties, interviews, movements, court, admin, audit) with a strict "
         "Controller → Service → Repository dependency flow.")
    add_image(doc, os.path.join(DIAG, "architecture.png"),
              "Figure 1 — High-level system architecture (three-tier modular monolith).", width=4.3)
    h2(doc, "2.2 Product Functions (Summary)")
    bullet(doc, "Maintain the Case Register: prisoners, their cases, and surrendered articles.")
    bullet(doc, "Generate an automated Release Diary (day / week / custom range) with CSV export.")
    bullet(doc, "Maintain the Duty Register with overlap validation.")
    bullet(doc, "Manage visitor Interview Requests through a review lifecycle.")
    bullet(doc, "Maintain the In-Out Register, including dedicated Court movements with outcomes.")
    bullet(doc, "Administer users, roles, and 26 fine-grained permissions (RBAC).")
    bullet(doc, "Produce day-wise / month-wise / upcoming release reports and status statistics.")
    bullet(doc, "Provide an operational dashboard, notifications, and an append-only audit trail.")
    h2(doc, "2.3 User Classes and Characteristics")
    table(doc, ["Role", "Description", "Primary Access"], [
        ("Administrator", "System owner / super-user.", "Full access, incl. user management."),
        ("Prison Officer", "Day-to-day facility operations.", "Registers, movements, releases, interviews, duties."),
        ("Police Official", "Investigation & court liaison.", "Cases and court movements."),
        ("Government Official", "Oversight / monitoring.", "Read-only access + reports."),
        ("Auditor", "Compliance / review.", "Read-only access + audit logs."),
    ], widths=[1.5, 2.6, 2.3])
    h2(doc, "2.4 Operating Environment")
    bullet(doc, "Backend: Java 21, Spring Boot 3.3, runs on any OS with a JVM; packaged as a single service.")
    bullet(doc, "Database: MySQL 8 (Docker container or local install); Flyway migrates on startup.")
    bullet(doc, "Frontend: Modern evergreen browser (Chrome/Edge/Firefox/Safari); served by Vite in dev.")
    h2(doc, "2.5 Design and Implementation Constraints")
    bullet(doc, "Authorization must be enforced on the backend; hiding UI controls is UX only.")
    bullet(doc, "No secret may be hard-coded; credentials and the JWT secret come from environment variables.")
    bullet(doc, "Passwords are persisted only as BCrypt hashes and are never logged.")
    bullet(doc, "The audit trail is append-only — no update or delete path.")
    bullet(doc, "Derived states (e.g. 'overdue') are computed at query time, never stored.")
    h2(doc, "2.6 Assumptions and Dependencies")
    bullet(doc, "A reachable MySQL instance and a configured JWT secret are available at runtime.")
    bullet(doc, "Demo users and seed data are provisioned at first startup for evaluation.")

    h1(doc, "3. Functional Requirements")
    para(doc, "Each requirement below is traceable to a backend controller/service and a frontend page.")

    modules = [
        ("FR-1", "Case Register (Prisoners, Cases & Articles)", [
            "The system shall allow registering a prisoner with personal, admission, and status details.",
            "The system shall record one or more cases per prisoner (FIR number, crime type, sections, court, investigating officer, status).",
            "The system shall record surrendered articles taken into custody at admission, each with a return status (HELD / RETURNED).",
            "The system shall support search, filtering, and pagination across the prisoner register.",
            "The system shall soft-delete prisoners (mark inactive) rather than removing history.",
        ]),
        ("FR-2", "Automated Release Diary", [
            "The system shall generate an upcoming-release diary for a day, a week, or a custom date range.",
            "The diary shall be derived from each prisoner's expected release date (never a stored flag).",
            "The system shall export the release diary as a downloadable CSV file.",
        ]),
        ("FR-3", "Duty Register", [
            "The system shall record staff duty assignments (location, purpose, date, start/end time).",
            "The system shall reject a duty whose end time is not after its start time.",
            "The system shall reject overlapping duty assignments for the same staff member.",
        ]),
        ("FR-4", "Interview Requests", [
            "The system shall capture visitor interview requests against a prisoner.",
            "The system shall enforce a review lifecycle: PENDING → APPROVED/REJECTED → SCHEDULED → COMPLETED (with CANCELLED).",
            "The system shall reject illegal lifecycle transitions and duplicate active requests.",
        ]),
        ("FR-5", "In-Out & Court Movement Register", [
            "The system shall record movements of prisoners/staff in and out of the facility.",
            "The system shall track a dedicated Court movement with hearing date, court, and outcome.",
            "The system shall mark a movement OUT or SCHEDULED on creation and support return/cancel transitions.",
            "The system shall derive 'overdue' movements and 'not-returned' court hearings at query time.",
        ]),
        ("FR-6", "Administration & RBAC", [
            "The system shall support user management (create, update, activate/deactivate).",
            "The system shall define 5 roles composed of 26 fine-grained permissions.",
            "The system shall enforce every permission on the backend via method-level security.",
            "The system shall allow a user to change their own password under a password policy.",
        ]),
        ("FR-7", "Reports", [
            "The system shall produce day-wise and month-wise release reports.",
            "The system shall produce an upcoming-release report over a rolling window.",
            "The system shall produce prisoner-status and case-status statistics for visualization.",
        ]),
        ("FR-8", "Dashboard, Notifications & Audit (Enhancements)", [
            "The system shall present an operational dashboard with KPI cards and charts.",
            "The system shall generate in-app notifications for releases, overdue movements, and pending interviews.",
            "The system shall write an append-only audit record for every state-changing action.",
        ]),
    ]
    for code, title, reqs in modules:
        h2(doc, f"3.{code[-1]} {code}: {title}")
        for i, req in enumerate(reqs, 1):
            numbered(doc, req)

    h1(doc, "4. External Interface Requirements")
    h2(doc, "4.1 User Interfaces")
    para(doc, "A responsive SPA with a role-filtered sidebar. Every register follows one standard "
              "list-page pattern (search, filters, pagination, detail/drawer). See the UI flow in Figure 2.")
    add_image(doc, os.path.join(DIAG, "ui-flow.png"),
              "Figure 2 — Navigation / UI flow across the planned screens.", width=6.3)
    h2(doc, "4.2 Software Interfaces")
    bullet(doc, "REST/JSON API under /api, documented via OpenAPI (Swagger UI).")
    bullet(doc, "Authentication via POST /api/auth/login returning an HS256 JWT bearer token.")
    bullet(doc, "JDBC connection to MySQL 8; schema managed by Flyway migrations.")
    h2(doc, "4.3 Communication Interfaces")
    bullet(doc, "HTTPS in production; CORS restricted to the configured SPA origin.")

    h1(doc, "5. Non-Functional Requirements")
    table(doc, ["Category", "Requirement"], [
        ("Security", "Stateless JWT auth; BCrypt password hashing; backend-enforced RBAC; no secrets in source."),
        ("Security", "Stack traces never exposed; a single advice returns sanitized JSON error bodies."),
        ("Reliability", "Audit writes run in a REQUIRES_NEW transaction so they survive a rolled-back request."),
        ("Usability", "Consistent list-page UX; clear validation messages; role-appropriate navigation."),
        ("Performance", "Indexed search columns; server-side pagination on every register."),
        ("Maintainability", "Layered architecture; DTOs at the boundary; TypeScript strict mode on the client."),
        ("Portability", "Dockerised database; environment-variable configuration; JVM-portable backend."),
        ("Auditability", "Append-only audit log with before/after values and actor identity."),
    ], widths=[1.6, 4.8])
    h2(doc, "5.1 Error Handling (HTTP Status Contract)")
    table(doc, ["Status", "Meaning"], [
        ("400", "Input validation failure (Bean Validation)."),
        ("401", "Unauthenticated / invalid or expired token."),
        ("403", "Authenticated but lacking the required permission."),
        ("404", "Resource not found."),
        ("409", "Conflict (e.g. duplicate unique key)."),
        ("422", "Business-rule violation (e.g. illegal state transition)."),
    ], widths=[1.2, 5.2])

    h1(doc, "6. Data Requirements")
    para(doc, "The system persists 17 related tables in MySQL 8 (InnoDB). Enumerated values are "
              "stored as human-readable strings. The entity-relationship model is shown in Figure 3.")
    add_image(doc, os.path.join(DIAG, "er.png"),
              "Figure 3 — Entity-Relationship (ER) diagram / database schema.", width=6.5)

    h1(doc, "Appendix A — Lifecycle State Machines")
    para(doc, "Illegal transitions are rejected by the service layer and surface as HTTP 422.")
    table(doc, ["Domain", "States & legal transitions"], [
        ("Release", "UPCOMING → {READY, ON_HOLD, CANCELLED}; READY → {RELEASED, ON_HOLD, CANCELLED}; "
                    "ON_HOLD → {UPCOMING, READY, CANCELLED}; RELEASED/CANCELLED terminal."),
        ("Interview", "PENDING → approve/reject/cancel; APPROVED → schedule/cancel; "
                      "SCHEDULED → complete/cancel; REJECTED/COMPLETED/CANCELLED terminal."),
        ("Movement", "Created OUT (if time-out set) else SCHEDULED; markReturned only from OUT; "
                     "cancel from SCHEDULED/OUT; overdue derived."),
        ("Court hearing", "Forward-only SCHEDULED → DEPARTED → AT_COURT → HEARING_COMPLETED → "
                          "DEPARTED_COURT → RETURNED; plus SCHEDULED → CANCELLED."),
    ], widths=[1.4, 5.0])

    out = os.path.join(HERE, "SRS_KT_CDP_019.docx")
    doc.save(out)
    return out


# ========================= ABSTRACT DOCUMENT =========================
def build_abstract():
    doc = Document()
    style_base(doc)
    title = doc.add_paragraph()
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = title.add_run(PROJECT)
    r.font.size = Pt(20)
    r.font.bold = True
    r.font.color.rgb = NAVY
    sub = doc.add_paragraph()
    sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    rs = sub.add_run("Project Abstract")
    rs.font.size = Pt(14)
    rs.font.bold = True
    rs.font.color.rgb = STEEL
    doc.add_paragraph()

    h1(doc, "Problem Statement")
    para(doc,
         "Detention facilities rely on a large number of manual, paper-based registers — the case "
         "register, release diary, duty roster, visitor interview requests, and the in-out movement "
         "register. Maintaining these by hand is slow, error-prone, hard to search, and difficult to "
         "audit. Upcoming releases and overdue court movements are easy to miss, access to sensitive "
         "records is hard to control, and there is no reliable, tamper-evident trail of who changed "
         "what and when.")

    h1(doc, "Objective")
    para(doc,
         "To design and build a secure, full-stack web application that digitises the core prison "
         "registers and the reports derived from them, with strict role-based access control, an "
         "operational dashboard, timely notifications, and an append-only audit trail — providing "
         "effective, searchable, and accountable management of day-to-day facility operations.")

    h1(doc, "Proposed Solution")
    para(doc,
         "The Criminal Detention Portal (CDP) is a three-tier modular-monolith web application. A "
         "React + TypeScript single-page application provides role-filtered screens for each "
         "register; a Spring Boot backend enforces business rules, lifecycle state machines, and "
         "permission checks; and a MySQL database (schema-managed by Flyway) stores all records. "
         "Authentication is stateless via JWT, authorization is enforced server-side through 26 "
         "fine-grained permissions, and every state-changing action is recorded in an immutable "
         "audit log. Reports and the release diary are derived at query time and exportable as CSV. "
         "The system is academic and uses only fictional data; it makes no automated decisions "
         "about guilt, sentencing, or risk.")

    h1(doc, "Modules")
    table(doc, ["Module", "Responsibility"], [
        ("Case Register", "Prisoners, their cases (FIR/sections/court), and surrendered articles."),
        ("Release Diary & Releases", "Release records with lifecycle; day/week/range diary + CSV export."),
        ("Duty Register", "Staff duty roster with overlap and time-order validation."),
        ("Interview Requests", "Visitor interview requests through a review lifecycle."),
        ("In-Out Register", "Movements in/out of the facility; overdue detection."),
        ("Court Movements", "Court-hearing movements with forward-only lifecycle and outcomes."),
        ("Reports", "Day/month/upcoming release reports; prisoner- and case-status statistics."),
        ("Administration (RBAC)", "Users, roles, and 26 permissions; profile & password management."),
        ("Dashboard / Notifications / Audit", "KPI dashboard, on-demand alerts, append-only audit trail."),
    ], widths=[2.2, 4.2])

    h1(doc, "Users & Roles")
    table(doc, ["Role", "Access"], [
        ("Administrator", "Full access, including user and role management."),
        ("Prison Officer", "Registers, movements, releases, interviews, and duties."),
        ("Police Official", "Cases and court movements."),
        ("Government Official", "Read-only access plus reports."),
        ("Auditor", "Read-only access plus audit logs."),
    ], widths=[2.0, 4.4])

    h1(doc, "Technologies Used")
    table(doc, ["Layer", "Technology"], [
        ("Frontend", "React 18, TypeScript (strict), Vite, react-router-dom v6, axios, recharts."),
        ("Backend", "Java 21, Spring Boot 3.3, Spring Web, Spring Security (JWT/BCrypt), Spring Data JPA/Hibernate."),
        ("Database", "MySQL 8 (InnoDB), Flyway migrations."),
        ("Auth", "Stateless JWT (HS256, jjwt), BCrypt password hashing, method-level @PreAuthorize."),
        ("API Docs", "springdoc-openapi (Swagger UI)."),
        ("Testing", "JUnit 5, Mockito, Spring Security Test, H2 (test profile)."),
        ("Tooling", "Docker / docker-compose, Maven, npm."),
    ], widths=[1.6, 4.8])

    h1(doc, "High-Level Architecture")
    add_image(doc, os.path.join(DIAG, "architecture.png"),
              "Figure — Three-tier architecture of the Criminal Detention Portal.", width=3.9)

    out = os.path.join(HERE, "Project_Abstract_KT_CDP_019.docx")
    doc.save(out)
    return out


if __name__ == "__main__":
    print("SRS      ->", build_srs())
    print("Abstract ->", build_abstract())
