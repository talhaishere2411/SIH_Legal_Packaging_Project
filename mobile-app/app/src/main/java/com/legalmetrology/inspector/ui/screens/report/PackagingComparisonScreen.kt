package com.legalmetrology.inspector.ui.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.legalmetrology.inspector.domain.model.PackagingMaterial
import com.legalmetrology.inspector.ui.screens.chat.CardSurface
import com.legalmetrology.inspector.ui.screens.chat.MaterialSpecRow
import com.legalmetrology.inspector.ui.screens.chat.SectionTitle
import com.legalmetrology.inspector.ui.theme.Gray100
import com.legalmetrology.inspector.ui.theme.Gray300
import com.legalmetrology.inspector.ui.theme.Gray500
import com.legalmetrology.inspector.ui.theme.Indigo500
import com.legalmetrology.inspector.ui.theme.Navy900
import com.legalmetrology.inspector.ui.theme.White

/** Side-by-side trade-off view for the recommended material and alternatives. */
@Composable
fun PackagingComparisonScreen(
    onBack: () -> Unit,
    viewModel: PackagingReportViewModel = hiltViewModel()
) {
    val recommendation = viewModel.recommendation
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Gray300)
            }
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text("Compare pack choices", style = MaterialTheme.typography.titleLarge, color = Gray100, fontWeight = FontWeight.Bold)
                Text(
                    recommendation?.commodity?.name ?: "Report unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }

        if (recommendation == null) {
            Text(
                "This report is no longer in memory. Return to chat and generate it again.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray300
            )
        } else {
            Text(
                "Swipe sideways to compare the same barrier figures, cost index and end-of-life trade-offs.",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComparisonMaterialCard(
                    heading = "Recommended",
                    material = recommendation.recommended,
                    pickIf = "You need the authored barrier margin and the stated shelf-life claim."
                )
                recommendation.alternatives.take(2).forEach { alternative ->
                    ComparisonMaterialCard(
                        heading = alternative.kind.displayName,
                        material = alternative.material,
                        pickIf = alternative.tradeoff
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonMaterialCard(
    heading: String,
    material: PackagingMaterial,
    pickIf: String
) {
    CardSurface(modifier = Modifier.width(286.dp)) {
        Column {
            SectionTitle(heading, Indigo500)
            Spacer(Modifier.height(8.dp))
            MaterialSpecRow(material)
            Spacer(Modifier.height(8.dp))
            Text("Cost index", style = MaterialTheme.typography.labelSmall, color = Gray500)
            Text("${material.costIndex} / 5", style = MaterialTheme.typography.bodyMedium, color = White, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Recyclable: ${if (material.recyclable) "yes" else "no"} · Compostable: ${if (material.compostableIs17088) "IS/ISO 17088" else "no"}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray300
            )
            Spacer(Modifier.height(8.dp))
            Text("Pick this if…", style = MaterialTheme.typography.labelSmall, color = Gray500)
            Text(pickIf, style = MaterialTheme.typography.bodySmall, color = Gray300)
        }
    }
}
