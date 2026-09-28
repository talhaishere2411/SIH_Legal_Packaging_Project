package com.legalmetrology.inspector.ui.screens.report

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.legalmetrology.inspector.domain.model.Alternative
import com.legalmetrology.inspector.domain.model.MapRecommendation
import com.legalmetrology.inspector.domain.model.RecommendationResponse
import com.legalmetrology.inspector.ui.screens.chat.CardSurface
import com.legalmetrology.inspector.ui.screens.chat.CitationRow
import com.legalmetrology.inspector.ui.screens.chat.ComplianceRow
import com.legalmetrology.inspector.ui.screens.chat.MaterialSpecRow
import com.legalmetrology.inspector.ui.screens.chat.SectionTitle
import com.legalmetrology.inspector.ui.screens.chat.SourceBadge
import com.legalmetrology.inspector.ui.screens.chat.fmt
import com.legalmetrology.inspector.ui.theme.Amber500
import com.legalmetrology.inspector.ui.theme.Blue500
import com.legalmetrology.inspector.ui.theme.Emerald500
import com.legalmetrology.inspector.ui.theme.Gray100
import com.legalmetrology.inspector.ui.theme.Gray300
import com.legalmetrology.inspector.ui.theme.Gray500
import com.legalmetrology.inspector.ui.theme.Indigo500
import com.legalmetrology.inspector.ui.theme.Navy700
import com.legalmetrology.inspector.ui.theme.Navy900
import com.legalmetrology.inspector.ui.theme.White

/**
 * Full packaging recommendation report.
 *
 * Every number on this screen is either calculated by the barrier model,
 * looked up in the materials database, or quoted from a regulation — and
 * each section carries a [SourceBadge] saying which. That is the whole
 * point of the product: a manufacturer should be able to defend this
 * report to an inspector.
 */
@Composable
fun PackagingReportScreen(
    onBack: () -> Unit,
    onValidateLabel: () -> Unit,
    viewModel: PackagingReportViewModel = hiltViewModel()
) {
    val recommendation = viewModel.recommendation

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Navy900,
                        0.4f to Indigo500.copy(alpha = 0.08f),
                        1f to Navy900
                    )
                )
            )
    ) {
        if (recommendation == null) {
            MissingReport(onBack = onBack)
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            ReportHeader(recommendation, onBack)

            Spacer(Modifier.height(18.dp))
            StructureSection(recommendation)

            Spacer(Modifier.height(14.dp))
            BarrierSection(recommendation)

            recommendation.map?.let {
                Spacer(Modifier.height(14.dp))
                MapSection(it)
            }

            Spacer(Modifier.height(14.dp))
            ComplianceSection(recommendation)

            if (recommendation.alternatives.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                AlternativesSection(recommendation)
            }

            Spacer(Modifier.height(14.dp))
            AssumptionsSection(recommendation)

            Spacer(Modifier.height(14.dp))
            CardSurface {
                SectionTitle("Next step")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Once you have artwork on the recommended structure, scan a " +
                        "printed prototype next to the 4×4 ArUco marker to check " +
                        "font heights, contrast and mandatory declarations before " +
                        "you commit to a print run.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray300
                )
                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Navy700, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .clickable(onClick = onValidateLabel)
                        .padding(horizontal = 14.dp, vertical = 13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Validate my printed design",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                        Icon(
                            Icons.Default.ArrowForward,
                            null,
                            tint = Indigo500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
        }
    }
}

// --- Sections --------------------------------------------------------------

@Composable
private fun ReportHeader(recommendation: RecommendationResponse, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                recommendation.commodity.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Gray100
            )
            recommendation.commodity.form?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Gray500)
            }
        }
        IconButton(onClick = onBack) {
            Icon(Icons.Default.Close, "Close report", tint = Gray300)
        }
    }
}

@Composable
private fun StructureSection(recommendation: RecommendationResponse) {
    CardSurface {
        SectionTitle("Recommended structure", Emerald500)
        Spacer(Modifier.height(10.dp))
        MaterialSpecRow(recommendation.recommended)
        Spacer(Modifier.height(12.dp))
        SourceBadge(recommendation.recommended.source)
    }
}

@Composable
private fun BarrierSection(recommendation: RecommendationResponse) {
    val b = recommendation.barriers
    CardSurface {
        SectionTitle("Barrier requirements")
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BarrierCell(
                label = if (b.otrTargetMin != null) "OTR window" else "OTR max",
                value = if (b.otrTargetMin != null) {
                    "${fmt(b.otrTargetMin!!)}–${fmt(b.otrTargetMax)}"
                } else {
                    "≤ ${fmt(b.otrTargetMax)}"
                },
                unit = b.otrUnit,
                condition = "${b.otrTempC}°C · ${b.otrRhPct}% RH",
                modifier = Modifier.weight(1f)
            )
            BarrierCell(
                label = "WVTR max",
                value = "≤ ${fmt(b.wvtrTargetMax)}",
                unit = b.wvtrUnit,
                condition = "${b.wvtrTempC}°C · ${b.wvtrRhPct}% RH",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "Why this number",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Gray300
        )
        Spacer(Modifier.height(5.dp))
        Text(b.rationale, style = MaterialTheme.typography.bodySmall, color = Gray500)
        Spacer(Modifier.height(12.dp))
        SourceBadge(b.source)
    }
}

@Composable
private fun BarrierCell(
    label: String,
    value: String,
    unit: String,
    condition: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Navy700, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        Spacer(Modifier.height(3.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = White)
        Text(unit, style = MaterialTheme.typography.labelSmall, color = Gray500)
        Spacer(Modifier.height(4.dp))
        Text(condition, style = MaterialTheme.typography.labelSmall, color = Gray500)
    }
}

@Composable
private fun MapSection(map: MapRecommendation) {
    CardSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Air, null, tint = Blue500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            SectionTitle("Modified atmosphere", Blue500)
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GasCell("O₂", map.o2Pct, modifier = Modifier.weight(1f))
            GasCell("CO₂", map.co2Pct, modifier = Modifier.weight(1f))
            GasCell("N₂", map.n2Pct, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        InfoLine("Storage temperature", map.storageTempC)
        map.perforation?.let { InfoLine("Perforation", it) }
        map.expectedShelfLifeDays?.let { InfoLine("Expected marketable life", "$it days") }

        Spacer(Modifier.height(12.dp))
        Text(map.notes, style = MaterialTheme.typography.bodySmall, color = Gray500)
        Spacer(Modifier.height(10.dp))
        SourceBadge(map.source)
    }
}

@Composable
private fun GasCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Navy700, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500)
        Spacer(Modifier.height(3.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = White)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Gray500, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = Gray300, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ComplianceSection(recommendation: RecommendationResponse) {
    CardSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Gavel, null, tint = Amber500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            SectionTitle("Regulatory compliance", Amber500)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "From the FSSAI (Packaging) Regulations, 2018. Hard limits are " +
                "enforced in code, not inferred by the model.",
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
        Spacer(Modifier.height(6.dp))
        recommendation.compliance.forEach { note ->
            ComplianceRow(note)
            Spacer(Modifier.height(2.dp))
            SourceBadge(note.source)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AlternativesSection(recommendation: RecommendationResponse) {
    CardSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Eco, null, tint = Emerald500, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            SectionTitle("Alternatives", Emerald500)
        }
        Spacer(Modifier.height(10.dp))
        recommendation.alternatives.forEach { alternative ->
            AlternativeBlock(alternative)
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun AlternativeBlock(alternative: Alternative) {
    Column(
        modifier = Modifier
            .background(Navy700, androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Layers, null, tint = Indigo500, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                alternative.kind.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Indigo500
            )
        }
        Spacer(Modifier.height(8.dp))
        MaterialSpecRow(alternative.material)
        Spacer(Modifier.height(8.dp))
        Text("Trade-off", style = MaterialTheme.typography.labelSmall, color = Gray500)
        Text(alternative.tradeoff, style = MaterialTheme.typography.bodySmall, color = Gray300)
    }
}

@Composable
private fun AssumptionsSection(recommendation: RecommendationResponse) {
    CardSurface {
        SectionTitle("Assumptions & sources")
        Spacer(Modifier.height(10.dp))
        recommendation.assumptions.forEach { assumption ->
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                Text("•", color = Gray500, modifier = Modifier.width(14.dp))
                Text(
                    assumption,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray300,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        CitationRow(recommendation.citations)
    }
}

@Composable
private fun MissingReport(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "This report is no longer in memory.",
            style = MaterialTheme.typography.titleMedium,
            color = Gray100
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Saved reports will be restored from local storage once the " +
                "Room-backed history is wired up.",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
        Spacer(Modifier.height(20.dp))
        IconButton(onClick = onBack) {
            Icon(Icons.Default.Close, "Back", tint = Gray300)
        }
    }
}
