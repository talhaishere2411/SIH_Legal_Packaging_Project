package com.legalmetrology.inspector.ui.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.legalmetrology.inspector.data.fixtures.FIXTURE_SUGGESTIONS
import com.legalmetrology.inspector.data.repository.RecommendationCache
import com.legalmetrology.inspector.data.repository.extractShelfLifeDays
import com.legalmetrology.inspector.data.repository.extractStorageType
import com.legalmetrology.inspector.domain.model.ChatMessage
import com.legalmetrology.inspector.domain.model.ChatRole
import com.legalmetrology.inspector.domain.model.CommodityProfile
import com.legalmetrology.inspector.domain.model.RecommendationRequest
import com.legalmetrology.inspector.domain.model.RecommendationResponse
import com.legalmetrology.inspector.domain.model.SourceKind
import com.legalmetrology.inspector.domain.model.SourceRef
import com.legalmetrology.inspector.domain.model.StorageType
import com.legalmetrology.inspector.domain.repository.RecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Values a manufacturer can correct in the inferred profile card. */
data class ProfileEdits(
    val moisturePct: Double?,
    val fatPct: Double?,
    val ph: Double?,
    val storage: StorageType
)

/**
 * Chat state for the Packaging Advisor.
 *
 * The demo is still offline, but it now behaves like a small reasoning
 * surface: scenario selection changes with shelf life/storage, profile edits
 * re-run the selected scenario, and follow-up chips resolve through an
 * answer bank instead of being mistaken for a new commodity query.
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
    private var lastRequest: RecommendationRequest? = null
    private var lastProfile: CommodityProfile? = null
    private var lastRecommendation: RecommendationResponse? = null
    private var profileMessageIndex: Int? = null
    private var recommendationMessageIndex: Int? = null

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
            // A chip belongs to the current report, not to a new commodity
            // query. The repository keeps this suspendable for the future
            // network-backed implementation.
            val current = lastRecommendation
            if (current != null) {
                val answer = repository.answerFollowUp(current.commodity.id, text, current)
                if (answer != null) {
                    delay(420)
                    _messages.add(
                        ChatMessage(
                            id = nextId(),
                            role = ChatRole.ASSISTANT,
                            text = answer,
                            suggestions = current.followUps
                        )
                    )
                    isThinking = false
                    return@launch
                }
            }

            delay(550)
            val replies = buildReplies(text)
            _messages.addAll(replies)
            isThinking = false
        }
    }

    /** Runs the deterministic peanut walkthrough used in a live demo. */
    fun playDemo() {
        if (isThinking) return
        val prompt = FIXTURE_SUGGESTIONS.first()
        _messages.clear()
        _suggestions.clear()
        _messages.add(ChatMessage(nextId(), ChatRole.USER, prompt))
        isThinking = true

        viewModelScope.launch {
            delay(650)
            val profile = repository.resolveCommodity(prompt) ?: run {
                isThinking = false
                return@launch
            }
            val storage = extractStorageType(prompt)
            val request = RecommendationRequest(
                query = prompt,
                commodityId = profile.id,
                shelfLifeDays = extractShelfLifeDays(prompt),
                storage = storage
            )
            val recommendation = repository.recommend(request) ?: run {
                isThinking = false
                return@launch
            }
            rememberRequest(profile, request, recommendation)
            cache.put(recommendation)
            appendProfileReply(profileForDisplay(profile, storage), storage)
            delay(850)
            appendRecommendationReply(recommendation)
            isThinking = false
        }
    }

    /** Re-runs the last scenario after a manufacturer corrects their profile. */
    fun editProfile(profileId: String, edits: ProfileEdits) {
        val previous = lastRequest ?: return
        val profile = lastProfile ?: return
        if (profile.id != profileId || isThinking) return

        val editedProfile = profile.copy(
            moisturePct = edits.moisturePct,
            fatPct = edits.fatPct,
            ph = edits.ph,
            defaultStorage = edits.storage,
            source = SourceRef(SourceKind.USER, "profile.override", "Your corrected profile")
        )
        val request = previous.copy(
            storage = edits.storage,
            overrides = mapOf(
                "moisturePct" to (edits.moisturePct?.toString() ?: "unknown"),
                "fatPct" to (edits.fatPct?.toString() ?: "unknown"),
                "ph" to (edits.ph?.toString() ?: "unknown")
            )
        )
        isThinking = true
        viewModelScope.launch {
            delay(420)
            val recommendation = repository.recommend(request)?.copy(commodity = editedProfile)
            if (recommendation != null) {
                lastProfile = editedProfile
                lastRequest = request
                lastRecommendation = recommendation
                cache.put(recommendation)
                profileMessageIndex?.let { index ->
                    if (index in _messages.indices) {
                        _messages[index] = _messages[index].copy(profile = editedProfile)
                    }
                }
                recommendationMessageIndex?.let { index ->
                    if (index in _messages.indices) {
                        _messages[index] = _messages[index].copy(
                            text = recommendation.narrative,
                            recommendation = recommendation,
                            suggestions = recommendation.followUps
                        )
                    }
                }
            }
            isThinking = false
        }
    }

    // --- reply construction -------------------------------------------------

    private suspend fun buildReplies(text: String): List<ChatMessage> {
        val profile = repository.resolveCommodity(text)
            ?: return listOf(unrecognisedMessage())

        val shelfLifeDays = extractShelfLifeDays(text)
        val storage = extractStorageType(text)
        val request = RecommendationRequest(
            query = text,
            commodityId = profile.id,
            shelfLifeDays = shelfLifeDays,
            storage = storage
        )
        val recommendation = repository.recommend(request)
            ?: return listOf(unrecognisedMessage())

        val displayProfile = profileForDisplay(profile, storage)
        rememberRequest(profile, request, recommendation)
        cache.put(recommendation)

        val profileReply = ChatMessage(
            id = nextId(),
            role = ChatRole.ASSISTANT,
            text = buildProfileIntro(displayProfile.name, storage?.displayName),
            profile = displayProfile
        )
        val recommendationReply = ChatMessage(
            id = nextId(),
            role = ChatRole.ASSISTANT,
            text = recommendation.narrative,
            recommendation = recommendation,
            suggestions = recommendation.followUps
        )
        profileMessageIndex = _messages.size
        recommendationMessageIndex = _messages.size + 1
        return listOf(profileReply, recommendationReply)
    }

    private fun appendProfileReply(profile: CommodityProfile, storage: StorageType?) {
        profileMessageIndex = _messages.size
        _messages.add(
            ChatMessage(
                id = nextId(),
                role = ChatRole.ASSISTANT,
                text = buildProfileIntro(profile.name, storage?.displayName),
                profile = profile
            )
        )
    }

    private fun appendRecommendationReply(recommendation: RecommendationResponse) {
        recommendationMessageIndex = _messages.size
        _messages.add(
            ChatMessage(
                id = nextId(),
                role = ChatRole.ASSISTANT,
                text = recommendation.narrative,
                recommendation = recommendation,
                suggestions = recommendation.followUps
            )
        )
    }

    private fun rememberRequest(
        profile: CommodityProfile,
        request: RecommendationRequest,
        recommendation: RecommendationResponse
    ) {
        lastProfile = profile
        lastRequest = request
        lastRecommendation = recommendation
    }

    private fun profileForDisplay(
        profile: CommodityProfile,
        storage: StorageType?
    ): CommodityProfile = if (storage == null) profile else profile.copy(defaultStorage = storage)

    private fun buildProfileIntro(name: String, storage: String?): String =
        if (storage != null) {
            "I've identified $name, stored $storage. Here's the profile I'll work " +
                "from — figures are inferred from IFCT 2017 and ICAR-CIPHET, so " +
                "correct anything wrong before I re-run the scenario."
        } else {
            "I've identified $name. Here's the profile I'll work from — figures " +
                "are inferred from IFCT 2017 and ICAR-CIPHET, so correct anything " +
                "wrong before I re-run the scenario."
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
