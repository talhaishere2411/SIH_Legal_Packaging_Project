package com.legalmetrology.inspector.ui.screens.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.legalmetrology.inspector.data.repository.RecommendationCache
import com.legalmetrology.inspector.domain.model.RecommendationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Retrieves the recommendation the chat produced, by id.
 *
 * TODO — PRODUCTION: fall back to a Room query (and then a network fetch)
 * when the in-memory cache has been cleared, so a saved report can be
 * reopened after the process restarts.
 */
@HiltViewModel
class PackagingReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cache: RecommendationCache
) : ViewModel() {

    val recommendation: RecommendationResponse? =
        savedStateHandle.get<String>("requestId")?.let { cache.get(it) }
}
