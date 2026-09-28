package com.legalmetrology.inspector.domain.model

// ============================================================
// PACKAGING ADVISOR — DOMAIN MODELS
//
// These are pure Kotlin data classes with no framework dependency.
// They mirror the JSON contract the FastAPI backend will emit, so the
// fixture-backed build and the networked build are interchangeable:
// swapping FixtureRecommendationRepository for a Retrofit-backed
// implementation should require no UI changes.
//
// DESIGN RULE — GROUNDING:
// Every numeric claim carries a SourceRef. The model is never the origin
// of a number; it only writes prose. Provenance is tracked per logical
// claim group (a BarrierSpec, a ComplianceNote) rather than per scalar,
// which keeps the contract readable without weakening traceability.
// ============================================================

// --- Provenance ------------------------------------------------------------

/**
 * Where a claim came from. Serialised as a flat triple so it survives
 * the JSON round-trip without a polymorphic adapter.
 */
enum class SourceKind(val displayName: String) {
    /** Produced by deterministic arithmetic on the server. */
    CALC("Calculated"),
    /** Exact-match lookup against a curated Postgres table. */
    DB("Database"),
    /** Retrieved text from the regulatory corpus in pgvector. */
    RAG("Regulation"),
    /** Supplied or corrected by the user. */
    USER("Your input")
}

data class SourceRef(
    val kind: SourceKind,
    /** Machine locator, e.g. "materials#metpet12", "fssai-pkg-2018#sch3-i7". */
    val detail: String,
    /** Human label, e.g. "FSSAI Packaging Regs 2018 · Schedule III item 7". */
    val label: String
)

// --- Commodity -------------------------------------------------------------

/**
 * Food groups used to drive barrier logic.
 *
 * NOTE: these are modelled on the seven food-simulant categories in
 * IS 9845:1998 (determination of overall migration). The mapping to the
 * standard's exact wording still needs to be confirmed during corpus
 * ingestion — treat the enum as provisional.
 */
enum class FoodGroup(val displayName: String, val blurb: String) {
    DRY_LOW_FAT("Dry · low fat", "Moisture-sensitive, low rancidity risk"),
    DRY_HIGH_FAT("Dry · high fat", "Rancidity is the dominant failure mode"),
    FATTY_SURFACE("Fatty / oil-bearing", "Free fat at the surface, high O₂ risk"),
    AQUEOUS_ACIDIC("Aqueous · acidic", "pH below 4.5, migration-sensitive"),
    AQUEOUS_NEUTRAL("Aqueous · near-neutral", "pH at or above 4.5"),
    ALCOHOLIC("Alcoholic", "Ethanol is an aggressive simulant"),
    FRESH_PRODUCE("Fresh produce · respiring", "Still respiring — needs a breathable film")
}

enum class StorageType(val displayName: String, val typicalTempC: String) {
    AMBIENT("Ambient", "25–35 °C"),
    CHILLED("Chilled", "0–8 °C"),
    FROZEN("Frozen", "≤ −18 °C")
}

/**
 * The auto-inferred product profile. This is what the assistant shows the
 * user for correction before recommending — most small manufacturers do
 * not know their own water activity, so inference is the core UX feature.
 */
data class CommodityProfile(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    /** e.g. "roasted", "fresh whole", "spray-dried powder". */
    val form: String? = null,
    val foodGroup: FoodGroup,
    val moisturePct: Double? = null,
    val fatPct: Double? = null,
    val ph: Double? = null,
    /** mg CO₂/kg/h — only meaningful for [FoodGroup.FRESH_PRODUCE]. */
    val respirationRate: Double? = null,
    val respirationTempC: Int? = null,
    val chillingSensitive: Boolean = false,
    val defaultStorage: StorageType = StorageType.AMBIENT,
    val source: SourceRef
) {
    val isFreshProduce: Boolean get() = foodGroup == FoodGroup.FRESH_PRODUCE
}

// --- Barrier requirements --------------------------------------------------

/**
 * The barrier window the packaging must sit inside.
 *
 * Produced by the deterministic barrier calculator on the server — never
 * by the model. Test conditions are mandatory because OTR and WVTR are
 * meaningless without temperature and relative humidity.
 */
data class BarrierSpec(
    val otrTargetMax: Double,
    /**
     * Lower bound, set only for respiring produce. A film that is too
     * tight drives the pack anaerobic just as surely as one that is too
     * loose lets the product spoil, so fresh produce is specified as a
     * window rather than a ceiling. Null means "lower is always better".
     */
    val otrTargetMin: Double? = null,
    val otrUnit: String = "cm³/m²·day",
    val otrTempC: Int = 23,
    val otrRhPct: Int = 0,
    val wvtrTargetMax: Double,
    val wvtrTargetMin: Double? = null,
    val wvtrUnit: String = "g/m²·day",
    val wvtrTempC: Int = 38,
    val wvtrRhPct: Int = 90,
    /** Plain-language explanation of how the target was reached. */
    val rationale: String,
    val source: SourceRef
) {
    /** True when this is a window rather than a ceiling. */
    val isTwoSided: Boolean get() = otrTargetMin != null || wvtrTargetMin != null
}

// --- Materials -------------------------------------------------------------

data class PackagingMaterial(
    val id: String,
    val name: String,
    /** Human-readable layer stack, e.g. "12 µ PET / 9 µ Al / 70 µ LDPE". */
    val structure: String,
    val otr: Double,
    val otrTempC: Int,
    val otrRhPct: Int,
    val wvtr: Double,
    val wvtrTempC: Int,
    val wvtrRhPct: Int,
    val thicknessUm: String,
    val sealability: String,
    val tensileMpa: Double? = null,
    val recyclable: Boolean = false,
    val compostableIs17088: Boolean = false,
    /** Applicable Indian Standard codes, e.g. ["IS 12252", "IS 2508"]. */
    val isCodes: List<String> = emptyList(),
    /** 1 = cheapest, 5 = most expensive. */
    val costIndex: Int = 3,
    val source: SourceRef
)

// --- Modified atmosphere ---------------------------------------------------

data class MapRecommendation(
    val o2Pct: String,
    val co2Pct: String,
    val n2Pct: String,
    val storageTempC: String,
    val perforation: String? = null,
    val expectedShelfLifeDays: Int? = null,
    val notes: String,
    val source: SourceRef
)

// --- Compliance ------------------------------------------------------------

enum class ComplianceStatus(val displayName: String) {
    MET("Requirement met"),
    ACTION_REQUIRED("Action required"),
    PROHIBITED("Prohibited"),
    NOT_APPLICABLE("Not applicable")
}

data class ComplianceNote(
    val requirement: String,
    val limit: String,
    val status: ComplianceStatus,
    val isReference: String? = null,
    val detail: String? = null,
    val source: SourceRef
)

// --- Alternatives ----------------------------------------------------------

enum class AlternativeKind(val displayName: String) {
    CHEAPER("Lower cost"),
    SUSTAINABLE("More sustainable"),
    HIGHER_BARRIER("Higher barrier")
}

data class Alternative(
    val kind: AlternativeKind,
    val material: PackagingMaterial,
    val tradeoff: String
)

// --- Citations -------------------------------------------------------------

data class Citation(
    val id: String,
    val title: String,
    /** Clause, schedule or page, e.g. "Schedule III, item 7". */
    val locator: String,
    val url: String? = null
)

// --- The response ----------------------------------------------------------

/**
 * The complete recommendation payload. One instance renders the whole
 * report screen; the chat shows a condensed card with the same object.
 */
data class RecommendationResponse(
    val requestId: String,
    val commodity: CommodityProfile,
    /** Assumptions the engine made when the user left a slot blank. */
    val assumptions: List<String>,
    val recommended: PackagingMaterial,
    val barriers: BarrierSpec,
    val map: MapRecommendation? = null,
    val compliance: List<ComplianceNote>,
    val alternatives: List<Alternative>,
    val citations: List<Citation>,
    /**
     * Prose written by the model. It explains and justifies, but every
     * number it refers to already exists elsewhere in this object.
     */
    val narrative: String,
    /** Follow-up prompts rendered as quick-reply chips. */
    val followUps: List<String> = emptyList()
)

/** Input to the recommendation engine. */
data class RecommendationRequest(
    val query: String,
    val commodityId: String? = null,
    val shelfLifeDays: Int? = null,
    val storage: StorageType? = null,
    /** User overrides applied on top of the inferred profile. */
    val overrides: Map<String, String> = emptyMap()
)

// --- Chat ------------------------------------------------------------------

enum class ChatRole { USER, ASSISTANT }

/**
 * A single chat turn. The assistant message optionally attaches a
 * [CommodityProfile] or a [RecommendationResponse], which is what makes
 * this a structured report rather than a wall of text.
 */
data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val text: String,
    val profile: CommodityProfile? = null,
    val recommendation: RecommendationResponse? = null,
    val suggestions: List<String> = emptyList()
)
