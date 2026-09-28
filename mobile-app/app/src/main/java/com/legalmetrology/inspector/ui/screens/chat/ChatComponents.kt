package com.legalmetrology.inspector.ui.screens.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.legalmetrology.inspector.domain.model.BarrierSpec
import com.legalmetrology.inspector.domain.model.ChatMessage
import com.legalmetrology.inspector.domain.model.ChatRole
import com.legalmetrology.inspector.domain.model.Citation
import com.legalmetrology.inspector.domain.model.CommodityProfile
import com.legalmetrology.inspector.domain.model.ComplianceNote
import com.legalmetrology.inspector.domain.model.ComplianceStatus
import com.legalmetrology.inspector.domain.model.PackagingMaterial
import com.legalmetrology.inspector.domain.model.RecommendationResponse
import com.legalmetrology.inspector.domain.model.SourceKind
import com.legalmetrology.inspector.domain.model.SourceRef
import com.legalmetrology.inspector.ui.theme.Amber500
import com.legalmetrology.inspector.ui.theme.Blue500
import com.legalmetrology.inspector.ui.theme.Coral500
import com.legalmetrology.inspector.ui.theme.Emerald500
import com.legalmetrology.inspector.ui.theme.Gray300
import com.legalmetrology.inspector.ui.theme.Gray500
import com.legalmetrology.inspector.ui.theme.Indigo200
import com.legalmetrology.inspector.ui.theme.Indigo500
import com.legalmetrology.inspector.ui.theme.Navy600
import com.legalmetrology.inspector.ui.theme.Navy700
import com.legalmetrology.inspector.ui.theme.Navy800
import com.legalmetrology.inspector.ui.theme.White

// ============================================================
// CHAT COMPONENTS
//
// The assistant never answers in plain prose alone. Every reply can
// attach a structured card — a product profile or a full recommendation
// — which is what separates this from a text box wrapped around an LLM.
// ============================================================

/** Trims trailing zeros so "1.0" reads as "1" and "0.05" stays "0.05". */
internal fun fmt(value: Double): String {
    if (value == Math.floor(value) && !value.isInfinite() && Math.abs(value) < 1e9) {
        return value.toLong().toString()
    }
    return String.format("%.2f", value).trimEnd('0').trimEnd('.')
}

// --- Message bubble --------------------------------------------------------

@Composable
fun ChatBubble(
    message: ChatMessage,
    onOpenReport: (RecommendationResponse) -> Unit,
    onSuggestion: (String) -> Unit,
    onEditProfile: (CommodityProfile) -> Unit
) {
    val isUser = message.role == ChatRole.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            Surface(
                shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                color = Indigo500,
                contentColor = White
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }
        } else {
            Column {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(start = 4.dp, end = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray300
                )
                message.profile?.let {
                    Spacer(Modifier.height(10.dp))
                    ProfileCard(profile = it, onEdit = { onEditProfile(it) })
                }
                message.recommendation?.let {
                    Spacer(Modifier.height(10.dp))
                    RecommendationCard(recommendation = it, onOpenReport = onOpenReport)
                }
            }
        }

        if (message.suggestions.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            QuickReplyRow(suggestions = message.suggestions, onClick = onSuggestion)
        }
    }
}

@Composable
fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, delayMillis = index * 140),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            Box(
                Modifier
                    .size(7.dp)
                    .alpha(alpha)
                    .background(Gray500, CircleShape)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text("Working through the barrier maths…", style = MaterialTheme.typography.labelSmall, color = Gray500)
    }
}

// --- Product profile card --------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileCard(profile: CommodityProfile, onEdit: () -> Unit) {
    CardSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Science, null, tint = Indigo500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                profile.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Spacer(Modifier.width(6.dp))
            profile.form?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = Gray500)
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Tap to correct profile values before re-running",
            modifier = Modifier.clickable(onClick = onEdit),
            style = MaterialTheme.typography.labelSmall,
            color = Indigo200
        )

        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profile.moisturePct?.let { MetricPill(Icons.Default.WaterDrop, "Moisture", "${fmt(it)}%") }
            profile.fatPct?.let { MetricPill(Icons.Default.Science, "Fat", "${fmt(it)}%") }
            profile.ph?.let { MetricPill(Icons.Default.Straighten, "pH", fmt(it)) }
            profile.respirationRate?.let {
                MetricPill(
                    Icons.Default.Air, "Respiration",
                    "${fmt(it)} mg/kg·h @ ${profile.respirationTempC ?: 20}°C"
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Thermostat, null, tint = Gray500, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "${profile.foodGroup.displayName} · default ${profile.defaultStorage.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = Gray500
            )
        }
        if (profile.chillingSensitive) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Chilling-sensitive — do not over-cool.",
                style = MaterialTheme.typography.labelSmall,
                color = Amber500
            )
        }

        Spacer(Modifier.height(10.dp))
        SourceBadge(profile.source)
    }
}

// --- Recommendation card ---------------------------------------------------

@Composable
fun RecommendationCard(
    recommendation: RecommendationResponse,
    onOpenReport: (RecommendationResponse) -> Unit
) {
    CardSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = Emerald500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Recommended structure",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Emerald500
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            recommendation.recommended.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = White
        )
        Text(
            recommendation.recommended.structure,
            style = MaterialTheme.typography.bodySmall,
            color = Gray300,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )

        Spacer(Modifier.height(14.dp))
        BarrierGrid(recommendation.barriers)

        recommendation.map?.let {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Air, null, tint = Blue500, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "MAP: O₂ ${it.o2Pct} · CO₂ ${it.co2Pct} · ${it.storageTempC}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray300
                )
            }
        }

        val actionRequired = recommendation.compliance.count {
            it.status == ComplianceStatus.ACTION_REQUIRED || it.status == ComplianceStatus.PROHIBITED
        }
        if (actionRequired > 0) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gavel, null, tint = Amber500, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "$actionRequired compliance item${if (actionRequired == 1) "" else "s"} need attention",
                    style = MaterialTheme.typography.bodySmall,
                    color = Amber500
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy700, RoundedCornerShape(10.dp))
                .clickable { onOpenReport(recommendation) }
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "View full report",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
                Icon(Icons.Default.ArrowForward, null, tint = Indigo500, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        CitationRow(recommendation.citations)
    }
}

@Composable
private fun BarrierGrid(barriers: BarrierSpec) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BarrierTile(
            label = if (barriers.otrTargetMin != null) "OTR window" else "OTR max",
            value = if (barriers.otrTargetMin != null) {
                "${fmt(barriers.otrTargetMin!!)}–${fmt(barriers.otrTargetMax)}"
            } else {
                "≤ ${fmt(barriers.otrTargetMax)}"
            },
            unit = barriers.otrUnit,
            condition = "${barriers.otrTempC}°C / ${barriers.otrRhPct}% RH",
            modifier = Modifier.weight(1f)
        )
        BarrierTile(
            label = "WVTR max",
            value = "≤ ${fmt(barriers.wvtrTargetMax)}",
            unit = barriers.wvtrUnit,
            condition = "${barriers.wvtrTempC}°C / ${barriers.wvtrRhPct}% RH",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BarrierTile(
    label: String,
    value: String,
    unit: String,
    condition: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Navy700, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        Spacer(Modifier.height(3.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = White)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = Gray500)
        Spacer(Modifier.height(4.dp))
        Text(condition, style = MaterialTheme.typography.labelSmall, color = Gray500, fontSize = 9.sp)
    }
}

// --- Compliance ------------------------------------------------------------

@Composable
fun ComplianceRow(note: ComplianceNote) {
    val (tint, icon) = when (note.status) {
        ComplianceStatus.MET -> Emerald500 to Icons.Default.CheckCircle
        ComplianceStatus.ACTION_REQUIRED -> Amber500 to Icons.Default.Warning
        ComplianceStatus.PROHIBITED -> Coral500 to Icons.Default.Warning
        ComplianceStatus.NOT_APPLICABLE -> Gray500 to Icons.Default.CheckCircle
    }
    Row(modifier = Modifier.padding(vertical = 7.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(note.requirement, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = White)
            Text(note.limit, style = MaterialTheme.typography.bodySmall, color = Gray300)
            note.detail?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
            note.isReference?.let {
                Spacer(Modifier.height(3.dp))
                Text(it, style = MaterialTheme.typography.labelSmall, color = Indigo200)
            }
        }
    }
}

// --- Material spec ---------------------------------------------------------

@Composable
fun MaterialSpecRow(material: PackagingMaterial) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(material.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = White)
        Text(
            material.structure,
            style = MaterialTheme.typography.bodySmall,
            color = Gray300,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SpecItem("Thickness", material.thicknessUm)
            SpecItem("Seal", material.sealability)
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SpecItem("OTR", "${fmt(material.otr)} ${material.otrUnitOf()}")
            SpecItem("WVTR", "${fmt(material.wvtr)} ${material.wvtrUnitOf()}")
        }
        if (material.isCodes.isNotEmpty()) {
            Spacer(Modifier.height(5.dp))
            Text(
                "Standards: ${material.isCodes.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = Indigo200
            )
        }
        if (material.compostableIs17088) {
            Spacer(Modifier.height(3.dp))
            Text(
                "Compostable to IS/ISO 17088",
                style = MaterialTheme.typography.labelSmall,
                color = Emerald500
            )
        }
    }
}

private fun PackagingMaterial.otrUnitOf(): String = "cm³/m²·day @ ${otrTempC}°C"
private fun PackagingMaterial.wvtrUnitOf(): String = "g/m²·day @ ${wvtrTempC}°C"

@Composable
private fun RowScope.SpecItem(label: String, value: String) {
    Column(modifier = Modifier.weight(1f)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        Text(value, style = MaterialTheme.typography.bodySmall, color = Gray300)
    }
}

// --- Chips & badges --------------------------------------------------------

@Composable
fun QuickReplyRow(suggestions: List<String>, onClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { onClick(suggestion) },
                label = {
                    Text(
                        suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = Navy700,
                    labelColor = Gray300
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = true,
                    borderColor = Navy600
                )
            )
        }
    }
}

@Composable
private fun MetricPill(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .background(Navy700, RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Indigo500, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(7.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500, fontSize = 9.sp)
            Text(value, style = MaterialTheme.typography.labelMedium, color = White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SourceBadge(source: SourceRef) {
    val tint = when (source.kind) {
        SourceKind.CALC -> Emerald500
        SourceKind.DB -> Blue500
        SourceKind.RAG -> Indigo500
        SourceKind.USER -> Amber500
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Verified, null, tint = tint, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            "${source.kind.displayName} · ${source.label}",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500,
            fontSize = 10.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CitationRow(citations: List<Citation>) {
    Column {
        Text("Sources", style = MaterialTheme.typography.labelSmall, color = Gray500)
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            citations.forEach { citation ->
                AssistChip(
                    onClick = { /* TODO: open source URL in a web view */ },
                    label = {
                        Text(
                            "${citation.title} · ${citation.locator}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Gavel, null, tint = Gray500, modifier = Modifier.size(13.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Navy700,
                        labelColor = Gray300
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = Navy600
                    )
                )
            }
        }
    }
}

// --- Shared surface --------------------------------------------------------

@Composable
fun CardSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Navy800,
        border = androidx.compose.foundation.BorderStroke(1.dp, Navy600),
        modifier = Modifier.fillMaxWidth().then(modifier)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun SectionTitle(text: String, tint: Color = Indigo500) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = tint
    )
}
