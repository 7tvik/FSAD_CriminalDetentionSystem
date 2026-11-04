# Assignment Deliverables — KT_CDP_019 Criminal Detention Portal

Files to upload individually to TAXILA (each student uploads their own copy).

| # | Required element | File to upload |
|---|------------------|----------------|
| 1 | **DB Design — ER Diagram** (schemas) | `diagrams/er.png` |
| 2 | **Architecture Diagram** (high level) | `diagrams/architecture.png` |
| 3 | **SRS Doc** (functional requirements) | `SRS_KT_CDP_019.docx` |
| 4 | **UI Design — Flow Diagram** of UIs | `diagrams/ui-flow.png` |
| 5 | **Abstract** (problem/objective/solution/tech/modules/users/roles) | `Project_Abstract_KT_CDP_019.docx` |
| 6 | **PPT** (presentation path) | `Presentation_KT_CDP_019.pptx` |
| 7 | **Prototype** (optional) | The running app — React `:5173`, API `:8080`, Swagger `:8080/swagger-ui/index.html` |

## Notes
- The SRS and the PPT **already embed** the three diagrams, so items 1, 2, and 4 also live inside those documents. The standalone PNGs are provided so you can upload them as separate items too.
- Open the `.docx` / `.pptx` in Microsoft Word / PowerPoint, review, and (if the portal prefers PDF) export to PDF via File → Save As / Export.

## Regenerating (optional)
The documents are generated from source so they can be rebuilt after edits:
- Diagrams: edit `diagrams/*.mmd`, then `mmdc -i <file>.mmd -o <file>.png -b white -s 2`
- Docs:  `.venv/bin/python build_docs.py`
- Slides: `.venv/bin/python build_ppt.py`

`.venv/`, `build_*.py`, and `*.mmd` are build tooling — **do not upload** them.
