package com.example.condominium_maintenance_system.ui.screens.requests

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.example.condominium_maintenance_system.R
import com.example.condominium_maintenance_system.ui.*
import com.example.condominium_maintenance_system.ui.components.*
import com.example.condominium_maintenance_system.ui.screens.common.*
import com.example.condominium_maintenance_system.ui.theme.*

@Composable
fun RequestsScreen(
    role: UserRole,
    requests: List<MaintenanceRequest>,
    workOrders: List<WorkOrder>,
    onBack: () -> Unit,
    onNewRequest: () -> Unit,
    onAction: (String) -> Unit
) {
    var managementTab by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            code = "M2",
            title = "Maintenance",
            onBack = onBack,
            trailing = if (role == UserRole.RESIDENT) GlyphType.ADD else GlyphType.CALENDAR,
            onTrailing = {
                if (role == UserRole.RESIDENT) onNewRequest() else onAction("New maintenance schedule opened.")
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    if (role == UserRole.RESIDENT) "Your maintenance" else "Maintenance operations",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (role == UserRole.RESIDENT) {
                        "Follow each request from your first report to the completed repair."
                    } else {
                        "Manage complaints, schedules and technician work orders."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (role == UserRole.RESIDENT) {
                item {
                    ActiveMaintenanceCard(requests.firstOrNull())
                }
                item {
                    SectionTitle("Previous requests", action = "View all") { onAction("All requests opened.") }
                }
                items(requests.drop(1)) { request ->
                    RequestListCard(request)
                }
                item {
                    PrimaryActionButton("Report a new issue", onClick = onNewRequest)
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard("Open work", "12", Modifier.weight(1f), highlighted = true)
                        MetricCard("Urgent", "03", Modifier.weight(1f))
                        MetricCard("Done", "28", Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        FilterChip(
                            selected = managementTab == 0,
                            onClick = { managementTab = 0 },
                            label = { Text("Work orders") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            selected = managementTab == 1,
                            onClick = { managementTab = 1 },
                            label = { Text("Schedule") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                if (managementTab == 0) {
                    item {
                        SectionTitle("Work order queue", action = "View all") { onAction("All work orders opened.") }
                    }
                    items(workOrders) { order ->
                        WorkOrderCard(order, onAssign = { onAction("Technician assignment opened.") })
                    }
                } else {
                    item { WeekStrip() }
                    item {
                        SectionTitle("Planned maintenance", action = "Add schedule") {
                            onAction("New maintenance schedule opened.")
                        }
                    }
                    item {
                        ScheduleCard("Lift inspection · Tower A", "Tomorrow · 10:00 AM", "Vendor visit", PillTone.GOLD)
                    }
                    item {
                        ScheduleCard("Pool pump check · Level 5", "Fri · 2:30 PM", "Jason C.", PillTone.BLUE)
                    }
                    item {
                        ScheduleCard("Fire alarm inspection", "18 Nov · 9:00 AM", "All towers", PillTone.PURPLE)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveMaintenanceCard(request: MaintenanceRequest?) {
    if (request == null) {
        EmptyStateCard("No active request", "Report an issue when something needs attention.")
        return
    }
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(CoralPale, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Glyph(GlyphType.WARNING, Coral, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(request.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${request.reference} · ${request.detail}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(request.status, PillTone.GOLD)
        }
        Spacer(Modifier.height(17.dp))
        TimelineStep(true, "Report submitted")
        TimelineStep(true, "Work order assigned")
        TimelineStep(true, "Technician is on the way · ${request.time}", active = true)
        TimelineStep(false, "Completed and closed", last = true)
    }
}

@Composable
private fun TimelineStep(done: Boolean, text: String, active: Boolean = false, last: Boolean = false) {
    Row(modifier = Modifier.height(if (last) 26.dp else 38.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .background(
                        when {
                            active -> Gold
                            done -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.outline
                        },
                        CircleShape
                    )
                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
            if (!last) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(25.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RequestListCard(request: MaintenanceRequest) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(request.title, style = MaterialTheme.typography.labelLarge)
                Text(
                    "${request.reference} · ${request.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                request.status,
                if (request.status == "Completed") PillTone.BLUE else PillTone.GOLD
            )
        }
    }
}

@Composable
private fun WorkOrderCard(order: WorkOrder, onAssign: () -> Unit) {
    CondoCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CoralPale, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Glyph(GlyphType.WORK, Coral, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text("${order.reference} · ${order.title}", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(3.dp))
                Text(
                    "${order.unit} · ${order.technician} · ${order.due}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            StatusPill(order.priority, if (order.priority == "Urgent") PillTone.CORAL else PillTone.GOLD)
            Text(
                "Assign",
                modifier = Modifier
                    .clickable(onClick = onAssign)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ScheduleCard(title: String, detail: String, assignee: String, tone: PillTone) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge)
                Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusPill(assignee, tone)
        }
    }
}
