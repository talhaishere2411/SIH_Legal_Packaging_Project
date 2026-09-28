package com.legalmetrology.inspector.data.repository

import com.legalmetrology.inspector.data.fixtures.FIXTURE_COMMODITIES
import com.legalmetrology.inspector.data.fixtures.FIXTURE_RECOMMENDATIONS
import com.legalmetrology.inspector.data.fixtures.FIXTURE_SCENARIOS
import com.legalmetrology.inspector.data.fixtures.FIXTURE_SHELF_LIFE_DAYS
import com.legalmetrology.inspector.data.fixtures.FIXTURE_SUGGESTIONS
import com.legalmetrology.inspector.data.fixtures.ScenarioKey
import com.legalmetrology.inspector.data.fixtures.fixtureFollowUpAnswer
import com.legalmetrology.inspector.data.fixtures.shelfLifeBucket
import com.legalmetrology.inspector.domain.model.CommodityProfile
import com.legalmetrology.inspector.domain.model.RecommendationRequest
import com.legalmetrology.inspector.domain.model.RecommendationResponse
import com.legalmetrology.inspector.domain.model.StorageType
import com.legalmetrology.inspector.domain.repository.RecommendationRepository

/**
 * Fixture-backed repository powering the demo build.
 *
 * Behaviour is deliberately simple: match the query against commodity
 * names and aliases, then serve the canned response. The intent parser,
 * barrier calculator and RAG retrieval all live on the server in the
 * real build — this class stands in for all of them, which is exactly
 * why the UI can be written and reviewed before any of that exists.
 *
 * Swap point: implement [RecommendationRepository] over Retrofit and
 * bind it in the Hilt module instead. Nothing above the interface
 * needs to change.
 */
class FixtureRecommendationRepository @javax.inject.Inject constructor() : RecommendationRepository {

    override suspend fun getSuggestions(): List<String> = FIXTURE_SUGGESTIONS

    override suspend fun resolveCommodity(query: String): CommodityProfile? =
        matchCommodity(query)

    override suspend fun recommend(request: RecommendationRequest): RecommendationResponse? {
        val profile = request.commodityId
            ?.let { id -> FIXTURE_COMMODITIES.firstOrNull { it.id == id } }
            ?: matchCommodity(request.query)
            ?: return null

        val defaultDays = FIXTURE_SHELF_LIFE_DAYS[profile.id] ?: 180
        val requestedDays = request.shelfLifeDays ?: defaultDays
        val requestedStorage = request.storage ?: profile.defaultStorage
        val requestedBucket = shelfLifeBucket(requestedDays)
        val exactKey = ScenarioKey(profile.id, requestedBucket, requestedStorage)
        val exact = FIXTURE_SCENARIOS[exactKey]
        if (exact != null) return applyRequestMetadata(exact, request, exactMatch = true)

        // A missing cell is not an excuse to invent a number. Pick the
        // nearest authored bucket for this commodity/storage, then say so.
        val sameStorage = FIXTURE_SCENARIOS
            .filterKeys { it.commodityId == profile.id && it.storage == requestedStorage }
        val nearest = sameStorage.entries.minByOrNull {
            kotlin.math.abs(it.key.shelfLife.ordinal - requestedBucket.ordinal)
        }?.value
        if (nearest != null) {
            return applyRequestMetadata(nearest, request, exactMatch = false)
        }

        // Finally use the commodity's authored default. This is the safe
        // fallback for unsupported storage types such as frozen peanuts.
        return FIXTURE_RECOMMENDATIONS[profile.id]?.let {
            applyRequestMetadata(it, request, exactMatch = false)
        }
    }

    override suspend fun answerFollowUp(
        commodityId: String,
        prompt: String,
        recommendation: RecommendationResponse
    ): String? = fixtureFollowUpAnswer(commodityId, prompt)

    private fun applyRequestMetadata(
        response: RecommendationResponse,
        request: RecommendationRequest,
        exactMatch: Boolean
    ): RecommendationResponse {
        val requestedDays = request.shelfLifeDays
        val requestedStorage = request.storage
        val notes = buildList {
            if (!exactMatch && requestedDays != null) {
                add(
                    "No exact local scenario is authored for ${requestedDays} days; " +
                        "the nearest authored scenario is shown and this limitation is recorded."
                )
            }
            if (!exactMatch && requestedStorage != null && requestedStorage != response.commodity.defaultStorage) {
                add("Storage ${requestedStorage.displayName} is not separately calibrated for this commodity; the default fixture is shown with this limitation.")
            }
            if (request.overrides.isNotEmpty()) {
                add("Profile overrides supplied: ${request.overrides.entries.joinToString { "${it.key} ${it.value}" }}. The demo keeps the authored scenario values and flags the override for review.")
            }
        }
        if (notes.isEmpty()) return response

        return response.copy(
            requestId = "${response.requestId}-${requestedDays ?: "default"}-${requestedStorage?.name?.lowercase() ?: "default"}",
            assumptions = response.assumptions + notes
        )
    }

    // --- matching ----------------------------------------------------------

    /**
     * Alias match on the normalised query string.
     *
     * Longest alias wins, so "red chilli powder" beats a bare "spice"
     * when both appear. This is a stand-in for the server-side intent
     * parser, which will do this properly with a language model.
     */
    private fun matchCommodity(query: String): CommodityProfile? {
        val q = normalise(query)
        if (q.isBlank()) return null

        var best: CommodityProfile? = null
        var bestLength = 0

        for (commodity in FIXTURE_COMMODITIES) {
            val candidates = listOf(commodity.name) + commodity.aliases
            for (candidate in candidates) {
                val c = normalise(candidate)
                if (c.length > bestLength && q.contains(c)) {
                    best = commodity
                    bestLength = c.length
                }
            }
        }
        return best
    }

    private fun normalise(value: String): String =
        value.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}

// ============================================================
// Query helpers
//
// The real intent parser will extract these slots server-side with a
// language model. These helpers exist so the demo can reflect a shelf
// life the user actually typed, rather than always echoing the canned
// value.
// ============================================================

/** Pulls a shelf life in days out of free text: "6 months", "10 days", "1 year". */
fun extractShelfLifeDays(query: String): Int? {
    val q = query.lowercase()
    Regex("(\\d+)\\s*month").find(q)?.let {
        return it.groupValues[1].toIntOrNull()?.times(30)
    }
    Regex("(\\d+)\\s*year").find(q)?.let {
        return it.groupValues[1].toIntOrNull()?.times(365)
    }
    Regex("(\\d+)\\s*week").find(q)?.let {
        return it.groupValues[1].toIntOrNull()?.times(7)
    }
    Regex("(\\d+)\\s*day").find(q)?.let {
        return it.groupValues[1].toIntOrNull()
    }
    return null
}

/** Pulls a storage type out of free text. */
fun extractStorageType(query: String): StorageType? {
    val q = query.lowercase()
    return when {
        q.contains("frozen") || q.contains("freeze") || q.contains("freezer") -> StorageType.FROZEN
        q.contains("chill") || q.contains("cold") || q.contains("refrigerat") -> StorageType.CHILLED
        q.contains("ambient") || q.contains("room temp") || q.contains("shelf") -> StorageType.AMBIENT
        else -> null
    }
}
