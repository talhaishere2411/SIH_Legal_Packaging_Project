package com.legalmetrology.inspector.data.fixtures

import com.legalmetrology.inspector.domain.model.Alternative
import com.legalmetrology.inspector.domain.model.AlternativeKind
import com.legalmetrology.inspector.domain.model.BarrierSpec
import com.legalmetrology.inspector.domain.model.Citation
import com.legalmetrology.inspector.domain.model.CommodityProfile
import com.legalmetrology.inspector.domain.model.ComplianceNote
import com.legalmetrology.inspector.domain.model.ComplianceStatus
import com.legalmetrology.inspector.domain.model.FoodGroup
import com.legalmetrology.inspector.domain.model.MapRecommendation
import com.legalmetrology.inspector.domain.model.PackagingMaterial
import com.legalmetrology.inspector.domain.model.RecommendationResponse
import com.legalmetrology.inspector.domain.model.SourceKind
import com.legalmetrology.inspector.domain.model.SourceRef
import com.legalmetrology.inspector.domain.model.StorageType

// ============================================================
// DEMO FIXTURES
//
// These drive the hardcoded demo build. Each commodity exercises a
// different branch of the recommendation engine:
//
//   peanut-roasted  → oxidation-limited (high fat, long ambient life)
//   okra-fresh      → respiration-limited (MAP + micro-perforation)
//   mango-fresh     → MAP with a chilling-injury caveat
//   milk-powder     → moisture-limited (caking, very low WVTR)
//   chilli-powder   → volatile/aroma retention at ambient
//
// ── DATA HONESTY ──────────────────────────────────────────────
// Commodity composition (moisture, fat, pH) is drawn from published
// Indian sources — principally IFCT 2017 (ICMR-NIN) — and is realistic.
// Shelf-life figures for fresh produce come from ICAR-CIPHET's published
// MAP protocols and are real trial results.
//
// Barrier targets and material property values are ENGINEERED
// ILLUSTRATIONS for UI development. They are plausible but are NOT yet
// curated from supplier datasheets. Do not present the OTR/WVTR numbers
// as verified. Replace the `dbMaterials` block below with the curated
// dataset before this is used for anything real.
// ──────────────────────────────────────────────────────────────
// ============================================================

// --- Shared source references ---------------------------------------------

private val SRC_IFCT = SourceRef(
    SourceKind.DB, "ifct2017", "IFCT 2017 · ICMR–National Institute of Nutrition"
)
private val SRC_CIPHET = SourceRef(
    SourceKind.DB, "ciphet-map", "ICAR-CIPHET MAP protocols"
)
private val SRC_CALC = SourceRef(
    SourceKind.CALC, "barrier.oxidative", "Barrier model · oxidation + moisture"
)
private val SRC_MAP_CALC = SourceRef(
    SourceKind.CALC, "map.respiration", "MAP model · respiration balance"
)
private val SRC_FSSAI_PKG = SourceRef(
    SourceKind.RAG, "fssai-pkg-2018", "FSSAI (Packaging) Regulations, 2018"
)
private val SRC_FSSAI_SCH3 = SourceRef(
    SourceKind.RAG, "fssai-pkg-2018#sch3", "FSSAI Packaging Regs 2018 · Schedule III"
)
private val SRC_FSSAI_SCH4 = SourceRef(
    SourceKind.RAG, "fssai-pkg-2018#sch4", "FSSAI Packaging Regs 2018 · Schedule IV"
)
private val SRC_BIS_HB = SourceRef(
    SourceKind.RAG, "bis-sustain-hb", "BIS Handbook on Sustainable Packaging"
)

// --- Curated citations -----------------------------------------------------

private val CIT_FSSAI_PKG = Citation(
    "cit-fssai-pkg-2018",
    "Food Safety and Standards (Packaging) Regulations, 2018",
    "Reg. 4(4) · Schedule III",
    "https://fssai.gov.in/upload/uploadfiles/files/Compendium_Packaging_01_02_2022.pdf"
)
private val CIT_SCHEDULE_IV = Citation(
    "cit-fssai-sch4",
    "FSSAI Packaging Regulations, 2018",
    "Schedule IV — suggestive packaging materials",
    "https://fssai.gov.in/upload/uploadfiles/files/Compendium_Packaging_01_02_2022.pdf"
)
private val CIT_IFCT = Citation(
    "cit-ifct2017",
    "Indian Food Composition Tables, 2017",
    "ICMR–National Institute of Nutrition, Hyderabad",
    "https://www.nin.res.in/ebooks/IFCT2017.pdf"
)
private val CIT_CIPHET = Citation(
    "cit-ciphet",
    "ICAR-CIPHET — Modified atmosphere packaging protocols",
    "Agri Structures & Environmental Control division",
    "https://ciphet.icar.gov.in/division/divisions/as-ec/"
)
private val CIT_BIS_SUSTAIN = Citation(
    "cit-bis-sustain",
    "BIS Handbook — A drive towards Sustainable Packaging",
    "IS 14534 · IS 17899T · IS/ISO 17088",
    "https://www.bis.gov.in/wp-content/uploads/2023/01/Final-handbook-coloured_F_compressed.pdf"
)
private val CIT_MAP_LIT = Citation(
    "cit-map-lit",
    "Modified Atmosphere Packaging of Fruits and Vegetables",
    "Recommended O₂ / CO₂ by commodity",
    "http://www.globalsciencebooks.info/Online/GSBOnline/images/0906/FP_3(1)/FP_3(1)1-31o.pdf"
)

// --- Material catalogue (ILLUSTRATIVE — see data-honesty note) -------------

private val M_METPET_PE = PackagingMaterial(
    id = "metpet12-al9-ldpe70",
    name = "Metalized PET / Al / LDPE laminate",
    structure = "12 µ PET / 9 µ Al / 70 µ LDPE",
    otr = 0.8, otrTempC = 23, otrRhPct = 0,
    wvtr = 0.5, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "91 µ total",
    sealability = "Heat-sealable (LDPE seal layer), good hot-tack",
    tensileMpa = 45.0,
    recyclable = false,
    compostableIs17088 = false,
    isCodes = listOf("IS 12252", "IS 2508"),
    costIndex = 4,
    source = SourceRef(SourceKind.DB, "materials#metpet12", "Materials catalogue (illustrative)")
)

private val M_FOIL_LAM = PackagingMaterial(
    id = "pet12-al12-ldpe60",
    name = "PET / Aluminium foil / LDPE laminate",
    structure = "12 µ PET / 12 µ Al foil / 60 µ LDPE",
    otr = 0.05, otrTempC = 23, otrRhPct = 0,
    wvtr = 0.02, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "84 µ total",
    sealability = "Heat-sealable, excellent hot-tack",
    tensileMpa = 60.0,
    recyclable = false,
    compostableIs17088 = false,
    isCodes = listOf("IS 12252", "IS 15392", "IS 2508"),
    costIndex = 5,
    source = SourceRef(SourceKind.DB, "materials#foillam", "Materials catalogue (illustrative)")
)

private val M_PET_PE = PackagingMaterial(
    id = "pet12-ldpe60",
    name = "PET / LDPE laminate",
    structure = "12 µ PET / 60 µ LDPE",
    otr = 60.0, otrTempC = 23, otrRhPct = 0,
    wvtr = 8.0, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "72 µ total",
    sealability = "Heat-sealable, good hot-tack",
    tensileMpa = 50.0,
    recyclable = false,
    compostableIs17088 = false,
    isCodes = listOf("IS 12252", "IS 2508"),
    costIndex = 3,
    source = SourceRef(SourceKind.DB, "materials#petpe", "Materials catalogue (illustrative)")
)

private val M_BOPP_PE = PackagingMaterial(
    id = "bopp20-ldpe40",
    name = "BOPP / LDPE laminate",
    structure = "20 µ BOPP / 40 µ LDPE",
    otr = 1200.0, otrTempC = 23, otrRhPct = 0,
    wvtr = 5.0, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "60 µ total",
    sealability = "Heat-sealable, moderate hot-tack",
    tensileMpa = 120.0,
    recyclable = false,
    compostableIs17088 = false,
    isCodes = listOf("IS 2508"),
    costIndex = 2,
    source = SourceRef(SourceKind.DB, "materials#bopppe", "Materials catalogue (illustrative)")
)

private val M_LDPE_50 = PackagingMaterial(
    id = "ldpe-50",
    name = "LDPE film, 50 µ",
    structure = "Monolayer LDPE 50 µ",
    otr = 4500.0, otrTempC = 23, otrRhPct = 0,
    wvtr = 16.0, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "50 µ",
    sealability = "Heat-sealable, excellent",
    tensileMpa = 18.0,
    recyclable = true,
    compostableIs17088 = false,
    isCodes = listOf("IS 2508"),
    costIndex = 1,
    source = SourceRef(SourceKind.DB, "materials#ldpe50", "Materials catalogue (illustrative)")
)

private val M_LDPE_MICROPERF = PackagingMaterial(
    id = "ldpe-40-microperf",
    name = "Micro-perforated LDPE, 40 µ",
    structure = "Monolayer LDPE 40 µ, laser micro-perforated",
    otr = 9000.0, otrTempC = 23, otrRhPct = 0,
    wvtr = 30.0, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "40 µ",
    sealability = "Heat-sealable outside perforated zone",
    tensileMpa = 16.0,
    recyclable = true,
    compostableIs17088 = false,
    isCodes = listOf("IS 2508"),
    costIndex = 2,
    source = SourceRef(SourceKind.DB, "materials#ldpemp", "Materials catalogue (illustrative)")
)

private val M_LDPE_PERF_LOW = PackagingMaterial(
    id = "ldpe-40-perf-low",
    name = "Lightly micro-perforated LDPE, 40 µ",
    structure = "Monolayer LDPE 40 µ, low-count laser perforation",
    otr = 5000.0, otrTempC = 23, otrRhPct = 0,
    wvtr = 22.0, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "40 µ",
    sealability = "Heat-sealable outside perforated zone",
    tensileMpa = 16.0,
    recyclable = true,
    compostableIs17088 = false,
    isCodes = listOf("IS 2508"),
    costIndex = 2,
    source = SourceRef(SourceKind.DB, "materials#ldpeperflow", "Materials catalogue (illustrative)")
)

private val M_PLA = PackagingMaterial(
    id = "pla-compostable",
    name = "PLA film, compostable",
    structure = "Monolayer PLA 30 µ",
    otr = 550.0, otrTempC = 23, otrRhPct = 0,
    wvtr = 280.0, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "30 µ",
    sealability = "Heat-sealable, narrow seal window",
    tensileMpa = 55.0,
    recyclable = false,
    compostableIs17088 = true,
    isCodes = listOf("IS/ISO 17088", "IS 17899T"),
    costIndex = 4,
    source = SourceRef(SourceKind.DB, "materials#pla", "Materials catalogue (illustrative)")
)

private val M_HB_PLA = PackagingMaterial(
    id = "pla-hb-laminate",
    name = "High-barrier metallised PLA laminate",
    structure = "Metallised PLA 20 µ / PLA 40 µ seal layer",
    otr = 12.0, otrTempC = 23, otrRhPct = 0,
    wvtr = 25.0, wvtrTempC = 38, wvtrRhPct = 90,
    thicknessUm = "60 µ total",
    sealability = "Heat-sealable, narrow seal window",
    tensileMpa = 60.0,
    recyclable = false,
    compostableIs17088 = true,
    isCodes = listOf("IS/ISO 17088", "IS 17899T"),
    costIndex = 5,
    source = SourceRef(SourceKind.DB, "materials#plahb", "Materials catalogue (illustrative)")
)

// --- Commodity profiles ----------------------------------------------------

val FIXTURE_COMMODITIES: List<CommodityProfile> = listOf(
    CommodityProfile(
        id = "peanut-roasted",
        name = "Roasted peanuts",
        aliases = listOf("peanut", "groundnut", "moongphali", "roasted nuts"),
        form = "Roasted, whole",
        foodGroup = FoodGroup.DRY_HIGH_FAT,
        moisturePct = 5.2,
        fatPct = 49.1,
        ph = 6.0,
        defaultStorage = StorageType.AMBIENT,
        source = SRC_IFCT
    ),
    CommodityProfile(
        id = "okra-fresh",
        name = "Fresh okra",
        aliases = listOf("okra", "bhindi", "ladies finger", "bhindi fresh"),
        form = "Fresh whole pods",
        foodGroup = FoodGroup.FRESH_PRODUCE,
        moisturePct = 89.0,
        fatPct = 0.2,
        ph = 6.0,
        respirationRate = 120.0,
        respirationTempC = 20,
        chillingSensitive = true,
        defaultStorage = StorageType.CHILLED,
        source = SRC_IFCT
    ),
    CommodityProfile(
        id = "mango-fresh",
        name = "Fresh mango",
        aliases = listOf("mango", "aam", "kesar mango", "alphonso"),
        form = "Fresh whole fruit",
        foodGroup = FoodGroup.FRESH_PRODUCE,
        moisturePct = 81.0,
        fatPct = 0.4,
        ph = 5.0,
        respirationRate = 45.0,
        respirationTempC = 20,
        chillingSensitive = true,
        defaultStorage = StorageType.AMBIENT,
        source = SRC_IFCT
    ),
    CommodityProfile(
        id = "milk-powder",
        name = "Whole milk powder",
        aliases = listOf("milk powder", "dairy powder", "skimmed milk powder", "milkpowder"),
        form = "Spray-dried powder",
        foodGroup = FoodGroup.DRY_HIGH_FAT,
        moisturePct = 3.0,
        fatPct = 26.7,
        ph = 6.6,
        defaultStorage = StorageType.AMBIENT,
        source = SRC_IFCT
    ),
    CommodityProfile(
        id = "chilli-powder",
        name = "Ground chilli powder",
        aliases = listOf("chilli", "chili powder", "mirchi", "red chilli powder", "spice", "spices"),
        form = "Ground powder",
        foodGroup = FoodGroup.DRY_HIGH_FAT,
        moisturePct = 9.0,
        fatPct = 14.1,
        ph = 5.5,
        defaultStorage = StorageType.AMBIENT,
        source = SRC_IFCT
    )
)

// --- Full canned recommendations -------------------------------------------

private val REC_PEANUT = RecommendationResponse(
    requestId = "fx-peanut-001",
    commodity = FIXTURE_COMMODITIES[0],
    assumptions = listOf(
        "Target shelf life read as 6 months (180 days) at ambient storage.",
        "Pack assumed 100 g retail pillow pack, surface area ≈ 0.032 m².",
        "Distribution assumed domestic, covered warehousing, no direct sunlight."
    ),
    recommended = M_METPET_PE,
    barriers = BarrierSpec(
        otrTargetMax = 1.0,
        wvtrTargetMax = 1.0,
        rationale = "At 49.1% fat, oxidative rancidity is the limiting failure mode over 180 " +
            "days at ambient. The oxygen budget for the whole shelf life is reached at an " +
            "OTR near 1 cm³/m²·day; above roughly 3 cm³/m²·day peroxide values climb " +
            "well before month six. WVTR below 1 g/m²·day keeps the product under its " +
            "5.2% equilibrium moisture so it stays crisp rather than turning chewy."
    , source = SRC_CALC),
    map = MapRecommendation(
        o2Pct = "< 0.5%",
        co2Pct = "—",
        n2Pct = "balance",
        storageTempC = "Ambient, below 30 °C",
        perforation = null,
        expectedShelfLifeDays = 180,
        notes = "Nitrogen flushing at the form-fill-seal head is what makes the 6-month " +
            "claim realistic. Residual oxygen after flushing should be under 2%.",
        source = SRC_MAP_CALC
    ),
    compliance = listOf(
        ComplianceNote(
            requirement = "Overall migration limit",
            limit = "60 mg/kg or 10 mg/dm²",
            status = ComplianceStatus.ACTION_REQUIRED,
            isReference = "IS 9845",
            detail = "Test the finished laminate at NABL-accredited lab before production.",
            source = SRC_FSSAI_PKG
        ),
        ComplianceNote(
            requirement = "Plastic layers in contact with food",
            limit = "PET → IS 12252, LDPE → IS 2508",
            status = ComplianceStatus.MET,
            isReference = "Schedule III items 7, 18",
            source = SRC_FSSAI_SCH3
        ),
        ComplianceNote(
            requirement = "Recycled plastics",
            limit = "Prohibited for food contact",
            status = ComplianceStatus.PROHIBITED,
            isReference = "Reg. 4(4)(e)",
            detail = "All layers must be virgin food-grade. Specify this to the converter.",
            source = SRC_FSSAI_PKG
        ),
        ComplianceNote(
            requirement = "Printing inks and pigments",
            limit = "IS 15495 (inks) · IS 9833 (pigments)",
            status = ComplianceStatus.ACTION_REQUIRED,
            isReference = "Reg. 4(3)(9)",
            detail = "Printed surface must not contact food — keep print on the outer PET ply.",
            source = SRC_FSSAI_PKG
        )
    ),
    alternatives = listOf(
        Alternative(
            AlternativeKind.HIGHER_BARRIER,
            M_FOIL_LAM,
            "Near-zero transmission. Worth it only if you are pushing past 12 months " +
                "or shipping through hot, humid corridors."
        ),
        Alternative(
            AlternativeKind.SUSTAINABLE,
            M_HB_PLA,
            "Compostable to IS/ISO 17088, but its OTR is roughly twelve times the target. " +
                "Realistic for a 2–3 month claim, not for six."
        ),
        Alternative(
            AlternativeKind.CHEAPER,
            M_PET_PE,
            "About a quarter of the laminate cost, but at OTR 60 it will not hold " +
                "six months — expect rancidity from month two."
        )
    ),
    citations = listOf(CIT_FSSAI_PKG, CIT_SCHEDULE_IV, CIT_IFCT, CIT_BIS_SUSTAIN),
    narrative = "For roasted peanuts at 49.1% fat, the six-month ambient target is an " +
        "oxygen problem before it is anything else. Rancidity is what will end the " +
        "product's life, so the film has to hold oxygen out almost entirely — under " +
        "1 cm³/m²·day — and nitrogen flushing at packing is not optional. Metalized " +
        "PET bonded to an aluminium layer with an LDPE sealant gets you there while " +
        "still running on ordinary form-fill-seal equipment. Two things to sort before " +
        "you commit to a print run: get the finished laminate migration-tested at a " +
        "NABL-accredited lab, and confirm with your converter that every layer is " +
        "virgin food-grade, since recycled content is prohibited outright.",
    followUps = listOf(
        "What changes for a 3-month claim?",
        "Can I do this without nitrogen flushing?",
        "Show me a compostable option"
    )
)

private val REC_OKRA = RecommendationResponse(
    requestId = "fx-okra-001",
    commodity = FIXTURE_COMMODITIES[1],
    assumptions = listOf(
        "Target read as extending marketable life; CIPHET's MAP protocol gives 10 days.",
        "Pack assumed 250 g retail punnet / pillow pack.",
        "Cold chain assumed available at 8–10 °C."
    ),
    recommended = M_LDPE_MICROPERF,
    barriers = BarrierSpec(
        otrTargetMin = 5000.0,
        otrTargetMax = 10000.0,
        wvtrTargetMax = 30.0,
        rationale = "Okra respires at roughly 120 mg CO₂/kg·h at 20 °C, which puts it in " +
            "the highest band. A sealed high-barrier film would drive the pack anaerobic " +
            "within a day or two and cause off-odours, so the film must let oxygen back " +
            "in. The required OTR balances respiration against pack surface area and " +
            "target headspace oxygen, giving a window of 5,000–10,000 — too tight and " +
            "you get anaerobic spoilage, too loose and you lose the modified atmosphere " +
            "entirely. WVTR is capped near 30 to limit weight loss without letting free " +
            "water pool in the pack."
    , source = SRC_MAP_CALC),
    map = MapRecommendation(
        o2Pct = "3–5%",
        co2Pct = "5–10%",
        n2Pct = "balance",
        storageTempC = "8–10 °C",
        perforation = "Laser micro-perforation, tuned to pack weight",
        expectedShelfLifeDays = 10,
        notes = "ICAR-CIPHET's published MAP protocol for okra reports 10 days of " +
            "marketable life, against 4–5 days in open ambient storage. Okra is " +
            "chilling-sensitive below about 7 °C, so do not push the cold chain lower.",
        source = SRC_CIPHET
    ),
    compliance = listOf(
        ComplianceNote(
            requirement = "Overall migration limit",
            limit = "60 mg/kg or 10 mg/dm²",
            status = ComplianceStatus.ACTION_REQUIRED,
            isReference = "IS 9845",
            source = SRC_FSSAI_PKG
        ),
        ComplianceNote(
            requirement = "Plastic in contact with food",
            limit = "LDPE → IS 2508",
            status = ComplianceStatus.MET,
            isReference = "Schedule III item 18",
            source = SRC_FSSAI_SCH3
        ),
        ComplianceNote(
            requirement = "Recycled plastics",
            limit = "Prohibited for food contact",
            status = ComplianceStatus.PROHIBITED,
            isReference = "Reg. 4(4)(e)",
            source = SRC_FSSAI_PKG
        )
    ),
    alternatives = listOf(
        Alternative(
            AlternativeKind.CHEAPER,
            M_LDPE_50,
            "Plain 50 µ LDPE is cheaper but sits at OTR 4,500, below the target — " +
                "expect anaerobic off-odours by day three."
        ),
        Alternative(
            AlternativeKind.SUSTAINABLE,
            M_PLA,
            "Compostable, but its transmission is far too low for a respiring product " +
                "and it is not perforated."
        ),
        Alternative(
            AlternativeKind.HIGHER_BARRIER,
            M_BOPP_PE,
            "Wrong direction — higher barrier is the failure mode here, not the fix."
        )
    ),
    citations = listOf(CIT_CIPHET, CIT_MAP_LIT, CIT_FSSAI_PKG, CIT_IFCT),
    narrative = "Okra is the opposite of the peanut problem. At around 120 mg CO₂/kg·h it " +
        "is respiring hard enough that a good barrier film will hurt you — seal it tight " +
        "and the pack goes anaerobic within a couple of days, which shows up as " +
        "off-odour and softening. What you want is a deliberately leaky film: " +
        "micro-perforated LDPE in the 5,000–8,000 OTR range, held at 8–10 °C, which is " +
        "where ICAR-CIPHET's own protocol gets about 10 days against 4–5 in open " +
        "ambient storage. One warning: okra is chilling-sensitive below roughly 7 °C, so " +
        "colder is not better here, and the perforation count has to be tuned to your " +
        "actual pack weight — do not copy a competitor's film spec.",
    followUps = listOf(
        "How do I calculate the perforation count?",
        "What if I have no cold chain?",
        "Same question for broccoli"
    )
)

private val REC_MANGO = RecommendationResponse(
    requestId = "fx-mango-001",
    commodity = FIXTURE_COMMODITIES[2],
    assumptions = listOf(
        "Target read as extending marketable life through transport and ripening.",
        "Pack assumed 4-fruit retail tray or carton.",
        "Single-layer packing assumed, no controlled-atmosphere container."
    ),
    recommended = M_LDPE_PERF_LOW,
    barriers = BarrierSpec(
        otrTargetMin = 3000.0,
        otrTargetMax = 6000.0,
        wvtrTargetMax = 25.0,
        rationale = "Mango is climacteric and respires near 45 mg CO₂/kg·h at 20 °C, " +
            "roughly a third of okra's rate, so the film still has to breathe but " +
            "through a narrower window than okra needs. Ethylene accumulation is the " +
            "thing to control here, which is why the CO₂ target is set above the O₂ " +
            "target."
    , source = SRC_MAP_CALC),
    map = MapRecommendation(
        o2Pct = "3–5%",
        co2Pct = "5–8%",
        n2Pct = "balance",
        storageTempC = "13 °C",
        perforation = "Micro-perforation, lower count than leafy produce",
        expectedShelfLifeDays = 21,
        notes = "Mango is chilling-sensitive. Below about 10 °C you get peel pitting and " +
            "uneven ripening, which is why the target is 13 °C rather than the 8 °C " +
            "used for okra. Do not refrigerate harder to buy extra days.",
        source = SRC_MAP_CALC
    ),
    compliance = listOf(
        ComplianceNote(
            requirement = "Overall migration limit",
            limit = "60 mg/kg or 10 mg/dm²",
            status = ComplianceStatus.ACTION_REQUIRED,
            isReference = "IS 9845",
            source = SRC_FSSAI_PKG
        ),
        ComplianceNote(
            requirement = "Plastic in contact with food",
            limit = "LDPE → IS 2508",
            status = ComplianceStatus.MET,
            isReference = "Schedule III item 18",
            source = SRC_FSSAI_SCH3
        )
    ),
    alternatives = listOf(
        Alternative(
            AlternativeKind.CHEAPER,
            M_LDPE_50,
            "Plain 50 µ LDPE at OTR 4,500 sits inside the window and is the cheapest " +
                "option, but with no perforation the CO₂ has nowhere to go and will " +
                "drift upward over a long transit."
        ),
        Alternative(
            AlternativeKind.SUSTAINABLE,
            M_PLA,
            "Compostable, but OTR 550 is roughly five times too tight for a respiring " +
                "fruit — the pack will go anaerobic."
        ),
        Alternative(
            AlternativeKind.HIGHER_BARRIER,
            M_BOPP_PE,
            "OTR 1,200 is far too tight. Only sensible if you move to active atmosphere " +
                "control rather than passive MAP."
        )
    ),
    citations = listOf(CIT_MAP_LIT, CIT_CIPHET, CIT_FSSAI_PKG, CIT_IFCT),
    narrative = "Mango sits between the two extremes. It is climacteric and still " +
        "respiring at roughly 45 mg CO₂/kg·h, so the film has to breathe, but because " +
        "ethylene drives ripening the useful lever is CO₂, not just oxygen — target " +
        "3–5% O₂ against 5–8% CO₂. The temperature matters more than the film here: " +
        "13 °C is the sweet spot, and going colder to buy extra days will cost you peel " +
        "pitting and uneven ripening, because mango is chilling-sensitive below about " +
        "10 °C. Micro-perforated LDPE tuned to a lower count than you would use for " +
        "leafy produce is the practical answer for retail trays.",
    followUps = listOf(
        "Can I ship mango without refrigeration?",
        "What about individually shrink-wrapped fruit?",
        "How does this change for export by sea?"
    )
)

private val REC_MILK_POWDER = RecommendationResponse(
    requestId = "fx-milkpowder-001",
    commodity = FIXTURE_COMMODITIES[3],
    assumptions = listOf(
        "Target read as 12 months at ambient.",
        "Pack assumed 500 g retail pouch or composite can.",
        "Storage assumed ambient, up to 35 °C and 70% RH in summer."
    ),
    recommended = M_FOIL_LAM,
    barriers = BarrierSpec(
        otrTargetMax = 0.5,
        wvtrTargetMax = 0.1,
        rationale = "Water is the enemy here, not oxygen. Starting at 3.0% moisture, milk " +
            "powder cakes and browns once it takes up enough water to climb past about " +
            "5%. At 70% RH and 35 °C the driving force is severe, which is why the WVTR " +
            "target is an order of magnitude tighter than the OTR target — the reverse " +
            "of the peanut case."
    , source = SRC_CALC),
    map = MapRecommendation(
        o2Pct = "< 1%",
        co2Pct = "—",
        n2Pct = "balance",
        storageTempC = "Ambient, cool and dry",
        perforation = null,
        expectedShelfLifeDays = 365,
        notes = "Nitrogen flushing is standard for milk powder and helps both oxidation " +
            "and caking. An oxygen scavenger sachet is worth considering for long " +
            "ambient distribution.",
        source = SRC_MAP_CALC
    ),
    compliance = listOf(
        ComplianceNote(
            requirement = "Overall migration limit",
            limit = "60 mg/kg or 10 mg/dm²",
            status = ComplianceStatus.ACTION_REQUIRED,
            isReference = "IS 9845",
            source = SRC_FSSAI_PKG
        ),
        ComplianceNote(
            requirement = "Plastic layers in contact with food",
            limit = "PET → IS 12252, LDPE → IS 2508",
            status = ComplianceStatus.MET,
            isReference = "Schedule III items 7, 18",
            source = SRC_FSSAI_SCH3
        ),
        ComplianceNote(
            requirement = "Aluminium foil for food packaging",
            limit = "IS 15392",
            status = ComplianceStatus.MET,
            isReference = "Schedule II item 4",
            source = SRC_FSSAI_SCH3
        ),
        ComplianceNote(
            requirement = "Indicative materials for milk products",
            limit = "PE or PP co-extruded multi-layer · paperboard/Al/PE laminate",
            status = ComplianceStatus.MET,
            isReference = "Schedule IV",
            detail = "Schedule IV lists foil-based laminated structures for milk products.",
            source = SRC_FSSAI_SCH4
        )
    ),
    alternatives = listOf(
        Alternative(
            AlternativeKind.CHEAPER,
            M_METPET_PE,
            "Metalized rather than foil. Roughly 20% cheaper and adequate for a 9-month " +
                "claim, but its WVTR of 0.5 is five times looser than the 12-month target."
        ),
        Alternative(
            AlternativeKind.SUSTAINABLE,
            M_HB_PLA,
            "Compostable, but WVTR around 25 is two orders of magnitude too high — " +
                "not viable for milk powder."
        )
    ),
    citations = listOf(CIT_FSSAI_PKG, CIT_SCHEDULE_IV, CIT_IFCT),
    narrative = "Milk powder inverts the peanut logic — moisture, not oxygen, is what " +
        "will kill it. At 3.0% starting moisture the powder cakes and browns once it " +
        "climbs past roughly 5%, and at 35 °C with 70% humidity the driving force is " +
        "brutal. That is why the water vapour target is ten times tighter than the " +
        "oxygen target, and why a foil laminate is the honest answer rather than a " +
        "metalized one: foil gets you to 0.02 g/m²·day where metalized PET only reaches " +
        "0.5. FSSAI's own Schedule IV already points at foil-based and paperboard " +
        "laminate structures for milk products, so you are on well-trodden regulatory " +
        "ground. Nitrogen flushing plus an oxygen scavenger is standard practice and " +
        "worth the small extra cost for a 12-month claim.",
    followUps = listOf(
        "What if I only need 6 months?",
        "Is a composite can better than a pouch?",
        "Do I need an oxygen scavenger?"
    )
)

private val REC_CHILLI = RecommendationResponse(
    requestId = "fx-chilli-001",
    commodity = FIXTURE_COMMODITIES[4],
    assumptions = listOf(
        "Target read as 12 months at ambient.",
        "Pack assumed 100 g retail pillow pack.",
        "Quality attribute of interest assumed to be colour and pungency retention."
    ),
    recommended = M_METPET_PE,
    barriers = BarrierSpec(
        otrTargetMax = 20.0,
        wvtrTargetMax = 5.0,
        rationale = "Chilli loses colour and pungency through oxidation of carotenoids " +
            "and volatile loss, so oxygen matters — but far less aggressively than for a " +
            "high-fat product at 49%. At 14.1% fat the oxidative load is moderate, and " +
            "the binding constraint is keeping moisture below about 10% so the powder " +
            "stays free-flowing and does not clump."
    , source = SRC_CALC),
    map = MapRecommendation(
        o2Pct = "< 2%",
        co2Pct = "—",
        n2Pct = "balance",
        storageTempC = "Ambient, away from light",
        perforation = null,
        expectedShelfLifeDays = 365,
        notes = "Light is a bigger threat to chilli colour than oxygen is. Specify an " +
            "opaque or metalized outer web if the pack will sit under retail lighting.",
        source = SRC_MAP_CALC
    ),
    compliance = listOf(
        ComplianceNote(
            requirement = "Overall migration limit",
            limit = "60 mg/kg or 10 mg/dm²",
            status = ComplianceStatus.ACTION_REQUIRED,
            isReference = "IS 9845",
            source = SRC_FSSAI_PKG
        ),
        ComplianceNote(
            requirement = "Plastic layers in contact with food",
            limit = "PET → IS 12252, LDPE → IS 2508",
            status = ComplianceStatus.MET,
            isReference = "Schedule III items 7, 18",
            source = SRC_FSSAI_SCH3
        ),
        ComplianceNote(
            requirement = "Printing inks and pigments",
            limit = "IS 15495 (inks) · IS 9833 (pigments)",
            status = ComplianceStatus.ACTION_REQUIRED,
            isReference = "Reg. 4(3)(9)",
            detail = "Chilli is oily enough for ink migration to matter — keep print reverse-side.",
            source = SRC_FSSAI_PKG
        )
    ),
    alternatives = listOf(
        Alternative(
            AlternativeKind.HIGHER_BARRIER,
            M_FOIL_LAM,
            "Blocks light as well as oxygen, so it is the strongest choice for colour " +
                "retention. Costs noticeably more and is hard to justify unless the pack " +
                "sits under strong retail lighting for months."
        ),
        Alternative(
            AlternativeKind.CHEAPER,
            M_PET_PE,
            "About a quarter cheaper, but OTR 60 is three times the target and it " +
                "transmits light — expect visible fading inside six to nine months."
        ),
        Alternative(
            AlternativeKind.SUSTAINABLE,
            M_HB_PLA,
            "Compostable to IS/ISO 17088 and inside the oxygen target at OTR 12. The " +
                "compromise is water vapour — WVTR 25 against a target of 5 — so this " +
                "only works if ambient humidity is controlled."
        )
    ),
    citations = listOf(CIT_FSSAI_PKG, CIT_SCHEDULE_IV, CIT_IFCT, CIT_BIS_SUSTAIN),
    narrative = "Ground chilli is a moderate case, and the thing most people get wrong " +
        "is optimising the wrong variable. Oxygen does drive colour loss through " +
        "carotenoid oxidation, so you need a real barrier — but at 14.1% fat the " +
        "oxidative load is a fraction of what peanuts face, and the binding constraint " +
        "is really light. Retail lighting will fade chilli faster than oxygen ingress " +
        "will, which is why a metalized laminate beats a clear PET one at almost the " +
        "same cost: it solves the oxygen target and the light problem in one web. Keep " +
        "moisture below 10% so the powder stays free-flowing. This is also one of the " +
        "few cases where a compostable option is genuinely defensible — a high-barrier " +
        "PLA laminate lands inside the oxygen target, with water vapour as the trade-off.",
    followUps = listOf(
        "How much does light really matter?",
        "Same question for turmeric",
        "Show me the compostable route"
    )
)

/** Canned responses keyed by commodity id. */
val FIXTURE_RECOMMENDATIONS: Map<String, RecommendationResponse> = mapOf(
    "peanut-roasted" to REC_PEANUT,
    "okra-fresh" to REC_OKRA,
    "mango-fresh" to REC_MANGO,
    "milk-powder" to REC_MILK_POWDER,
    "chilli-powder" to REC_CHILLI
)

/**
 * Shelf life each canned example was worked out for, in days.
 *
 * Used by the chat to detect when the user asked about a different
 * horizon than the example covers, so it can say so instead of quietly
 * disagreeing with them. Goes away once the backend recalculates live.
 */
val FIXTURE_SHELF_LIFE_DAYS: Map<String, Int> = mapOf(
    "peanut-roasted" to 180,
    "okra-fresh" to 10,
    "mango-fresh" to 21,
    "milk-powder" to 365,
    "chilli-powder" to 365
)

/** Example prompts for the empty chat state. */
val FIXTURE_SUGGESTIONS: List<String> = listOf(
    "How should I package roasted peanuts for 6 months?",
    "I need to ship fresh okra — what film?",
    "Best packaging for mango in transit",
    "Packaging for milk powder, 12 month shelf life",
    "What film keeps chilli powder from fading?"
)
