package com.legalmetrology.inspector.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.legalmetrology.inspector.ui.theme.Gray100
import com.legalmetrology.inspector.ui.theme.Gray300
import com.legalmetrology.inspector.domain.model.CommodityProfile
import com.legalmetrology.inspector.domain.model.StorageType
import com.legalmetrology.inspector.ui.theme.Emerald500
import com.legalmetrology.inspector.ui.theme.Gray500
import com.legalmetrology.inspector.ui.theme.Indigo500
import com.legalmetrology.inspector.ui.theme.Navy600
import com.legalmetrology.inspector.ui.theme.Navy700
import com.legalmetrology.inspector.ui.theme.Navy900
import com.legalmetrology.inspector.ui.theme.White

/**
 * Packaging Advisor — the main surface.
 *
 * The user describes what they need to pack in plain language; the
 * assistant answers with a structured product profile and a packaging
 * recommendation, both rendered as cards rather than prose.
 *
 * The screen holds no recommendation logic. It renders whatever the
 * [ChatViewModel] produces, which in turn comes from
 * [com.legalmetrology.inspector.domain.repository.RecommendationRepository].
 */
@Composable
fun ChatScreen(
    onOpenReport: (String) -> Unit,
    onOpenLabelValidator: () -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    var draft by remember { mutableStateOf("") }
    var editingProfile by remember { mutableStateOf<com.legalmetrology.inspector.domain.model.CommodityProfile?>(null) }

    // Keep the newest message in view.
    LaunchedEffect(viewModel.messages.size, viewModel.isThinking) {
        val lastIndex = viewModel.messages.lastIndex
        if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Navy900,
                        0.45f to Indigo500.copy(alpha = 0.07f),
                        1f to Navy900
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            ChatTopBar(
                onOpenHistory = onOpenHistory,
                onOpenLabelValidator = onOpenLabelValidator
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 8.dp,
                    bottom = 16.dp
                )
            ) {
                if (viewModel.messages.isEmpty()) {
                    item {
                        EmptyState(
                            suggestions = viewModel.suggestions,
                            onPick = { viewModel.send(it) },
                            onPlayDemo = { viewModel.playDemo() }
                        )
                    }
                }

                items(viewModel.messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        onOpenReport = { it.requestId.let(onOpenReport) },
                        onSuggestion = { viewModel.send(it) },
                        onEditProfile = { editingProfile = it }
                    )
                }

                if (viewModel.isThinking) {
                    item { TypingIndicator() }
                }
            }

            ComposerBar(
                draft = draft,
                onDraftChange = { draft = it },
                onSend = {
                    viewModel.send(draft)
                    draft = ""
                },
                sendEnabled = draft.isNotBlank() && !viewModel.isThinking
            )
        }
    }

    editingProfile?.let { profile ->
        ProfileEditorDialog(
            profile = profile,
            onDismiss = { editingProfile = null },
            onSave = { edits ->
                viewModel.editProfile(profile.id, edits)
                editingProfile = null
            }
        )
    }
}

// --- Top bar ---------------------------------------------------------------

@Composable
private fun ChatTopBar(
    onOpenHistory: () -> Unit,
    onOpenLabelValidator: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Indigo500, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(
                    "Packaging Advisor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray100
                )
                Text(
                    "Grounded in FSSAI 2018 · IS standards",
                    style = MaterialTheme.typography.labelSmall,
                    color = Indigo500
                )
            }
        }
        Row {
            IconButton(onClick = onOpenLabelValidator) {
                Icon(Icons.Default.QrCodeScanner, "Validate a printed design", tint = Gray300)
            }
            IconButton(onClick = onOpenHistory) {
                Icon(Icons.Default.History, "Saved reports", tint = Gray300)
            }
        }
    }
}

// --- Empty state -----------------------------------------------------------

@Composable
private fun EmptyState(
    suggestions: List<String>,
    onPick: (String) -> Unit,
    onPlayDemo: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        CardSurface {
            Icon(Icons.Default.AutoAwesome, null, tint = Indigo500, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(10.dp))
            Text(
                "Describe what you need to pack",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray100
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Tell me the commodity, how long it needs to last and how you " +
                    "store it. I'll infer the rest — moisture, fat content, " +
                    "respiration rate — from IFCT 2017 and ICAR-CIPHET, and " +
                    "recommend a structure with the barrier numbers to back it up.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray300
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "▶  Play the 6-month peanut demo",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPlayDemo)
                    .background(Navy700, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Emerald500
            )
            Spacer(Modifier.height(16.dp))
            Text("Or try one of these", style = MaterialTheme.typography.labelMedium, color = Gray500)
            Spacer(Modifier.height(8.dp))
            QuickReplyRow(suggestions = suggestions, onClick = onPick)
        }
    }
}

@Composable
private fun ProfileEditorDialog(
    profile: CommodityProfile,
    onDismiss: () -> Unit,
    onSave: (ProfileEdits) -> Unit
) {
    var moisture by remember { mutableStateOf(profile.moisturePct?.toString().orEmpty()) }
    var fat by remember { mutableStateOf(profile.fatPct?.toString().orEmpty()) }
    var ph by remember { mutableStateOf(profile.ph?.toString().orEmpty()) }
    var storage by remember { mutableStateOf(profile.defaultStorage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Correct product profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "These values are your inputs. The demo selects the nearest authored scenario and flags the assumption in the report.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = moisture,
                    onValueChange = { moisture = it },
                    label = { Text("Moisture (%)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat (%)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = ph,
                    onValueChange = { ph = it },
                    label = { Text("pH") },
                    singleLine = true
                )
                Text("Storage", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StorageType.values().forEach { option ->
                        TextButton(onClick = { storage = option }) {
                            Text(
                                option.displayName,
                                fontWeight = if (storage == option) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ProfileEdits(
                            moisturePct = moisture.toDoubleOrNull(),
                            fatPct = fat.toDoubleOrNull(),
                            ph = ph.toDoubleOrNull(),
                            storage = storage
                        )
                    )
                }
            ) { Text("Re-run recommendation") }
        }
    )
}

// --- Composer --------------------------------------------------------------

@Composable
private fun ComposerBar(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("e.g. roasted peanuts, 6 months, ambient", color = Gray500)
            },
            shape = RoundedCornerShape(22.dp),
            maxLines = 4,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Gray100),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Navy700,
                unfocusedContainerColor = Navy700,
                disabledContainerColor = Navy700,
                focusedBorderColor = Indigo500,
                unfocusedBorderColor = Navy600,
                cursorColor = Indigo500
            )
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (sendEnabled) Indigo500 else Navy700,
                    CircleShape
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onSend, enabled = sendEnabled) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Send",
                    tint = if (sendEnabled) White else Gray500
                )
            }
        }
    }
}
