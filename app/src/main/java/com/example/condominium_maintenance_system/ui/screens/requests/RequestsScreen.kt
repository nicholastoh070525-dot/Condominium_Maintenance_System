@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.condominium_maintenance_system.ui.screens.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.condominium_maintenance_system.data.model.MaintenanceRules
import com.example.condominium_maintenance_system.ui.MaintenanceRequest
import com.example.condominium_maintenance_system.ui.MaintenanceRequestUpdate
import com.example.condominium_maintenance_system.ui.MaintenanceTask
import com.example.condominium_maintenance_system.ui.MaintenanceTaskDraft
import com.example.condominium_maintenance_system.ui.UserProfile
import com.example.condominium_maintenance_system.ui.UserRole
import com.example.condominium_maintenance_system.ui.components.CondoCard
import com.example.condominium_maintenance_system.ui.components.EmptyStateCard
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.components.InfoBanner
import com.example.condominium_maintenance_system.ui.components.PillTone
import com.example.condominium_maintenance_system.ui.components.PrimaryActionButton
import com.example.condominium_maintenance_system.ui.components.SectionTitle
import com.example.condominium_maintenance_system.ui.components.StatusPill
import com.example.condominium_maintenance_system.ui.screens.common.ScreenHeader
import com.example.condominium_maintenance_system.ui.theme.BluePale
import com.example.condominium_maintenance_system.ui.theme.Coral
import com.example.condominium_maintenance_system.ui.theme.CoralPale
import com.example.condominium_maintenance_system.ui.theme.Gold
import com.example.condominium_maintenance_system.ui.theme.GoldPale
import com.example.condominium_maintenance_system.ui.theme.Purple
import com.example.condominium_maintenance_system.ui.theme.PurplePale
import com.example.condominium_maintenance_system.ui.theme.Success
import com.example.condominium_maintenance_system.ui.theme.SuccessPale
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private data class TimeOption(
    val label: String,
    val hour: Int,
    val minute: Int
)

private val maintenanceTimes = listOf(
    TimeOption("9:00 AM", 9, 0),
    TimeOption("10:30 AM", 10, 30),
    TimeOption("12:00 PM", 12, 0),
    TimeOption("2:00 PM", 14, 0),
    TimeOption("3:30 PM", 15, 30),
    TimeOption("5:00 PM", 17, 0)
)

@Composable
fun RequestsScreen(
    role: UserRole,
    profile: UserProfile,
    requests: List<MaintenanceRequest>,
    tasks: List<MaintenanceTask>,
    loading: Boolean,
    saving: Boolean,
    processingId: String?,
    initialRequestId: String? = null,
    onBack: () -> Unit,
    onNewRequest: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateRequest: (MaintenanceRequest, MaintenanceRequestUpdate) -> Unit,
    onDeleteRequest: (MaintenanceRequest) -> Unit,
    onCreateTask: (MaintenanceTaskDraft) -> Unit,
    onUpdateTask: (MaintenanceTask, String, String?) -> Unit,
    onInitialRequestHandled: () -> Unit = {}
) {
    var managementTab by remember { mutableIntStateOf(0) }
    var requestFilter by remember { mutableStateOf("Open") }
    var detailRequest by remember { mutableStateOf<MaintenanceRequest?>(null) }
    var manageRequest by remember { mutableStateOf<MaintenanceRequest?>(null) }
    var deleteRequest by remember { mutableStateOf<MaintenanceRequest?>(null) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }
    var showCreateTask by remember { mutableStateOf(false) }
    var completeTask by remember { mutableStateOf<MaintenanceTask?>(null) }
    LaunchedEffect(initialRequestId, requests) {
        val target = initialRequestId?.let { id -> requests.firstOrNull { it.id == id } }
        if (target != null) {
            detailRequest = target
            onInitialRequestHandled()
        }
    }
    val today = remember { Calendar.getInstance() }
    var calendarMonthOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }
    val calendarMonth = remember(calendarMonthOffset) {
        (today.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, calendarMonthOffset)
        }
    }

    val filteredRequests = when (requestFilter) {
        "Open" -> requests.filter { MaintenanceRules.isOpen(it.status) }
        "Completed" -> requests.filter { it.status == "COMPLETED" }
        else -> requests
    }
    val openRequests = requests.filter { MaintenanceRules.isOpen(it.status) }
    val pendingRequests = requests.filter {
        MaintenanceRules.normalizeRequestStatus(it.status) == "PENDING"
    }
    val inProgressRequests = requests.filter {
        MaintenanceRules.normalizeRequestStatus(it.status) == "IN_PROGRESS"
    }
    val completedRequests = requests.filter {
        MaintenanceRules.normalizeRequestStatus(it.status) == "COMPLETED"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Maintenance",
            onBack = onBack,
            trailing = if (role == UserRole.RESIDENT) GlyphType.ADD else GlyphType.CALENDAR,
            onTrailing = {
                if (role == UserRole.RESIDENT) {
                    onNewRequest()
                } else {
                    managementTab = 2
                }
            }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MaintenancePageIntro(
                    title = if (role == UserRole.RESIDENT) "Your maintenance"
                    else "Maintenance operations",
                    subtitle = if (role == UserRole.RESIDENT) {
                        "Report an issue, add photos and follow every update until the repair is complete."
                    } else {
                        "Review complaints, assign schedules, record reports and complete your tasks."
                    }
                )
            }

            if (loading && requests.isEmpty() && tasks.isEmpty()) {
                item {
                    CondoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Loading maintenance data…")
                        }
                    }
                }
            }

            if (role == UserRole.RESIDENT) {
                item {
                    ResidentComplaintSummary(
                        pending = pendingRequests.size,
                        inProgress = inProgressRequests.size,
                        totalActive = openRequests.size
                    )
                }
                val active = openRequests.firstOrNull()
                item {
                    SectionTitle("Current request")
                }
                if (active == null) {
                    item {
                        EmptyStateCard(
                            "No active request",
                            if (requests.isEmpty()) "Report an issue when something needs attention."
                            else "All your maintenance requests are complete."
                        )
                    }
                } else {
                    item {
                        ActiveMaintenanceCard(active) { detailRequest = active }
                    }
                }
                val otherOpenRequests = openRequests.drop(1)
                if (otherOpenRequests.isNotEmpty()) {
                    item {
                        SectionTitle("Other open complaints")
                    }
                    items(otherOpenRequests, key = { "open-${it.id}" }) { request ->
                        RequestListCard(request, role) { detailRequest = request }
                    }
                }
                item {
                    SectionTitle(
                        "Request history",
                        action = "Refresh",
                        onAction = onRefresh
                    )
                }
                if (completedRequests.isEmpty()) {
                    item {
                        EmptyStateCard(
                            if (requests.isEmpty()) "Start with your first report"
                            else "No completed requests yet",
                            if (requests.isEmpty()) {
                                "Add a description and optional photos so management can respond faster."
                            } else {
                                "Completed complaints will be kept here for reference."
                            }
                        )
                    }
                } else {
                    items(completedRequests, key = { "history-${it.id}" }) { request ->
                        RequestListCard(request, role) { detailRequest = request }
                    }
                }
                if (tasks.isNotEmpty()) {
                    item {
                        SectionTitle("Scheduled work for your unit")
                    }
                    items(tasks.take(3), key = { "resident-task-${it.id}" }) { task ->
                        CompactTaskCard(task)
                    }
                }
                item {
                    PrimaryActionButton("Report a new issue", onClick = onNewRequest)
                }
            } else {
                item {
                    ManagementComplaintSummary(
                        open = openRequests.size,
                        urgent = openRequests.count { it.urgency == "Urgent" },
                        myTasks = tasks.count {
                            it.assigneeId == profile.id && it.status != "COMPLETED"
                        }
                    )
                }
                item {
                    SegmentedControl(
                        labels = listOf("Requests", "My tasks", "Calendar"),
                        selectedIndex = managementTab,
                        onSelected = { managementTab = it }
                    )
                }

                when (managementTab) {
                    0 -> {
                        item {
                            val filters = listOf("Open", "Completed", "All")
                            SegmentedControl(
                                labels = filters,
                                selectedIndex = filters.indexOf(requestFilter).coerceAtLeast(0),
                                onSelected = { requestFilter = filters[it] },
                                compact = true
                            )
                        }
                        item {
                            SectionTitle(
                                "Complaint queue",
                                action = "Refresh",
                                onAction = onRefresh
                            )
                        }
                        if (filteredRequests.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    "No requests in this view",
                                    "New resident complaints will appear here when they are submitted."
                                )
                            }
                        } else {
                            items(filteredRequests, key = { "request-${it.id}" }) { request ->
                                ManagementRequestCard(
                                    request = request,
                                    processing = processingId == request.id
                                ) {
                                    detailRequest = request
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            SectionTitle(
                                "Assigned tasks",
                                action = "Add schedule",
                                onAction = { showCreateTask = true }
                            )
                        }
                        item {
                            InfoBanner(
                                title = "Ownership matters",
                                body = "Tasks are assigned to the manager who creates them. Only that manager can move their task to in progress or completed.",
                                glyph = GlyphType.WORK,
                                badge = "${tasks.count { it.assigneeId == profile.id }} mine"
                            )
                        }
                        if (tasks.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    "No maintenance tasks",
                                    "Schedule preventive work or a repair visit to populate the maintenance calendar."
                                )
                            }
                        } else {
                            items(tasks, key = { "task-${it.id}" }) { task ->
                                ManagementTaskCard(
                                    task = task,
                                    profile = profile,
                                    processing = processingId == task.id,
                                    onStart = {
                                        onUpdateTask(task, "IN_PROGRESS", null)
                                    },
                                    onComplete = { completeTask = task }
                                )
                            }
                        }
                        item {
                            PrimaryActionButton("Schedule a maintenance task") {
                                showCreateTask = true
                            }
                        }
                    }

                    else -> {
                        item {
                            MaintenanceCalendar(
                                month = calendarMonth,
                                selectedDay = selectedDay,
                                tasks = tasks,
                                requests = requests,
                                onPreviousMonth = {
                                    calendarMonthOffset -= 1
                                    selectedDay = 1
                                },
                                onNextMonth = {
                                    calendarMonthOffset += 1
                                    selectedDay = 1
                                },
                                onSelectDay = { selectedDay = it }
                            )
                        }
                        item {
                            SectionTitle(
                                "Agenda",
                                action = "Add task",
                                onAction = { showCreateTask = true }
                            )
                        }
                        item {
                            CalendarAgenda(
                                month = calendarMonth,
                                day = selectedDay,
                                tasks = tasks,
                                requests = requests
                            )
                        }
                    }
                }
            }
        }
    }

    detailRequest?.let { request ->
        val latest = requests.firstOrNull { it.id == request.id } ?: request
        val manageLabel = when (MaintenanceRules.normalizeRequestStatus(latest.status)) {
            "PENDING" -> "Assign and start"
            "IN_PROGRESS" -> "Complete job"
            else -> null
        }
        RequestDetailDialog(
            request = latest,
            role = role,
            onDismiss = { detailRequest = null },
            onPhotoClick = { selectedPhotoUrl = it },
            onDelete = if (role == UserRole.RESIDENT && latest.residentId == profile.id) {
                {
                    detailRequest = null
                    deleteRequest = latest
                }
            } else {
                null
            },
            manageLabel = manageLabel,
            onManage = if (role == UserRole.MANAGEMENT && manageLabel != null) {
                {
                    detailRequest = null
                    manageRequest = latest
                }
            } else {
                null
            }
        )
    }

    deleteRequest?.let { request ->
        ConfirmDeleteComplaintDialog(
            deleting = processingId == request.id,
            onDismiss = { deleteRequest = null },
            onConfirm = {
                onDeleteRequest(request)
                deleteRequest = null
            }
        )
    }

    selectedPhotoUrl?.let { photoUrl ->
        PhotoViewerDialog(
            photoUrl = photoUrl,
            onDismiss = { selectedPhotoUrl = null }
        )
    }

    manageRequest?.let { request ->
        when (MaintenanceRules.normalizeRequestStatus(request.status)) {
            "PENDING" -> AssignRequestDialog(
                request = request,
                saving = processingId == request.id,
                onDismiss = { manageRequest = null },
                onSubmit = { technician, scheduledAt ->
                    onUpdateRequest(
                        request,
                        MaintenanceRequestUpdate(
                            status = "IN_PROGRESS",
                            technicianName = technician,
                            scheduledAt = scheduledAt,
                            report = null
                        )
                    )
                    manageRequest = null
                }
            )

            "IN_PROGRESS" -> CompleteRequestDialog(
                request = request,
                saving = processingId == request.id,
                onDismiss = { manageRequest = null },
                onSubmit = { report ->
                    onUpdateRequest(
                        request,
                        MaintenanceRequestUpdate(
                            status = "COMPLETED",
                            technicianName = request.technicianName,
                            scheduledAt = request.scheduledAt,
                            report = report
                        )
                    )
                    manageRequest = null
                }
            )
        }
    }

    if (showCreateTask) {
        CreateTaskDialog(
            profile = profile,
            saving = saving,
            onDismiss = { showCreateTask = false },
            onSubmit = {
                onCreateTask(it)
                showCreateTask = false
            }
        )
    }

    completeTask?.let { task ->
        CompleteTaskDialog(
            task = task,
            saving = processingId == task.id,
            onDismiss = { completeTask = null },
            onSubmit = { report ->
                onUpdateTask(task, "COMPLETED", report)
                completeTask = null
            }
        )
    }
}

@Composable
private fun MaintenancePageIntro(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        )
        Spacer(Modifier.height(6.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ManagementComplaintSummary(
    open: Int,
    urgent: Int,
    myTasks: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCountCard(
            label = "OPEN WORK",
            value = open,
            background = MaterialTheme.colorScheme.primary,
            foreground = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )
        SummaryCountCard(
            label = "URGENT",
            value = urgent,
            background = GoldPale,
            foreground = Color(0xFF80561F),
            modifier = Modifier.weight(1f)
        )
        SummaryCountCard(
            label = "MY TASKS",
            value = myTasks,
            background = MaterialTheme.colorScheme.surface,
            foreground = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    compact: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compact) 14.dp else 17.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            labels.forEachIndexed { index, label ->
                val selected = selectedIndex == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(if (compact) 36.dp else 42.dp)
                        .clip(RoundedCornerShape(if (compact) 11.dp else 13.dp))
                        .clickable { onSelected(index) },
                    shape = RoundedCornerShape(if (compact) 11.dp else 13.dp),
                    color = when {
                        compact && selected -> MaterialTheme.colorScheme.primary
                        selected -> MaterialTheme.colorScheme.surface
                        else -> Color.Transparent
                    },
                    shadowElevation = if (selected && !compact) 1.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                compact && selected -> MaterialTheme.colorScheme.onPrimary
                                selected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResidentComplaintSummary(
    pending: Int,
    inProgress: Int,
    totalActive: Int
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryCountCard(
                label = "PENDING",
                value = pending,
                background = GoldPale,
                foreground = Color(0xFF80561F),
                modifier = Modifier.weight(1f)
            )
            SummaryCountCard(
                label = "IN PROGRESS",
                value = inProgress,
                background = BluePale,
                foreground = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCountCard(
                label = "TOTAL ACTIVE",
                value = totalActive,
                background = MaterialTheme.colorScheme.primary,
                foreground = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            "Total active includes pending and in-progress complaints.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SummaryCountCard(
    label: String,
    value: Int,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(20.dp),
        color = background,
        border = if (background == MaterialTheme.colorScheme.primary) {
            null
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = foreground.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
            Text(
                value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium,
                color = foreground,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActiveMaintenanceCard(
    request: MaintenanceRequest,
    onClick: () -> Unit
) {
    val requestStatus = MaintenanceRules.normalizeRequestStatus(request.status)
    CondoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
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
                    "${request.unitCode} · ${request.detail}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                MaintenanceRules.requestStatusLabel(request.status),
                requestStatusTone(request.status)
            )
        }
        Spacer(Modifier.height(17.dp))
        TimelineStep(true, "Report submitted")
        TimelineStep(
            done = requestStatus != "PENDING",
            text = if (requestStatus == "PENDING") {
                "Waiting for management to assign a technician"
            } else {
                buildString {
                    append("In progress")
                    request.technicianName?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                    request.scheduledAt?.let { append(" · ${MaintenanceRules.dateTimeSummary(it)}") }
                }
            },
            active = requestStatus == "PENDING"
        )
        TimelineStep(
            done = requestStatus == "COMPLETED",
            text = if (requestStatus == "COMPLETED") "Completed and report available"
            else "Awaiting maintenance completion report",
            active = requestStatus == "IN_PROGRESS",
            last = true
        )
    }
}

@Composable
private fun TimelineStep(
    done: Boolean,
    text: String,
    active: Boolean = false,
    last: Boolean = false
) {
    Row(modifier = Modifier.height(if (last) 28.dp else 38.dp)) {
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
            color = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RequestListCard(
    request: MaintenanceRequest,
    role: UserRole,
    onClick: () -> Unit
) {
    CondoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(request.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${request.unitCode} · ${MaintenanceRules.formatTimestamp(request.createdAt, "d MMM · h:mm a", "Just now")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (role == UserRole.MANAGEMENT) {
                    Text(
                        "${request.residentName} · ${request.unitCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusPill(
                MaintenanceRules.requestStatusLabel(request.status),
                requestStatusTone(request.status)
            )
        }
        if (request.photoUrls.isNotEmpty() || request.report != null) {
            Spacer(Modifier.height(9.dp))
            Text(
                listOfNotNull(
                    request.photoUrls.size.takeIf { it > 0 }?.let { "$it photo${if (it == 1) "" else "s"}" },
                    request.report?.takeIf { it.isNotBlank() }?.let { "Management report available" }
                ).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ManagementRequestCard(
    request: MaintenanceRequest,
    processing: Boolean,
    onClick: () -> Unit
) {
    val requestStatus = MaintenanceRules.normalizeRequestStatus(request.status)
    CondoCard(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = !processing, onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (request.urgency == "Urgent") CoralPale else BluePale,
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Glyph(
                    GlyphType.WORK,
                    if (request.urgency == "Urgent") Coral else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(request.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(
                    "${request.unitCode} · ${request.residentName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${request.category} · ${request.urgency}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                MaintenanceRules.requestStatusLabel(request.status),
                requestStatusTone(request.status)
            )
        }
        if (request.scheduledAt != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Scheduled ${MaintenanceRules.dateTimeSummary(request.scheduledAt)}" +
                        request.technicianName?.let { " · $it" }.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            when {
                processing -> "Saving changes…"
                requestStatus == "PENDING" -> "Assign technician and start ›"
                requestStatus == "IN_PROGRESS" -> "Complete job and add report ›"
                else -> "View completed report ›"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RequestDetailDialog(
    request: MaintenanceRequest,
    role: UserRole,
    onDismiss: () -> Unit,
    onPhotoClick: (String) -> Unit,
    onDelete: (() -> Unit)?,
    manageLabel: String?,
    onManage: (() -> Unit)?
) {
    val requestStatus = MaintenanceRules.normalizeRequestStatus(request.status)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(request.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(3.dp))
                Text(
                    "Unit ${request.unitCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    StatusPill(
                        MaintenanceRules.requestStatusLabel(requestStatus),
                        requestStatusTone(requestStatus)
                    )
                    StatusPill(request.urgency, requestUrgencyTone(request.urgency))
                }
                Spacer(Modifier.height(16.dp))
                DetailBlock {
                    DetailBlockTitle(
                        title = "Complaint description",
                        glyph = GlyphType.WARNING,
                        color = Coral
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        request.description,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 23.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )
                    Spacer(Modifier.height(12.dp))
                    DetailInfoRow(
                        label = "Reported by",
                        value = request.residentName.ifBlank { "Resident" }
                    )
                    Spacer(Modifier.height(8.dp))
                    DetailInfoRow(
                        label = "Reported on",
                        value = MaintenanceRules.formatTimestamp(
                            request.createdAt,
                            "d MMM yyyy · h:mm a",
                            "Recently"
                        )
                    )
                    if (request.photoUrls.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "Photos · ${request.photoUrls.size}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            "Tap a photo to view it full screen.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            request.photoUrls.forEach { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Maintenance issue photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(142.dp)
                                        .height(104.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .clickable { onPhotoClick(url) }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                MaintenanceResponseBlock(request, requestStatus)
                if (role == UserRole.RESIDENT) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "The management office updates this complaint as work progresses.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (onManage != null) {
                TextButton(onClick = onManage) {
                    Text(manageLabel ?: "Update", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDismiss) { Text("Back") }
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ConfirmDeleteComplaintDialog(
    deleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm delete?", style = MaterialTheme.typography.titleLarge) },
        confirmButton = {
            Button(
                enabled = !deleting,
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(if (deleting) "Deleting…" else "Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun PhotoViewerDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember(photoUrl) { mutableFloatStateOf(1f) }
    var offsetX by remember(photoUrl) { mutableFloatStateOf(0f) }
    var offsetY by remember(photoUrl) { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        if (newScale > 1f) {
            offsetX += panChange.x
            offsetY += panChange.y
        } else {
            offsetX = 0f
            offsetY = 0f
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Complaint photo enlarged",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 64.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .transformable(transformState)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Complaint photo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        "Pinch to zoom",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onDismiss),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Glyph(
                            GlyphType.CLOSE,
                            Color.White,
                            modifier = Modifier.size(19.dp),
                            strokeWidth = 2.2.dp
                        )
                    }
                }
            }

            if (scale > 1f) {
                Text(
                    "Reset zoom",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 22.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable {
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                        .padding(horizontal = 17.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DetailBlock(
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = color,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            content = content
        )
    }
}

@Composable
private fun DetailBlockTitle(
    title: String,
    glyph: GlyphType,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Glyph(glyph, color, modifier = Modifier.size(16.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String
) {
    Column {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MaintenanceResponseBlock(
    request: MaintenanceRequest,
    requestStatus: String
) {
    val backgroundColor = when (requestStatus) {
        "COMPLETED" -> SuccessPale.copy(alpha = 0.55f)
        "IN_PROGRESS" -> BluePale.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.surface
    }
    val accentColor = when (requestStatus) {
        "COMPLETED" -> Success
        "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
        else -> Gold
    }
    val heading = when (requestStatus) {
        "COMPLETED" -> "Maintenance completed"
        "IN_PROGRESS" -> "Maintenance in progress"
        else -> "Waiting for assignment"
    }
    val summary = when (requestStatus) {
        "COMPLETED" -> "The repair is complete. Review the work report below."
        "IN_PROGRESS" -> "Management has assigned this complaint and scheduled the visit."
        else -> "Management has not assigned a technician yet. Updates will appear here."
    }

    DetailBlock(color = backgroundColor) {
        DetailBlockTitle(
            title = "Maintenance response",
            glyph = if (requestStatus == "COMPLETED") GlyphType.CHECK else GlyphType.WORK,
            color = accentColor
        )
        Spacer(Modifier.height(10.dp))
        Text(heading, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(3.dp))
        Text(
            summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(13.dp))
        DetailInfoRow(
            label = "Technician",
            value = request.technicianName?.takeIf { it.isNotBlank() } ?: "Not assigned"
        )
        Spacer(Modifier.height(10.dp))
        DetailInfoRow(
            label = "Scheduled visit",
            value = if (request.scheduledAt.isNullOrBlank()) {
                "Not scheduled"
            } else {
                MaintenanceRules.dateTimeSummary(request.scheduledAt)
            }
        )
        if (!request.report.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(Modifier.padding(13.dp)) {
                    Text(
                        "WORK REPORT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Success,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        request.report,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignRequestDialog(
    request: MaintenanceRequest,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var technician by remember { mutableStateOf(request.technicianName.orEmpty()) }
    var scheduledAt by remember { mutableStateOf(request.scheduledAt) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Assign and start", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Unit ${request.unitCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 590.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(11.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Glyph(
                                    GlyphType.WORK,
                                    MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Text(
                                "Schedule this repair",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(
                            "Enter who will handle the complaint and when the visit is planned. Saving starts the maintenance workflow.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(11.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            StatusPill("PENDING", PillTone.GOLD)
                            Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StatusPill("IN_PROGRESS", PillTone.BLUE)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = technician,
                    onValueChange = { technician = it },
                    label = { Text("Assigned technician / vendor") },
                    placeholder = { Text("Example: Rafiq A. · Plumbing") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
                Spacer(Modifier.height(12.dp))
                ScheduleField(
                    value = scheduledAt,
                    onValueChange = { scheduledAt = it }
                )
                if (technician.isBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Enter the technician or vendor handling this job.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (scheduledAt == null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Choose the maintenance date and time.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving &&
                        technician.isNotBlank() &&
                        scheduledAt != null,
                onClick = { onSubmit(technician.trim(), requireNotNull(scheduledAt)) }
            ) {
                Text(if (saving) "Starting…" else "Start maintenance")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CompleteRequestDialog(
    request: MaintenanceRequest,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var report by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Complete maintenance", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Unit ${request.unitCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DetailBlock(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    DetailBlockTitle(
                        title = "Work details",
                        glyph = GlyphType.WORK,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(11.dp))
                    DetailInfoRow(
                        label = "Technician",
                        value = request.technicianName?.takeIf { it.isNotBlank() } ?: "Not recorded"
                    )
                    Spacer(Modifier.height(9.dp))
                    DetailInfoRow(
                        label = "Scheduled visit",
                        value = MaintenanceRules.dateTimeSummary(request.scheduledAt)
                    )
                }
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = report,
                    onValueChange = { if (it.length <= 1500) report = it },
                    label = { Text("Maintenance report (required)") },
                    placeholder = {
                        Text("Record the diagnosis, work completed, parts replaced and any follow-up advice.")
                    },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
                if (report.isBlank()) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "A report is required before the request can be marked Completed.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && report.isNotBlank(),
                onClick = { onSubmit(report.trim()) }
            ) {
                Text(if (saving) "Completing…" else "Mark completed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CompactTaskCard(task: MaintenanceTask) {
    CondoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${MaintenanceRules.dateTimeSummary(task.scheduledAt)} · ${task.assigneeName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                MaintenanceRules.taskStatusLabel(task.status),
                taskStatusTone(task.status)
            )
        }
    }
}

@Composable
private fun ManagementTaskCard(
    task: MaintenanceTask,
    profile: UserProfile,
    processing: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    val mine = task.assigneeId == profile.id
    CondoCard {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(41.dp)
                    .background(
                        if (task.priority == "Urgent") CoralPale else PurplePale,
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Glyph(
                    GlyphType.WORK,
                    if (task.priority == "Urgent") Coral else Purple,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${task.unit} · ${MaintenanceRules.dateTimeSummary(task.scheduledAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (mine) "Assigned to you" else "Assigned to ${task.assigneeName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (mine) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                MaintenanceRules.taskStatusLabel(task.status),
                taskStatusTone(task.status)
            )
        }
        if (!task.notes.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                task.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!task.report.isNullOrBlank()) {
            Spacer(Modifier.height(10.dp))
            CondoCard(color = SuccessPale.copy(alpha = 0.62f)) {
                Text("Completion report", style = MaterialTheme.typography.labelSmall, color = Success)
                Spacer(Modifier.height(3.dp))
                Text(task.report, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (mine && task.status == "ASSIGNED") {
            Spacer(Modifier.height(4.dp))
            TextButton(enabled = !processing, onClick = onStart) {
                Text(if (processing) "Updating…" else "Start task")
            }
        } else if (mine && task.status == "IN_PROGRESS") {
            Spacer(Modifier.height(4.dp))
            TextButton(enabled = !processing, onClick = onComplete) {
                Text("Add report and complete")
            }
        }
    }
}

@Composable
private fun CreateTaskDialog(
    profile: UserProfile,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (MaintenanceTaskDraft) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") }
    var scheduledAt by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule maintenance", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 590.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "This task will be assigned to ${profile.fullName}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 100) title = it },
                    label = { Text("Task title") },
                    placeholder = { Text("Example: Inspect Tower B water pump") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
                Spacer(Modifier.height(11.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit or location") },
                    placeholder = { Text("Example: Level 5 pump room") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
                Spacer(Modifier.height(11.dp))
                Text("Priority", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf("Low", "Normal", "Urgent").forEach { option ->
                        FilterChip(
                            selected = priority == option,
                            onClick = { priority = option },
                            label = { Text(option) }
                        )
                    }
                }
                Spacer(Modifier.height(11.dp))
                ScheduleField(
                    value = scheduledAt,
                    onValueChange = { scheduledAt = it }
                )
                Spacer(Modifier.height(11.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 800) notes = it },
                    label = { Text("Instructions") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && title.isNotBlank() && scheduledAt != null,
                onClick = {
                    onSubmit(
                        MaintenanceTaskDraft(
                            title = title,
                            unit = unit,
                            priority = priority,
                            scheduledAt = requireNotNull(scheduledAt),
                            notes = notes
                        )
                    )
                }
            ) {
                Text(if (saving) "Scheduling…" else "Schedule task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun CompleteTaskDialog(
    task: MaintenanceTask,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var report by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete task", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column {
                Text(
                    "${task.title}\n${task.unit} · ${MaintenanceRules.dateTimeSummary(task.scheduledAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = report,
                    onValueChange = { if (it.length <= 1500) report = it },
                    label = { Text("Completion report") },
                    placeholder = { Text("What was inspected, repaired or replaced?") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving && report.isNotBlank(),
                onClick = { onSubmit(report) }
            ) {
                Text(if (saving) "Saving…" else "Complete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun ScheduleField(
    value: String?,
    onValueChange: (String?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val parsedDate = remember(value) { MaintenanceRules.parseTimestamp(value.orEmpty()) }
    val selectedTime = remember(value) {
        parsedDate?.let { date ->
            val calendar = Calendar.getInstance().apply { time = date }
            findClosestTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = datePickerMillis(parsedDate)
    )

    Text("Visit schedule", style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(7.dp))
    SurfaceField(
        label = if (value == null) "Choose a date" else MaintenanceRules.dateSummary(value),
        onClick = { showDatePicker = true }
    )
    Spacer(Modifier.height(7.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        maintenanceTimes.forEach { time ->
            FilterChip(
                selected = selectedTime == time,
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: datePickerMillis(null)
                    onValueChange(scheduleFromMillis(millis, time))
                },
                label = { Text(time.label) }
            )
        }
    }
    if (value != null) {
        TextButton(onClick = { onValueChange(null) }) {
            Text(
                "Clear schedule",
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis ?: datePickerMillis(null)
                        onValueChange(scheduleFromMillis(millis, selectedTime ?: maintenanceTimes[0]))
                        showDatePicker = false
                    }
                ) {
                    Text("Use date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Select maintenance date",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                },
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun SurfaceField(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(15.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Glyph(
            GlyphType.CALENDAR,
            MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text("Choose", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun MaintenanceCalendar(
    month: Calendar,
    selectedDay: Int,
    tasks: List<MaintenanceTask>,
    requests: List<MaintenanceRequest>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (Int) -> Unit
) {
    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val daysInMonth = month.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = (month.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val cells: List<Int?> = List<Int?>(firstDayOffset) { null } + (1..daysInMonth).toList()
    val weeks = cells.chunked(7)
    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.US).format(month.time)

    CondoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "‹",
                modifier = Modifier
                    .clickable(onClick = onPreviousMonth)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleLarge
            )
            Text(monthTitle, style = MaterialTheme.typography.titleMedium)
            Text(
                "›",
                modifier = Modifier
                    .clickable(onClick = onNextMonth)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            weekdays.forEach { name ->
                Text(
                    name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (index in 0 until 7) {
                    val day = week.getOrNull(index)
                    if (day == null) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val taskCount = tasks.count {
                            isSameDay(it.scheduledAt, month, day)
                        }
                        val requestCount = requests.count {
                            isSameDay(it.scheduledAt, month, day)
                        }
                        CalendarDayCell(
                            day = day,
                            selected = day == selectedDay,
                            today = isCurrentMonth(month) && day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                            hasTasks = taskCount > 0,
                            hasComplaintVisit = requestCount > 0,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelectDay(day) }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarLegendItem("Task", Gold)
            CalendarLegendItem("Complaint visit", MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CalendarLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    selected: Boolean,
    today: Boolean,
    hasTasks: Boolean,
    hasComplaintVisit: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .then(
                if (today && !selected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (hasTasks) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.onPrimary else Gold,
                                CircleShape
                            )
                    )
                }
                if (hasComplaintVisit) {
                    Box(
                        Modifier
                            .size(5.dp)
                            .background(
                                if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                }
                if (!hasTasks && !hasComplaintVisit) {
                    Spacer(Modifier.size(5.dp))
                }
            }
        }
    }
}

@Composable
private fun CalendarAgenda(
    month: Calendar,
    day: Int,
    tasks: List<MaintenanceTask>,
    requests: List<MaintenanceRequest>
) {
    val dailyTasks = tasks.filter { isSameDay(it.scheduledAt, month, day) }
    val dailyRequests = requests.filter { isSameDay(it.scheduledAt, month, day) }
    val title = SimpleDateFormat("EEEE, d MMMM", Locale.US).format(
        (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }.time
    )
    CondoCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(9.dp))
        if (dailyTasks.isEmpty() && dailyRequests.isEmpty()) {
            Text(
                "No maintenance work is scheduled for this date.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        dailyTasks.forEach { task ->
            AgendaRow(
                title = task.title,
                detail = "${MaintenanceRules.timeSummary(task.scheduledAt)} · ${task.assigneeName}",
                badge = MaintenanceRules.taskStatusLabel(task.status)
            )
        }
        dailyRequests.forEach { request ->
            AgendaRow(
                title = request.title,
                detail = "${MaintenanceRules.timeSummary(request.scheduledAt)} · ${request.unitCode}",
                badge = MaintenanceRules.requestStatusLabel(request.status)
            )
        }
    }
}

@Composable
private fun AgendaRow(
    title: String,
    detail: String,
    badge: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(8.dp)
                .background(Gold, CircleShape)
        )
        Spacer(Modifier.width(9.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        StatusPill(badge, PillTone.GOLD)
    }
}

private fun requestStatusTone(status: String): PillTone = when (status) {
    "COMPLETED" -> PillTone.GREEN
    "IN_PROGRESS" -> PillTone.BLUE
    else -> PillTone.GOLD
}

private fun requestUrgencyTone(urgency: String): PillTone = when (urgency) {
    "Urgent" -> PillTone.CORAL
    "Low" -> PillTone.BLUE
    else -> PillTone.GOLD
}

private fun taskStatusTone(status: String): PillTone = when (status) {
    "COMPLETED" -> PillTone.GREEN
    "IN_PROGRESS" -> PillTone.BLUE
    "CANCELLED" -> PillTone.CORAL
    else -> PillTone.GOLD
}

private fun datePickerMillis(date: java.util.Date?): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = 0
    if (date != null) {
        val local = Calendar.getInstance().apply { time = date }
        calendar.set(Calendar.YEAR, local.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, local.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, local.get(Calendar.DAY_OF_MONTH))
    } else {
        val local = Calendar.getInstance()
        calendar.set(Calendar.YEAR, local.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, local.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, local.get(Calendar.DAY_OF_MONTH))
    }
    return calendar.timeInMillis
}

private fun scheduleFromMillis(dateMillis: Long, time: TimeOption): String {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = dateMillis
    }
    return MaintenanceRules.scheduleTimestamp(
        year = utc.get(Calendar.YEAR),
        month = utc.get(Calendar.MONTH),
        day = utc.get(Calendar.DAY_OF_MONTH),
        hour = time.hour,
        minute = time.minute
    )
}

private fun findClosestTime(hour: Int, minute: Int): TimeOption =
    maintenanceTimes.minByOrNull {
        kotlin.math.abs(it.hour * 60 + it.minute - (hour * 60 + minute))
    } ?: maintenanceTimes[0]

private fun isCurrentMonth(month: Calendar): Boolean {
    val today = Calendar.getInstance()
    return month.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            month.get(Calendar.MONTH) == today.get(Calendar.MONTH)
}

private fun isSameDay(value: String?, month: Calendar, day: Int): Boolean {
    val parsed = MaintenanceRules.parseTimestamp(value.orEmpty()) ?: return false
    val calendar = Calendar.getInstance().apply { time = parsed }
    return calendar.get(Calendar.YEAR) == month.get(Calendar.YEAR) &&
            calendar.get(Calendar.MONTH) == month.get(Calendar.MONTH) &&
            calendar.get(Calendar.DAY_OF_MONTH) == day
}
