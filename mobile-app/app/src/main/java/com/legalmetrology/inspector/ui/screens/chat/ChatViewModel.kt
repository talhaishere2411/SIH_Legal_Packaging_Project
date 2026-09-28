package com.legalmetrology.inspector.ui.screens.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legalmetrology.inspector.data.fixtures.FIXTURE_SHELF_LIFE_DAYS
import com.legalmetrology.inspector.data.fixtures.FIXTURE_SUGGESTIONS
import com.legalmetrology.inspector.data.repository.RecommendationCache
import com.legalmetrology.inspector.data.repository.extractShelfLifeDays
import com.legalmetrology.inspector.data.repository.extractStorageType
import com.legalmetrology.inspector.domain.model.ChatMessage
import com.legalmetrology.inspector.domain.model.ChatRole
import com.legalmetrology.inspector.domain.model.RecommendationRequest
import com.legalmetrology.inspector.domain.model.RecommendationResponse
import com.legalmetrology.inspector.domain.repository.RecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Chat state for the Packaging Advisor.
 *
 * Talks only to [RecommendationRepository], so it does not know or care
 * whether replies come from the fixture set or from the FastAPI service.
 *
 * TODO — BACKEND INTEGRATION:
 *  - Stream assistant tokens instead of dropping the whole reply in one go.
 *  - Persist conversations to Room so history survives a restart.
 *  - Send the full slot set (shelf life, storage, pack size) once the
 *    server recalculates barriers from them.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: RecommendationRepository,
    private val cache: RecommendationCache
) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages

    private val _suggestions = mutableStateListOf<String>()
    val suggestions: List<String> get() = _suggestions

    var isThinking by mutableStateOf(false)
        private set

    private var sequence = 0

    init {
        viewModelScope.launch {
            _suggestions.addAll(repository.getSuggestions())
        }
    }

    fun send(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank() || isThinking) return

        _messages.add(ChatMessage(nextId(), ChatRole.USER, text))
        _suggestions.clear()
        isThinking = true

        viewModelScope.launch {
            // Purely presentational — makes the reply read as considered
            // rather than instantaneous. Remove once streaming is in.
            delay(550)
            _messages.addAll(buildReplies(text))
            isThinking = false
        }
    }

    // --- reply construction -------------------------------------------------

    private suspend fun buildReplies(text: String): List<ChatMessage> {
        val profile = repository.resolveCommodity(text)
            ?: return listOf(unrecognisedMessage())

        val shelfLifeDays = extractShelfLifeDays(text)
        val storage = extractStorageType(text)

        val base = repository.recommend(
            RecommendationRequest(
                query = text,
                commodityId = profile.id,
                shelfLifeDays = shelfLifeDays,
                storage = storage
            )
        ) ?: return listOf(unrecognisedMessage())

        val recommendation = reconcileShelfLife(base, profile.id, shelfLifeDays)
        cache.put(recommendation)

        return listOf(
            ChatMessage(
                id = nextId(),
                role = ChatRole.ASSISTANT,
                text = buildProfileIntro(profile.name, storage?.displayName),
                profile = profile
            ),
            ChatMessage(
                id = nextId(),
                role = ChatRole.ASSISTANT,
                text = recommendation.narrative,
                recommendation = recommendation,
                suggestions = recommendation.followUps
            )
        )
    }

    /**
     * The fixture set ships one worked example per commodity, so a shelf
     * life the user typed cannot change the numbers yet. Admit that
     * rather than quietly answering a different question.
     */
    private fun reconcileShelfLife(
        base: RecommendationResponse,
        commodityId: String,
        requestedDays: Int?
    ): RecommendationResponse {
        val exampleDays = FIXTURE_SHELF_LIFE_DAYS[commodityId] ?: return base
        if (requestedDays == null || requestedDays == exampleDays) return base

        return base.copy(
            assumptions = base.assumptions +
                "You asked about $requestedDays days. This demo ships one worked " +
                "example per commodity, so the figures below still assume " +
                "$exampleDays days. Live recalculation arrives with the backend."
        )
    }

    private fun buildProfileIntro(name: String, storage: String?): String =
        if (storage != null) {
            "I've identified $name, stored $storage. Here's the profile I'll work " +
                "from — figures are inferred from IFCT 2017 and ICAR-CIPHET, so " +
                "correct anything that doesn't match your product."
        } else {
            "I've identified $name. Here's the profile I'll work from — figures " +
                "are inferred from IFCT 2017 and ICAR-CIPHET, so correct anything " +
                "that doesn't match your product."
        }

    private fun unrecognisedMessage(): ChatMessage = ChatMessage(
        id = nextId(),
        role = ChatRole.ASSISTANT,
        text = "I couldn't identify that commodity. Right now I have worked " +
            "examples for roasted peanuts, fresh okra, fresh mango, whole milk " +
            "powder and ground chilli powder. Try one of these, or name the " +
            "product along with its fat and moisture content and I'll work from " +
            "first principles.",
        suggestions = FIXTURE_SUGGESTIONS
    )

    private fun nextId(): String = "msg-${sequence++}"
}
