package com.legalmetrology.inspector.domain.repository

import com.legalmetrology.inspector.domain.model.CommodityProfile
import com.legalmetrology.inspector.domain.model.RecommendationRequest
import com.legalmetrology.inspector.domain.model.RecommendationResponse

/**
 * The seam between the chat UI and whatever produces recommendations.
 *
 * Two implementations are planned:
 *
 *  1. [FixtureRecommendationRepository] — serves canned responses from
 *     [RecommendationFixtures]. Powers the demo build; no network needed.
 *
 *  2. A Retrofit-backed implementation calling `POST /recommend` on the
 *     FastAPI service. The server sits behind its own provider-agnostic
 *     generation interface, so the choice of LLM (cloud API vs local
 *     model) stays a server-side concern and never reaches the client.
 *
 * Because the UI only ever sees [RecommendationResponse], moving from (1)
 * to (2) requires no changes above this interface.
 */
interface RecommendationRepository {

    /** Example prompts shown on an empty chat. */
    suspend fun getSuggestions(): List<String>

    /** Best-effort profile for a free-text query; null when unrecognised. */
    suspend fun resolveCommodity(query: String): CommodityProfile?

    /**
     * Full recommendation, or null when the commodity could not be
     * resolved — the caller is then expected to ask a clarifying question
     * rather than guess.
     */
    suspend fun recommend(request: RecommendationRequest): RecommendationResponse?

    /**
     * Answers a structured quick-reply about the current recommendation.
     * The fixture implementation uses a local answer bank; the network
     * implementation can map this to a follow-up endpoint later.
     */
    suspend fun answerFollowUp(
        commodityId: String,
        prompt: String,
        recommendation: RecommendationResponse
    ): String?
}
