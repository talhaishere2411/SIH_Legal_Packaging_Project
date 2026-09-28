# SIH Intelligent Food Packaging System — Demo Video Script

**Target duration:** 3:45–4:00 minutes at a clear, moderately fast pace  
**Primary focus:** Module 1 — Packaging Advisor  
**Secondary focus:** Module 2 — Label and Layout Validation

> **Presenter note:** The architecture diagram shows the target integrated architecture. The current Android demo already implements the Module 1 user experience offline with curated fixture data. The PostgreSQL database, FastAPI backend, live Retrieval-Augmented Generation service and supplier integrations are the next production phase; they should not be presented as live in this build.

---

## 0:00–0:15 — Opening: the problem

**Show:** Architecture diagram, then the app logo.

**Say:**

“Small food businesses often know their product and target shelf life, but not the exact packaging barrier, film structure, modified atmosphere or regulation they need. Our Intelligent Food Packaging System turns that packaging problem into a guided, evidence-backed workflow for Micro, Small and Medium Enterprises — MSMEs — farmers and food startups.”

---

## 0:15–0:55 — Explain the architecture diagram

**Show:** Point to the left side, then the orange Module 1 block.

**Say:**

“On the left, the MSME uses an Android native application built with Kotlin and Jetpack Compose. The user describes the commodity, desired shelf life and storage conditions in plain language.

“The orange block is Module 1, the Packaging Advisor. It has four stages. First is parameter intake: the system identifies the commodity, moisture, fat content, pH and storage conditions. Second is barrier calculation: it determines the required Oxygen Transmission Rate, or OTR, Water Vapour Transmission Rate, or WVTR, and respiration thresholds for fresh produce. Third is material selection: it recommends the layer structure, thickness, sealing layer and Modified Atmosphere Packaging, or MAP, gas composition. Fourth is sustainability and compliance: it provides cheaper and more sustainable alternatives with citations to Indian food-packaging requirements.

“The production architecture connects exact material values to PostgreSQL — an open-source relational database — through SQLModel, an Object-Relational Mapping, or ORM, layer. Regulatory evidence comes through RAG, meaning Retrieval-Augmented Generation. Crucially, the language model explains the recommendation; it must not invent a number. Numeric barriers come from calculations or curated data, and legal limits come from rules and citations.”

---

## 0:55–1:15 — Briefly explain Module 2

**Show:** Point to the green block at the bottom of the diagram.

**Say:**

“The green block is Module 2, the original Legal Metrology inspection workflow. An inspector captures a package image using the Android camera and ArUco fiducial marker. ArUco is the computer-vision marker used for scale calibration. A You Only Look Once, or YOLOv8, model detects mandatory label fields. Optical Character Recognition, or OCR, extracts text and numbers using PaddleOCR, with Qwen2.5-VL — a vision-language model — as a fallback. A Legal Metrology rule engine checks the extracted values, and low-confidence cases go to a human review queue before a report is finalized.”

---

## 1:15–1:35 — Start the app

**Show:** Launch the application, splash screen and login screen.

**Say:**

“Now let us see the user experience. The app opens through a splash screen and a simple officer login. After login, the Packaging Advisor chat is the primary home screen. The current demonstration is fully offline, so it does not require a backend connection after the Android dependencies are installed.”

**On screen:** Use the demo credentials if required:

```text
Email: officer@lm.gov.in
Password: password123
```

---

## 1:35–2:00 — Natural-language intake and profile inference

**Show:** Enter: `Best packaging for mango in transit`.

**Say:**

“The user does not need to know packaging terminology. I can simply type, ‘Best packaging for mango in transit.’ The assistant identifies fresh mango and presents a structured product profile: moisture, fat content, pH, respiration rate and storage assumption. The source badge distinguishes database information from calculated information. In the production system, commodity composition is grounded in the Indian Food Composition Tables, or IFCT, from the Indian Council of Medical Research–National Institute of Nutrition, and fresh-produce behaviour is grounded in Indian Council of Agricultural Research, or ICAR, and the Central Institute of Post-Harvest Engineering and Technology, or CIPHET, sources.”

---

## 2:00–2:25 — Editable profile and scenario reasoning

**Show:** Tap `Tap to correct profile values before re-running`.

**Say:**

“If the manufacturer knows that the inferred values are different, the profile card is editable. I can correct moisture, fat, pH and storage, then select ‘Re-run recommendation.’ The corrected profile is marked as user input, and the assumptions are shown rather than hidden.

“The offline demo also includes a scenario matrix. For example, roasted peanuts use different authored scenarios for up to 30 days, one to six months and beyond six months. This changes the recommended structure instead of quietly answering a different shelf-life question. Missing scenarios fall back to the nearest authored case and explain that limitation.”

---

## 2:25–3:15 — Recommendation card and full report

**Show:** Open the recommendation card and then `View full report`.

**Say:**

“The recommendation card gives the user the material structure, barrier requirements, Modified Atmosphere Packaging gases and any compliance items needing attention. The full report expands this into practical decisions.

“First is the recommended structure — for example, a polyethylene terephthalate and aluminium laminate with a low-density polyethylene seal layer. PET means Polyethylene Terephthalate; LDPE means Low-Density Polyethylene. Next are the OTR and WVTR targets. Every figure includes its test temperature and relative humidity because a barrier number without test conditions is incomplete.

“For fresh produce, the report shows the minimum and maximum barrier window, MAP oxygen, carbon dioxide and nitrogen percentages, storage temperature and micro-perforation guidance. For dry products, it explains whether oxidation or moisture is the dominant failure mode.

“The compliance section connects the pack to Food Safety and Standards Authority of India, or FSSAI, requirements and relevant Indian Standards, or IS codes. The alternatives section compares cheaper, more sustainable and higher-barrier options. The assumptions and citations make the result auditable, while the visible badge reminds us that the current barrier values are engineering illustrations, not measured supplier data.

“The user can also ask follow-up questions through the answer bank, use ‘Play the peanut demo’ for a guided walkthrough, and open the side-by-side comparison of recommended, lower-cost and sustainable materials.”

---

## 3:15–3:40 — How MSMEs gain practical access to packaging help

**Show:** Report, comparison screen and citation section.

**Say:**

“This is solution-oriented rather than just informational. An MSME can describe its product, correct the important assumptions, select a shelf-life scenario and use the report as a specification sheet when contacting a packaging converter. Instead of asking for ‘a good pouch,’ the manufacturer can request a defined layer structure, thickness, OTR, WVTR and seal requirement.

“The report also tells the manufacturer what to verify: finished-pack migration testing, virgin food-contact layers, suitable printing inks and the applicable Indian Standards. The next step is to send the specification to a converter and, where required, an NABL laboratory — the National Accreditation Board for Testing and Calibration Laboratories — before committing to a production run. Future backend and supplier integrations can turn this into direct quotations and material availability; the current demo establishes the decision-support layer first.”

---

## 3:40–3:58 — Short Module 2 demonstration

**Show:** `Validate my printed design` entry point and the existing inspection flow.

**Say:**

“Finally, from the packaging report, the manufacturer or inspector can move to label validation. The Android camera captures the printed design beside the ArUco scale marker. Computer vision detects mandatory declarations, OCR reads the text, and the rule engine checks legal metrology requirements. Low-confidence findings are sent for human review rather than being treated as automatic violations.”

---

## 3:58–4:00 — Closing

**Show:** Architecture diagram and app report together.

**Say:**

“Module 1 helps an MSME choose and justify the right pack. Module 2 helps verify the printed label. Together, they connect packaging science, Indian compliance and practical manufacturing decisions in one mobile-first workflow.”
