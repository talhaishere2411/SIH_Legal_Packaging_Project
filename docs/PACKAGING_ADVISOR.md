# Packaging Advisor — Module 1

> **Status: offline demo skeleton.** Frontend contract, fixture repository and
> Packaging Advisor UI exist; there is no backend or curated materials dataset.
> The pure Kotlin domain/fixture/chat logic compiles with Kotlin/JVM, while the
> full Android build is still pending (see [Verification status](#11-verification-status)).
> Everything below that is marked *planned* is design, not working code.

---

## 1. The pivot

This repository started as a Legal Metrology inspection tool (SIH26034). The
project direction changed, and the repo now carries two modules with their
priority inverted:

| Module | What it is | Status |
|---|---|---|
| **Module 1 — Packaging Advisor** | A RAG-driven consultant that tells a food processor which packaging material and specification to use for a given commodity. | **Primary system.** Skeleton built. |
| **Module 2 — ArUco label validator** | The original YOLO / PaddleOCR / Qwen2.5-VL pipeline, repurposed from scale calibration to label & layout validation (L1 / L2 / L3 tri-tier verdict). | **Side feature.** Pre-existing code, untouched. |

The two share the app shell, login, navigation and theme. Module 1 is where new
work goes.

## 2. Problem statement (as given)

**Inputs:** commodity type · moisture content · oil/fat content · pH ·
respiration rate · desired shelf life · storage temperature · relative humidity ·
transportation conditions · storage type (ambient / chilled / frozen).

**Outputs:**
- Material recommendation from: LDPE, HDPE, PET, metalized films, aluminium foil
  laminates, biodegradable films, breathable films.
- Specification: OTR, WVTR, film thickness, sealability, gas permeability,
  mechanical strength, MAP suitability.
- For fresh produce: respiration rate drives breathable / micro-perforated films
  plus a target gas composition.
- Optional: shelf-life prediction, sustainability analysis, cost optimization,
  QR traceability, eco-friendly alternatives.

**Goal:** a decision-support tool for the food processing and packaging industry —
usable by industries, farmers, startups and researchers.

## 3. What exists today

Everything currently lives in the Android app and runs fully offline against
fixtures. There is no server yet.

| Piece | File | State |
|---|---|---|
| Response contract | `domain/model/Recommendation.kt` | Done — pure Kotlin, mirrors the JSON the FastAPI service will emit |
| Repository seam | `domain/repository/RecommendationRepository.kt` | Done — 3 methods |
| Fixture implementation | `data/repository/FixtureRecommendationRepository.kt` | Done — keyword/alias matching + canned responses |
| Demo data | `data/fixtures/RecommendationFixtures.kt` | Done — 5 commodities, 9 materials |
| DI binding | `di/RepositoryModule.kt` | Done — **the single swap point** for Retrofit |
| Session handoff | `data/repository/RecommendationCache.kt` | Done, in-memory — Room is a TODO |
| Chat UI | `ui/screens/chat/{ChatScreen,ChatViewModel,ChatComponents}.kt` | Done |
| Report UI | `ui/screens/report/{PackagingReportScreen,PackagingReportViewModel}.kt` | Done |
| Navigation | `ui/navigation/{Screen,AppNavGraph}.kt` | Done — Chat is the post-login start destination |
| Architecture | `docs/architecture-v2.svg` | Done |
| FastAPI backend | — | **Not started** |
| Curated materials DB | — | **Not started** |
| pgvector corpus | — | **Not started** |

## 4. Architecture at a glance

Full diagram: [`docs/architecture-v2.svg`](./architecture-v2.svg).

The central design decision is that **not everything should be RAGged**. Three
stores, each doing the job it is actually good at:

### Store A — Postgres (structured, curated, NOT RAG)

Tables: `commodities` (CIPHET's ~30, extended; moisture, fat, pH, respiration) ·
`materials` (OTR, WVTR, thickness, seal, strength, cost) · `structures`
(laminate stacks + measured barriers) · `is_materials_map` (IS code ↔ polymer ↔
FSSAI schedule) · `fssai_schedule_iv` (food category → permitted materials) ·
`map_recommendations` (O₂/CO₂/N₂ %, temp, shelf-life days) · `sml_limits` ·
`users` · `sessions` · `reports`.

These are exact-match lookups on **measured** values. Embeddings add nothing
here, and a language model asked to recall an OTR will invent one.

### Store B — pgvector (RAG)

Corpus: FSSAI (Packaging) Regulations 2018 · FSSAI Labelling & Display 2020 ·
BIS Sustainable Packaging Handbook · CPCB SOP / IS-ISO 17088 · IS 9845:1998 ·
selected MAP and respiration literature (flagged as international, not Indian
regulation).

Chunking is **clause-aware**: schedule rows stay whole, each chunk carries a
breadcrumb prefix. Retrieval is cosine + Postgres FTS (`tsvector`) fused with
RRF; pre-filter on `metadata.is_codes[]` when the query names a material.
Total corpus is ~400–600 chunks — retrieval buys **citation grounding**, not
scale, so this deliberately has no reranker.

### Store C — Rules (versioned YAML in-repo, loaded at boot, unit-tested)

OML 60 mg/kg or 10 mg/dm² (IS 9845) · recycled plastics prohibited for food
contact · printing inks → IS 15495 · pigments → IS 9833 · packaged-water
carve-out · food-category taxonomy borrowed from IS 9845's seven groups.

**A model must never be the source of a legal limit.** Limits are data, loaded
from a file a human reviews, and enforced in code.

## 5. The grounding contract

> **The LLM never generates a number.**

Every output field carries a `SourceRef` with `kind ∈ {calc, db, rag, user}`.
The model writes narrative; it never originates a figure. Response validation
rejects any unsourced numeric claim before the client sees it. This is encoded
in the type system — see `SourceKind` and `SourceRef` in `Recommendation.kt` —
not left as a convention.

## 6. Pipeline (planned, 8 steps)

Orchestrated by an agent loop that does slot filling and returns **exactly one**
clarifying question when a critical slot is empty.

1. **Intent parser** — LLM emitting schema-constrained JSON, never free-form.
   Unmatched slots stay `null` rather than being guessed.
2. **Profile lookup** — Postgres (IFCT 2017 + CIPHET). Shown to the user as an
   editable card.
3. **Barrier calculator** — pure Python, **no model**. This is the real IP; for
   fresh produce it works from the respiration equation.
4. **Candidate selector** — SQL filter, ranked by barrier margin → cost →
   sustainability. Every row carries its test conditions.
5. **Compliance validator** — hard filters in code; clause text and citation
   pulled from pgvector.
6. **MAP module** — Postgres; fresh produce only, skipped for dry/ambient/frozen.
7. **Sustainability module** — Postgres flags + RAG narrative.
8. **Report composer** — LLM, narrative only, emitting typed JSON.

## 7. Response contract

`RecommendationResponse` is the whole payload — one instance renders the report
screen, and the chat shows a condensed card built from the same object.

```
RecommendationResponse
├── requestId        String
├── commodity        CommodityProfile    ← auto-inferred, user-correctable
├── assumptions      List<String>        ← what the engine assumed for blank slots
├── recommended      PackagingMaterial   ← structure, OTR, WVTR, thickness, seal,
│                                          tensile, recyclable, compostable,
│                                          IS codes, cost index, provenance
├── barriers         BarrierSpec         ← target window, incl. optional MINIMUMS
├── map              MapRecommendation?  ← O₂/CO₂/N₂ %, temp, perforation
├── compliance       List<ComplianceNote>
├── alternatives     List<Alternative>   ← CHEAPER / SUSTAINABLE / HIGHER_BARRIER
├── citations        List<Citation>
├── narrative        String              ← model prose; refers only to numbers
│                                          already present elsewhere in the object
└── followUps        List<String>        ← quick-reply chips
```

Two design points worth calling out:

- **`BarrierSpec` has minimums, not just ceilings.** A film that is too tight
  drives respiring produce anaerobic just as fast as one that is too loose lets
  it spoil, so fresh produce is specified as a *window*.
- **OTR/WVTR always carry test conditions.** Every barrier figure is meaningless
  without temperature, RH and thickness, so `BarrierSpec` and `PackagingMaterial`
  both carry `(value, unit, temp_C, rh_pct)` — and `PackagingMaterial` additionally
  carries thickness and structure.

## 8. Demo mode

The app ships with 5 canned commodities, chosen to exercise every engine branch:

| Commodity | Failure mode exercised |
|---|---|
| Roasted peanuts | Dry, high fat, ambient → high O₂ barrier (oxidation-limited) |
| Fresh okra | Respiring → MAP + micro-perforation |
| Fresh mango | Climacteric → MAP with a chilling-injury caveat |
| Whole milk powder | Moisture-sensitive → very low WVTR (caking) |
| Ground chilli powder | Volatile/aroma + light retention at ambient |

Example prompts on the empty chat (not a blank box — this matters for demo
credibility):

```
How should I package roasted peanuts for 6 months?
I need to ship fresh okra — what film?
Best packaging for mango in transit
Packaging for milk powder, 12 month shelf life
What film keeps chilli powder from fading?
```

Shelf life and storage type typed into the message are parsed out of the text and
resolved through a local scenario matrix keyed by commodity, `SHORT` (up to one
month), `MEDIUM` (one to six months) and `LONG` (beyond six months), plus storage
type. The peanut walkthrough changes from PET / LDPE at 30 days, to metalized
PET / Al / LDPE at six months, to foil / LDPE beyond six months. If a cell is not
authored, the nearest scenario is shown with an explicit assumption note rather
than inventing a number. The profile card can be edited for moisture, fat, pH and
storage, which re-runs the selected fixture scenario. Follow-up chips use a
commodity-scoped answer bank; the report also exposes a side-by-side comparison
view and a guided peanut demo.

## 9. Data honesty

Read this before quoting any number from the demo build.

- **Commodity composition** (moisture, fat, pH) is drawn from published Indian
  sources, principally **IFCT 2017** (ICMR-NIN). Realistic.
- **Fresh-produce shelf life** comes from **ICAR-CIPHET**'s published MAP
  protocols. Real trial results.
- **Barrier targets and material OTR/WVTR values are ENGINEERED ILLUSTRATIONS**
  for UI development. Plausible, not measured, **not** from supplier datasheets.

The same warning is repeated in the `DATA HONESTY` header at the top of
`RecommendationFixtures.kt`. Fixture-coherence rule for future edits: assert
`recommended.otr ∈ [otrTargetMin ?: 0, otrTargetMax]` (and likewise for WVTR),
and make sure the prose agrees with the numbers.

## 10. Hard regulatory constants

Recorded here so they survive into the rules store. **Re-verify each against the
corpus at ingestion time** — do not treat this table as settled.

| Rule | Value |
|---|---|
| Overall migration limit | 60 mg/kg **or** 10 mg/dm², IS 9845, with no visible colour migration |
| Specific migration limits | Table 1 of the FSSAI (Packaging) Regulations, 2018 |
| Recycled plastics | Prohibited for food contact, including carry bags |
| Printing inks | IS 15495 |
| Pigments / colourants | IS 9833 |
| Packaged drinking water | Colourless, transparent, tamper-proof PE (IS 10146) / PVC (IS 10151) / PET-PBT (IS 12252) / PP (IS 10910) only |

**FSSAI Schedule III — polymer → IS number** (hand-built; do *not* fetch BIS PDFs):

PE→10146 · PS→10142 · PVC→10151 · PP→10910 · Ionomer→11434 · EAA→11704 ·
PET/PBT→12252 · Nylon 6→12247 · EVA→13601 · EMAA→13576 · PC→14971 · flexible
laminates for edible oils→14636 · PET/PBT moulding→13193 · PE films→2508 ·
LLDPE films→14500 · HDPE→7328 · melamine→14999 · LDPE films→2508 · blow-moulded
containers→7408 · stretch cling films→14995

Schedule IV is an FSSAI-published *food category → suitable materials* lookup,
and is explicitly flagged "indicative… not restricting the use of any other
packaging material complying with the specified standards." That is the correct
legal posture for the engine: suggest, cite, never forbid what the regulation
itself does not forbid.

## 11. Verification status

**Verification is partial.** The pure Kotlin domain, fixture repository and chat
ViewModel now compile with Kotlin/JVM 2.4.20 and `-Werror`. A smoke check covers
the scenario matrix, barrier-window coherence and follow-up answer-bank lookup.

The requested `./gradlew assembleDebug` was also attempted. The wrapper starts,
but this sandbox cannot complete the Gradle 8.13 distribution download because
its TLS connection to `services.gradle.org` is blocked. Android dependencies were
therefore not resolved, so Compose, Hilt and Android resource compatibility still
need a normal Android/Gradle environment.

The earlier static pass over the whole Kotlin tree recorded **508 checks**:

| Check | Sites |
|---|---|
| Constructor named arguments resolve to a real field | 170 |
| Enum member references exist | 90 |
| Function call sites resolve to a declared function | 125 |
| Property accesses exist on the bound receiver type | 94 |
| `Modifier.weight` calls sit inside a `Row`/`Column` scope | 29 |

The checker was validated by injecting 5 deliberate faults into a throwaway file;
all 5 were caught. One residual flag
(`MockInspectionApiService.kt:91 request.email`) is a known tool artifact, not a
code defect.

**What still needs the first Android build:**

- Compose API parameter names and overload resolution against BOM 2024.12.01
- Hilt generated binding validation
- Android resource, manifest and CameraX/OpenCV dependency resolution

**Run this first:**

```bash
cd mobile-app && ./gradlew assembleDebug
```

(The Gradle wrapper was missing from the repo until this branch — `gradlew`,
`gradlew.bat`, `gradle-wrapper.jar` and a `.gitattributes` handling CRLF were
added precisely so a fresh clone can build.)

## 12. Known gaps

Small, and worth closing before the backend work starts:

- `BarrierSpec.isTwoSided` and `CommodityProfile.isFreshProduce` are defined but
  never read — the UI branches on `recommendation.map != null` instead. Either
  wire them in or delete them.
- `RecommendationCache` is in-memory; process death loses saved reports. Room
  replacement is the TODO.
- Retrofit is declared in Gradle but unused — no networking code exists yet.
- No history / saved-reports screen, no profile editing (the profile card renders
  but is not yet editable).

## 13. Roadmap

1. **Curate the materials dataset** — real OTR/WVTR from supplier datasheets,
   each row with `(value, unit, temp_C, rh_pct, thickness_µm, test_method,
   source)`. This is the highest-value item; every recommendation is downstream
   of it.
2. **FastAPI skeleton** — serve the same `RecommendationResponse` shape, with the
   provider-agnostic LLM interface stubbed behind it.

   ```
   backend/app/
   ├── main.py
   ├── api/v1/{auth,chat,recommend,commodities,validate_label}.py
   ├── core/{config,security}.py
   ├── db/{session,models}.py
   ├── schemas/
   ├── services/{orchestrator,intent,profile,barrier,selector,
   │             compliance,map_module,report}.py
   ├── services/rag/{ingest,retrieve}.py
   ├── data/rules/*.yaml
   └── data/seed/*.csv
   ```
3. **Clause-aware ingestion** of the FSSAI compendia into pgvector.
4. **Swap the mobile repo to Retrofit** — one edit in `di/RepositoryModule.kt`.
5. Module 2 label validator, repurposed from the existing ArUco pipeline.

## 14. Decisions on record

- **Mobile-first**, not web-first. Backend API client comes later.
- **Python backend** (FastAPI).
- **Commodity scope** = CIPHET's ~30 published commodities, not the ~200-item
  international set. (Only ~18 have been verified so far.)
- **The LLM is abstracted** behind a provider-agnostic interface with a stub
  implementation for now; the provider is chosen later and never leaks to the
  client.
- **Scaffold order was explicitly: mobile UI + fixtures only.** No backend, no
  network, all local.
