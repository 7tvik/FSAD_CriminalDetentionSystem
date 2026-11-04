#!/usr/bin/env python3
"""Generate the KT_CDP_019 Criminal Detention Portal presentation (.pptx)."""
import os
from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR

HERE = os.path.dirname(os.path.abspath(__file__))
DIAG = os.path.join(HERE, "diagrams")

NAVY = RGBColor(0x1F, 0x4E, 0x79)
STEEL = RGBColor(0x2E, 0x5E, 0x8C)
LIGHT = RGBColor(0xEE, 0xF2, 0xF7)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
DARK = RGBColor(0x22, 0x2A, 0x33)
GREY = RGBColor(0x66, 0x6F, 0x7A)

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
SW, SH = prs.slide_width, prs.slide_height
BLANK = prs.slide_layouts[6]


def add_slide():
    return prs.slides.add_slide(BLANK)


def rect(slide, x, y, w, h, fill, line=None):
    from pptx.enum.shapes import MSO_SHAPE
    shp = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, w, h)
    shp.fill.solid()
    shp.fill.fore_color.rgb = fill
    if line is None:
        shp.line.fill.background()
    else:
        shp.line.color.rgb = line
        shp.line.width = Pt(0.75)
    shp.shadow.inherit = False
    return shp


def textbox(slide, x, y, w, h, anchor=MSO_ANCHOR.TOP):
    tb = slide.shapes.add_textbox(x, y, w, h)
    tf = tb.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = anchor
    return tb, tf


def set_run(run, text, size, color=DARK, bold=False, italic=False, font="Calibri"):
    run.text = text
    run.font.size = Pt(size)
    run.font.color.rgb = color
    run.font.bold = bold
    run.font.italic = italic
    run.font.name = font


def header_band(slide, title, kicker=None):
    rect(slide, 0, 0, SW, Inches(1.15), NAVY)
    rect(slide, 0, Inches(1.15), SW, Pt(4), STEEL)
    tb, tf = textbox(slide, Inches(0.55), Inches(0.12), Inches(12.2), Inches(0.95),
                     anchor=MSO_ANCHOR.MIDDLE)
    if kicker:
        p0 = tf.paragraphs[0]
        set_run(p0.add_run(), kicker.upper(), 11, RGBColor(0xBF, 0xD4, 0xE8), bold=True)
    p = tf.add_paragraph() if kicker else tf.paragraphs[0]
    set_run(p.add_run(), title, 26, WHITE, bold=True)


def footer(slide, idx):
    tb, tf = textbox(slide, Inches(0.4), Inches(7.05), Inches(12.5), Inches(0.35))
    p = tf.paragraphs[0]
    set_run(p.add_run(), "KT_CDP_019 — Criminal Detention Portal", 9, GREY)
    tb2, tf2 = textbox(slide, Inches(11.8), Inches(7.05), Inches(1.1), Inches(0.35))
    p2 = tf2.paragraphs[0]
    p2.alignment = PP_ALIGN.RIGHT
    set_run(p2.add_run(), str(idx), 9, GREY)


def bullets(slide, items, x=Inches(0.7), y=Inches(1.5), w=Inches(12.0), h=Inches(5.2), size=18):
    tb, tf = textbox(slide, x, y, w, h)
    for i, item in enumerate(items):
        if isinstance(item, tuple):
            text, lvl = item
        else:
            text, lvl = item, 0
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.level = lvl
        p.space_after = Pt(8)
        bullet_char = "▪  " if lvl == 0 else "–  "
        set_run(p.add_run(), bullet_char, size - 2, STEEL if lvl == 0 else GREY, bold=True)
        set_run(p.add_run(), text, size if lvl == 0 else size - 2,
                DARK, bold=False)
    return tb


def centered_image(slide, path, max_w, max_h, top):
    from PIL import Image
    iw, ih = Image.open(path).size
    ratio = min(max_w / iw, max_h / ih)
    w = int(iw * ratio)
    h = int(ih * ratio)
    left = int((SW - w) / 2)
    slide.shapes.add_picture(path, left, top, width=w, height=h)


# ---------- Slide 1: Title ----------
s = add_slide()
rect(s, 0, 0, SW, SH, NAVY)
rect(s, 0, Inches(4.55), SW, Pt(3), RGBColor(0x9E, 0xC1, 0xE0))
tb, tf = textbox(s, Inches(0.8), Inches(2.2), Inches(11.7), Inches(2.3), anchor=MSO_ANCHOR.MIDDLE)
p = tf.paragraphs[0]
set_run(p.add_run(), "Criminal Detention Portal", 46, WHITE, bold=True)
p2 = tf.add_paragraph()
set_run(p2.add_run(), "A Prison-Administration System — Registers & Reports for the Effective Management of Prisons",
        18, RGBColor(0xCF, 0xDF, 0xEE), italic=True)
tb3, tf3 = textbox(s, Inches(0.8), Inches(4.8), Inches(11.7), Inches(1.6))
for txt in ["Project Code: KT_CDP_019   |   Full Stack Application Development (FSAD)",
            "Stack: React + TypeScript · Spring Boot (Java 21) · MySQL 8",
            "Academic / demonstration project — all data is fictional."]:
    pp = tf3.add_paragraph()
    set_run(pp.add_run(), txt, 13, RGBColor(0xBF, 0xD4, 0xE8))
footer(s, 1)

# ---------- Slide 2: Agenda ----------
s = add_slide()
header_band(s, "Agenda", "Presentation path")
bullets(s, [
    "Problem statement & objective",
    "Proposed solution & key features",
    "High-level architecture",
    "Technology stack",
    "Functional modules",
    "Users & role-based access control (RBAC)",
    "Database design (ER diagram)",
    "UI / navigation flow",
    "Lifecycle state machines & security highlights",
    "Demo (prototype) & conclusion",
], size=18)
footer(s, 2)

# ---------- Slide 3: Problem Statement ----------
s = add_slide()
header_band(s, "Problem Statement", "Why this project")
bullets(s, [
    "Detention facilities keep many manual, paper-based registers: case register, release diary, duty roster, interview requests, in-out movements.",
    "Manual records are slow, error-prone, hard to search, and difficult to audit.",
    "Upcoming releases and overdue court movements are easy to miss.",
    "Access to sensitive records is hard to control by role.",
    "No tamper-evident trail of who changed what, and when.",
], size=19)
footer(s, 3)

# ---------- Slide 4: Objective ----------
s = add_slide()
header_band(s, "Objective", "What we set out to build")
bullets(s, [
    "Digitise the core prison registers and the reports derived from them.",
    "Enforce strict role-based access control across every operation.",
    "Surface time-critical information: an operational dashboard and on-demand notifications.",
    "Guarantee accountability through an append-only audit trail.",
    ("Keep it academic & safe: fictional data only; no automated guilt, sentencing, or risk decisions.", 0),
], size=19)
footer(s, 4)

# ---------- Slide 5: Proposed Solution ----------
s = add_slide()
header_band(s, "Proposed Solution & Key Features", "Overview")
bullets(s, [
    "A secure, full-stack web application built as a three-tier modular monolith.",
    "React + TypeScript SPA with role-filtered screens for each register.",
    "Spring Boot backend enforcing business rules, lifecycle state machines, and permissions.",
    "MySQL database with schema managed by Flyway migrations.",
    "Stateless JWT authentication; 26 backend-enforced permissions.",
    "Automated release diary, reports, and CSV export — derived at query time.",
    "Operational dashboard (KPI cards + charts), notifications, and immutable audit log.",
], size=18)
footer(s, 5)

# ---------- Slide 6: Architecture ----------
s = add_slide()
header_band(s, "High-Level Architecture", "Three-tier modular monolith")
centered_image(s, os.path.join(DIAG, "architecture.png"), Inches(5.6), Inches(5.3), Inches(1.45))
footer(s, 6)

# ---------- Slide 7: Technology Stack ----------
s = add_slide()
header_band(s, "Technology Stack", "Tools & frameworks")
rows = [
    ("Frontend", "React 18 · TypeScript (strict) · Vite · react-router-dom v6 · axios · recharts"),
    ("Backend", "Java 21 · Spring Boot 3.3 · Spring Web · Spring Security · Spring Data JPA / Hibernate"),
    ("Database", "MySQL 8 (InnoDB) · Flyway migrations (V1 schema, V2 seed)"),
    ("Security", "Stateless JWT (HS256, jjwt) · BCrypt hashing · method-level @PreAuthorize"),
    ("API Docs", "springdoc-openapi · Swagger UI"),
    ("Testing", "JUnit 5 · Mockito · Spring Security Test · H2 (test profile)"),
    ("Tooling", "Docker / docker-compose · Maven · npm"),
]
top = Inches(1.5)
for i, (k, v) in enumerate(rows):
    y = top + Inches(0.78) * i
    rect(s, Inches(0.7), y, Inches(2.5), Inches(0.68), LIGHT, line=STEEL)
    tb, tf = textbox(s, Inches(0.8), y, Inches(2.3), Inches(0.68), anchor=MSO_ANCHOR.MIDDLE)
    set_run(tf.paragraphs[0].add_run(), k, 14, NAVY, bold=True)
    tb2, tf2 = textbox(s, Inches(3.4), y, Inches(9.2), Inches(0.68), anchor=MSO_ANCHOR.MIDDLE)
    set_run(tf2.paragraphs[0].add_run(), v, 13, DARK)
footer(s, 7)

# ---------- Slide 8: Modules ----------
s = add_slide()
header_band(s, "Functional Modules", "Seven core requirements + enhancements")
mods = [
    ("Case Register", "Prisoners, cases (FIR/sections/court), surrendered articles"),
    ("Release Diary", "Day / week / range diary + CSV; release lifecycle"),
    ("Duty Register", "Staff roster with overlap & time-order validation"),
    ("Interview Requests", "Visitor requests through a review lifecycle"),
    ("In-Out Register", "Movements in/out; overdue detection (derived)"),
    ("Court Movements", "Forward-only hearing lifecycle with outcomes"),
    ("Reports", "Day/month/upcoming releases; status statistics"),
    ("Administration (RBAC)", "Users, roles, 26 permissions, profile"),
    ("Dashboard · Notifications · Audit", "KPIs, on-demand alerts, append-only trail"),
]
cols, cw, ch = 3, Inches(4.0), Inches(1.55)
gx, gy = Inches(0.5), Inches(1.5)
for i, (title, desc) in enumerate(mods):
    r, c = divmod(i, cols)
    x = gx + c * (cw + Inches(0.17))
    y = gy + r * (ch + Inches(0.17))
    rect(s, x, y, cw, ch, LIGHT, line=STEEL)
    tb, tf = textbox(s, x + Inches(0.18), y + Inches(0.12), cw - Inches(0.36), ch - Inches(0.24))
    set_run(tf.paragraphs[0].add_run(), title, 15, NAVY, bold=True)
    pp = tf.add_paragraph()
    pp.space_before = Pt(4)
    set_run(pp.add_run(), desc, 12, DARK)
footer(s, 8)

# ---------- Slide 9: Users & Roles ----------
s = add_slide()
header_band(s, "Users & Role-Based Access Control", "5 roles · 26 permissions")
roles = [
    ("Administrator", "Full access, including user & role management"),
    ("Prison Officer", "Registers, movements, releases, interviews, duties"),
    ("Police Official", "Cases and court movements"),
    ("Government Official", "Read-only access + reports"),
    ("Auditor", "Read-only access + audit logs"),
]
top = Inches(1.6)
for i, (k, v) in enumerate(roles):
    y = top + Inches(0.92) * i
    rect(s, Inches(0.9), y, Inches(3.3), Inches(0.74), NAVY)
    tb, tf = textbox(s, Inches(1.05), y, Inches(3.1), Inches(0.74), anchor=MSO_ANCHOR.MIDDLE)
    set_run(tf.paragraphs[0].add_run(), k, 15, WHITE, bold=True)
    tb2, tf2 = textbox(s, Inches(4.5), y, Inches(8.0), Inches(0.74), anchor=MSO_ANCHOR.MIDDLE)
    set_run(tf2.paragraphs[0].add_run(), v, 14, DARK)
tb, tf = textbox(s, Inches(0.9), Inches(6.35), Inches(11.5), Inches(0.5))
set_run(tf.paragraphs[0].add_run(),
        "Authorization is enforced on the backend via @PreAuthorize — hiding UI controls is UX only.",
        13, STEEL, italic=True)
footer(s, 9)

# ---------- Slide 10: ER / DB design ----------
s = add_slide()
header_band(s, "Database Design — ER Diagram", "MySQL 8 · 17 tables · Flyway")
centered_image(s, os.path.join(DIAG, "er.png"), Inches(12.4), Inches(5.3), Inches(1.45))
footer(s, 10)

# ---------- Slide 11: UI flow ----------
s = add_slide()
header_band(s, "UI / Navigation Flow", "Planned screens")
centered_image(s, os.path.join(DIAG, "ui-flow.png"), Inches(10.5), Inches(5.3), Inches(1.45))
footer(s, 11)

# ---------- Slide 12: State machines & security ----------
s = add_slide()
header_band(s, "Lifecycle State Machines & Security", "Correctness + safety")
tb, tf = textbox(s, Inches(0.7), Inches(1.4), Inches(6.0), Inches(5.4))
set_run(tf.paragraphs[0].add_run(), "State machines (illegal transition → HTTP 422)", 16, NAVY, bold=True)
for t in ["Release: UPCOMING → READY/ON_HOLD → RELEASED/CANCELLED",
          "Interview: PENDING → APPROVED/REJECTED → SCHEDULED → COMPLETED",
          "Movement: SCHEDULED/OUT → RETURNED (+CANCELLED); overdue derived",
          "Court: forward-only SCHEDULED → … → RETURNED"]:
    p = tf.add_paragraph(); p.space_after = Pt(6)
    set_run(p.add_run(), "▪  ", 13, STEEL, bold=True)
    set_run(p.add_run(), t, 13, DARK)
tb2, tf2 = textbox(s, Inches(7.0), Inches(1.4), Inches(5.8), Inches(5.4))
set_run(tf2.paragraphs[0].add_run(), "Security highlights", 16, NAVY, bold=True)
for t in ["Stateless JWT (HS256); BCrypt password hashing",
          "Backend-enforced RBAC with 26 permissions",
          "No hard-coded secrets — env-var configuration",
          "Append-only audit log (REQUIRES_NEW transaction)",
          "Sanitized error bodies — no stack traces exposed",
          "Derived states never persisted"]:
    p = tf2.add_paragraph(); p.space_after = Pt(6)
    set_run(p.add_run(), "▪  ", 13, STEEL, bold=True)
    set_run(p.add_run(), t, 13, DARK)
footer(s, 12)

# ---------- Slide 13: Demo & conclusion ----------
s = add_slide()
header_band(s, "Prototype Demo & Conclusion", "Wrap-up")
bullets(s, [
    "Working prototype: React SPA on :5173, Spring Boot API on :8080, MySQL via Docker.",
    "Swagger UI documents and exercises every secured endpoint.",
    "Demo logins: admin / officer / police / govt / auditor (role-specific views).",
    "All 7 core functional requirements implemented end-to-end (DB → API → UI) under RBAC.",
    "Security constraints satisfied; business lifecycles covered by automated tests.",
    ("A searchable, auditable, role-aware replacement for manual prison registers.", 0),
], size=18)
footer(s, 13)

out = os.path.join(HERE, "Presentation_KT_CDP_019.pptx")
prs.save(out)
print("PPTX ->", out, "| slides:", len(prs.slides._sldIdLst))
