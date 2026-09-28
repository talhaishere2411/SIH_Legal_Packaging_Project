package com.legalmetrology.inspector.data.repository

import com.legalmetrology.inspector.domain.model.RecommendationResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds recommendations generated during the session so the report screen
 * can retrieve one by id.
 *
 * Navigation arguments have to be primitives, and a recommendation is far
 * too large to serialise into a route, so the chat stores the object here
 * and passes only [RecommendationResponse.requestId].
 *
 * TODO — PRODUCTION: replace with a Room-backed inspection/report table so
 * saved reports survive process death and work offline.
 */
@Singleton
class RecommendationCache @Inject constructor() {

    private val items = LinkedHashMap<String, RecommendationResponse>()

    fun put(recommendation: RecommendationResponse) {
        items[recommendation.requestId] = recommendation
    }

    fun get(requestId: String): RecommendationResponse? = items[requestId]
}
