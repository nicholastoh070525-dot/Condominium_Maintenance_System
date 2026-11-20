package com.example.condominium_maintenance_system.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.ui.VisitorRequest
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.PillTone
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.components.StatusPill
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.BlueDeep
import com.example.condominium_maintenance_system.ui.theme.BluePale

@Composable
fun VisitorDetailScreen(
    visitor: VisitorRequest,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = "Visitor details", onBack = onBack, trailing = GlyphType.CARD)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(BlueDeep, MaterialTheme.colorScheme.primary)
                                ),
                                RoundedCornerShape(26.dp)
                            )
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .background(BluePale, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Glyph(
                                GlyphType.CARD,
                                MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                visitor.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "${visitor.vehicleColor} · ${visitor.plate}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD6E1F9)
                            )
                            Spacer(Modifier.height(7.dp))
                            Text(
                                visitor.passReference ?: "Completed visit",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFB9CCF5)
                            )
                        }
                        StatusPill("Complete", PillTone.BLUE)
                    }
                }
            }

            item {
                SectionTitle("Details")
            }
            item {
                CondoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        DetailRow("Visitor", visitor.name)
                        DetailRow("Unit", visitor.unitCode.ifBlank { "—" })
                        DetailRow("Resident", visitor.residentName.ifBlank { "—" })
                        DetailRow("Vehicle color", visitor.vehicleColor)
                        DetailRow("Number plate", visitor.plate)
                        DetailRow("Visit time", displayVisitTime(visitor.visitTime))
                        if (visitor.note.isNotBlank()) {
                            DetailRow("Note", visitor.note)
                        }
                    }
                }
            }

            item {
                SectionTitle("Visit timeline")
            }
            item {
                CondoCard {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        DetailRow("Approved at", displayVisitTime(visitor.approvedAt.orEmpty()))
                        DetailRow("Completed at", displayVisitTime(visitor.completedAt.orEmpty()))
                        DetailRow("Status", "Complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
