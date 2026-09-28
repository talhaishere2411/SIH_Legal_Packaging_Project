package com.legalmetrology.inspector.data.repository

import com.legalmetrology.inspector.data.fixtures.FIXTURE_COMMODITIES
import com.legalmetrology.inspector.data.fixtures.FIXTURE_RECOMMENDATIONS
import com.legalmetrology.inspector.data.fixtures.FIXTURE_SUGGESTIONS
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
class FixtureRecommendationRepository : RecommendationRepository {

    override suspend fun getSuggestions(): List<String> = FIXTURE_SUGGESTIONS

    override suspend fun resolveCommodity(query: String): CommodityProfile? =
        matchCommodity(query)

    override suspend fun recommend(request: RecommendationRequest): RecommendationResponse? {
        val profile = request.commodityId
            ?.let { id -> FIXTURE_COMMODITIES.firstOrNull { it.id == id } }
            ?: matchCommodity(request.query)
            ?: return null

        return FIXTURE_RECOMMENDATIONS[profile.id]
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
