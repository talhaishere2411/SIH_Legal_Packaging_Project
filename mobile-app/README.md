# Legal Metrology Inspector — Android App

Kotlin + Jetpack Compose Android app for Legal Metrology enforcement officers.

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material3 |
| AR calibration | ARCore + SceneView 2.2.1 |
| Barcode detection | ML Kit Barcode Scanning |
| Networking | Retrofit 2 + OkHttp |
| Serialization | Kotlin Serialization |
| Local database | Room |
| DI | Hilt |
| Animations | Lottie Compose |
| PDF export | iText 7 Community |
| Permissions | Accompanist Permissions |

## Getting Started

### Prerequisites
1. [Android Studio Meerkat](https://developer.android.com/studio) (2024.3+)
2. Android SDK API 26+ (minSdk 26)
3. A physical Android device **with ARCore support** (recommended)
   - Check: [ARCore supported devices](https://developers.google.com/ar/devices)
   - The app degrades gracefully to coin-fallback on non-ARCore devices

### Opening the Project
1. Open Android Studio
2. Select **Open an Existing Project**
3. Navigate to `mobile-app/` and click **Open**
4. Wait for Gradle sync to complete (~3–5 min first time)

### Running on Device
1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect via USB
4. Click **▶ Run** in Android Studio

### Demo Credentials
| Role | Email | Password |
|---|---|---|
| Officer | officer@lm.gov.in | password123 |
| Controller | controller@lm.gov.in | password123 |
| Director | director@lm.gov.in | password123 |

## Project Structure

```
app/src/main/java/com/legalmetrology/inspector/
├── ar/
│   ├── ArScaleManager.kt      ← ARCore metric calibration (mm/px)
│   └── ArFrameCapture.kt      ← Full-res still capture + metadata
├── data/
│   ├── api/
│   │   ├── ApiDtos.kt         ← Request/response DTOs
│   │   └── MockInspectionApiService.kt ← MOCK: replace with Retrofit
│   ├── db/
│   │   ├── InspectionDatabase.kt
│   │   ├── InspectionDao.kt
│   │   └── entity/
│   ├── fixtures/
│   │   └── RecommendationFixtures.kt ← DEMO DATA for Module 1 (see §Packaging Advisor)
│   └── repository/
│       ├── FixtureRecommendationRepository.kt ← offline impl of the seam
│       └── RecommendationCache.kt  ← chat → report handoff (in-memory)
├── domain/
│   ├── model/
│   │   ├── Models.kt          ← Pure domain models (no framework deps)
│   │   └── Recommendation.kt  ← Module 1 contract (mirrors FastAPI JSON)
│   └── repository/
│       └── RecommendationRepository.kt ← THE SEAM: fixtures now, Retrofit later
├── di/                        ← Hilt DI modules (RepositoryModule = swap point)
├── ui/
│   ├── navigation/            ← NavGraph + Screen routes
│   ├── screens/
│   │   ├── splash/
│   │   ├── login/
│   │   ├── onboarding/        ← Package type + category selection
│   │   ├── scan/              ← AR camera + overlays (Module 2)
│   │   ├── review/            ← Photo review + submission
│   │   ├── report/            ← Compliance verdict cards
│   │   │                        + PackagingReportScreen.kt (Module 1)
│   │   ├── chat/              ← MODULE 1 home: ChatScreen, ChatViewModel, cards
│   │   ├── history/           ← Past inspections
│   │   ├── dashboard/         ← Officer home
│   │   └── ecommerce/         ← Screenshot-based listing check
│   └── theme/                 ← Colors, Typography, Theme
├── LegalMetrologyApp.kt       ← @HiltAndroidApp
└── MainActivity.kt
```

## Packaging Advisor (Module 1)

The app's primary flow is now a chat surface where a manufacturer describes what
they need to pack and gets back a **structured recommendation** — material,
barrier window, MAP gas mix, compliance notes, sustainable alternatives and
citations — rather than prose. Sign in now lands on `ChatScreen`; the full report
is a second destination reached by `requestId`.

Full design, data model and roadmap: **[`../docs/PACKAGING_ADVISOR.md`](../docs/PACKAGING_ADVISOR.md)**.

### How it is wired

```
ChatScreen ──► ChatViewModel ──► RecommendationRepository (interface)
                                        │
                                        ├── FixtureRecommendationRepository  ← bound now
                                        └── Retrofit implementation          ← later
```

`di/RepositoryModule.kt` is the **only** file to change when the FastAPI backend
lands. Every screen and ViewModel is written against the interface, so nothing
above it moves.

### Grounding rule

The model writes narrative; it never originates a number. Every claim group
carries a `SourceRef` (`CALC` / `DB` / `RAG` / `USER`), which is enforced by the
type in `domain/model/Recommendation.kt`, not left as a convention.

### Demo mode

Runs entirely offline on 5 canned commodities in `data/fixtures/RecommendationFixtures.kt`
— roasted peanuts, fresh okra, fresh mango, milk powder, ground chilli — each
exercising a different engine branch.

The demo has a small scenario matrix keyed by commodity, shelf-life bucket and
storage type. The peanut walkthrough visibly changes from PET / LDPE for up to
30 days, to metalized PET / Al / LDPE for 1–6 months, to foil / LDPE beyond six
months. Missing cells fall back to the nearest authored scenario and record that
assumption rather than inventing a number. Profile cards can be corrected for
moisture, fat, pH and storage; quick replies use a local answer bank; reports
include a side-by-side material comparison and a guided peanut demo.

**Commodity composition** comes from IFCT 2017 and fresh-produce shelf life from
ICAR-CIPHET MAP protocols. **The OTR/WVTR values and barrier targets are
engineered illustrations** for UI development — plausible but not measured. Do
not present them as verified. The report repeats this as a visible demo badge.

### Verification status

The pure Kotlin domain, fixtures, repository and chat ViewModel now compile with
Kotlin/JVM 2.4.20 and `-Werror`; a smoke check covers the peanut scenario matrix,
barrier-window coherence and follow-up bank. The full Android build is still
pending: `./gradlew assembleDebug` reaches the wrapper but this sandbox cannot
complete the Gradle 8.13 distribution download because its TLS connection to
`services.gradle.org` is blocked. Run it in a normal Android/Gradle environment
before trusting Compose or Android-specific API compatibility.

## Backend Integration

All backend calls are currently **mocked** for demo. Look for `TODO — BACKEND INTEGRATION` comments throughout the codebase.

Key integration points:
- `MockInspectionApiService.kt` → replace with real Retrofit interface
- `InspectionRepository` → add real network calls + Room caching
- `ScanScreen.kt` → wire `ArSceneView` composable (SceneView library)
- `ReviewScreen.kt` → replace mock progress with real multipart upload

## ARCore Scale Calibration

The metric conversion formula used:

```
Physical Height (mm) = pixelHeight × D_meters × 1000 / fy
```

Where:
- `D_meters` = ARCore hit-test distance from camera to surface plane
- `fy` = Y-axis focal length from `frame.camera.imageIntrinsics`
- `pixelHeight` = YOLO bounding box height in pixels

See `ArScaleManager.kt` for the full implementation.

## Legal Metrology Rules Implemented

- **Rule 7** (as amended G.S.R. 629(E), 2017): Label-area-based font size thresholds
- **Rule 6(1)**: All 9 mandatory declaration fields
- **Rule 18**: MRP format requirements
- **2017 Amendment**: E-commerce listing mandatory fields
- **Schedule II**: Standard pack sizes (biscuits, tea, edible oil, soft drinks)
