package com.example.condominium_maintenance_system.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.condominium_maintenance_system.ui.*
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.screens.account.AccountScreen
import com.example.condominium_maintenance_system.ui.screens.announcements.AnnouncementsScreen
import com.example.condominium_maintenance_system.ui.screens.auth.LoginScreen
import com.example.condominium_maintenance_system.ui.screens.billing.BillingScreen
import com.example.condominium_maintenance_system.ui.screens.booking.BookingScreen
import com.example.condominium_maintenance_system.ui.screens.community.CommunityScreen
import com.example.condominium_maintenance_system.ui.screens.home.HomeScreen
import com.example.condominium_maintenance_system.ui.screens.requests.NewRequestScreen
import com.example.condominium_maintenance_system.ui.screens.requests.RequestsScreen
import com.example.condominium_maintenance_system.ui.theme.BlueDeep
import com.example.condominium_maintenance_system.ui.theme.Canvas
import kotlinx.coroutines.launch

@Composable
fun CondoTrackApp(
    initialRole: UserRole? = null,
    initialDestination: AppDestination = AppDestination.HOME
) {
    var role by remember(initialRole) { mutableStateOf(initialRole) }
    var destination by remember(initialDestination) { mutableStateOf(initialDestination) }
    var feePaid by remember { mutableStateOf(false) }

    val visitors = remember {
        mutableStateListOf(
            VisitorRequest("Daniel Tan", "WXY 8821", "Today, 6:00 PM", "Bay B-14", "Approved"),
            VisitorRequest("Mei Lin", "VCT 2048", "Saturday, 10:00 AM", "Awaiting bay", "Pending")
        )
    }
    val requests = remember {
        mutableStateListOf(
            MaintenanceRequest(
                title = "Kitchen sink leak",
                reference = "#MNT-1043",
                detail = "Plumbing · Assigned to Rafiq",
                status = "In progress",
                time = "Today, 2:30 PM"
            ),
            MaintenanceRequest(
                title = "Air-con filter cleaning",
                reference = "#MNT-0987",
                detail = "Preventive maintenance",
                status = "Completed",
                time = "Completed 24 Oct"
            )
        )
    }
    val workOrders = remember {
        mutableStateListOf(
            WorkOrder("WO-3021", "Water leak", "Unit 12-08", "Rafiq A.", "Due today", "Urgent"),
            WorkOrder("WO-3018", "Flickering light", "Unit 08-03", "Rizal K.", "Tomorrow", "Scheduled"),
            WorkOrder("WO-3007", "Air-con service", "Unit 15-11", "Jason C.", "Friday", "Assigned")
        )
    }
    val bookings = remember {
        mutableStateListOf(
            FacilityBooking("Function room", "Unit 12-08", 14, "3:00 PM – 6:00 PM", "Pending"),
            FacilityBooking("Swimming pool", "Unit 05-02", 14, "9:00 AM – 12:00 PM", "Approved"),
            FacilityBooking("Sports court", "Unit 08-03", 15, "6:00 PM – 9:00 PM", "Approved"),
            FacilityBooking("BBQ pit", "Unit 12-08", 21, "6:00 PM – 9:00 PM", "Approved")
        )
    }

    val lostItems = remember {
        mutableStateListOf(
            LostFoundItem("Set of keys with blue tag", "Found at Level 2 lobby · Today, 8:10 AM", "Unclaimed", "Found"),
            LostFoundItem("Black compact umbrella", "Found at Tower B lift lobby · Yesterday", "Unclaimed", "Found"),
            LostFoundItem("Brown wallet", "Returned to owner · 4 Nov 2026", "Returned", "Found")
        )
    }
    val notices = remember {
        mutableStateListOf(
            CommunityNotice(
                title = "Water supply interruption on 8 November",
                body = "Water will be unavailable from 10:00 AM to 2:00 PM while the main valve is replaced.",
                time = "Pinned",
                pinned = true
            ),
            CommunityNotice(
                title = "Your kitchen leak is now in progress",
                body = "Rafiq is expected to arrive between 2:30 and 3:00 PM. Please make sure someone is home.",
                time = "Today · 9:12 AM"
            ),
            CommunityNotice(
                title = "Fire alarm inspection this Friday",
                body = "The safety check will take about 15 minutes per unit. Access is required.",
                time = "4 Nov · Community"
            )
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val message: (String) -> Unit = { text ->
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    val signedInRole = role
    if (signedInRole == null) {
        LoginScreen { selectedRole ->
            role = selectedRole
            destination = AppDestination.HOME
        }
        return
    }

    fun navigate(next: AppDestination) {
        if (next == AppDestination.BILLING && signedInRole != UserRole.RESIDENT) return
        destination = next
    }

    val showNavigation = destination != AppDestination.NEW_REQUEST
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showNavigation) {
                CondoBottomNavigation(
                    role = signedInRole,
                    current = destination,
                    onNavigate = ::navigate
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            val goHome = { destination = AppDestination.HOME }
            when (destination) {
                AppDestination.HOME -> HomeScreen(
                    role = signedInRole,
                    onNavigate = ::navigate,
                    onOpenNotifications = {
                        if (signedInRole == UserRole.RESIDENT) navigate(AppDestination.ANNOUNCEMENTS)
                        else message("No urgent management notifications right now.")
                    }
                )

                AppDestination.ACCESS -> AccountScreen(
                    role = signedInRole,
                    visitors = visitors,
                    onBack = goHome,
                    onAddVisitor = {
                        visitors.add(0, it)
                        message("Visitor parking request submitted.")
                    },
                    onVisitorAction = { target, status ->
                        val index = visitors.indexOf(target)
                        if (index >= 0) visitors[index] = target.copy(status = status)
                        message("Visitor request $status.")
                    },
                    onMessage = message
                )

                AppDestination.MAINTENANCE -> RequestsScreen(
                    role = signedInRole,
                    requests = requests,
                    workOrders = workOrders,
                    onBack = goHome,
                    onNewRequest = { navigate(AppDestination.NEW_REQUEST) },
                    onAction = message
                )

                AppDestination.BOOKINGS -> BookingScreen(
                    role = signedInRole,
                    bookings = bookings,
                    onBack = goHome,
                    onAddBooking = { new ->
                        val taken = bookings.any {
                            it.facility == new.facility && it.date == new.date &&
                                    it.slot == new.slot && it.occupiesSlot()
                        }
                        if (taken) {
                            message("That time slot is already booked.")
                        } else {
                            bookings.add(0, new)
                            message("Facility booking request submitted.")
                        }
                    },
                    onBookingAction = { target, status ->
                        val index = bookings.indexOf(target)
                        if (index >= 0) bookings[index] = target.copy(status = status)
                        message("Booking $status.")
                    },
                    onAction = message
                )

                AppDestination.LOST_FOUND -> CommunityScreen(
                    role = signedInRole,
                    items = lostItems,
                    onBack = goHome,
                    onAddItem = {
                        lostItems.add(0, it)
                        message("Item posted to the community board.")
                    },
                    onItemAction = { target, status ->
                        val index = lostItems.indexOf(target)
                        if (index >= 0) lostItems[index] = target.copy(status = status)
                        message("Item marked as $status.")
                    },
                    onAction = message
                )

                AppDestination.ANNOUNCEMENTS -> AnnouncementsScreen(
                    role = signedInRole,
                    notices = notices,
                    onBack = goHome,
                    onAddNotice = {
                        notices.add(0, it)
                        message("Announcement published.")
                    },
                    onMarkRead = {
                        notices.indices.forEach { index ->
                            notices[index] = notices[index].copy(read = true)
                        }
                        message("All notifications marked as read.")
                    },
                    onAction = message
                )

                AppDestination.BILLING -> {
                    if (signedInRole == UserRole.RESIDENT) {
                        BillingScreen(
                            fee = MaintenanceFee(
                                month = "November 2026",
                                amount = "RM 285.00",
                                dueDate = "15 Nov 2026"
                            ),
                            feePaid = feePaid,
                            onBack = goHome,
                            onPay = { method ->
                                feePaid = true
                                message("$method payment completed. Receipt saved.")
                            }
                        )
                    }
                }

                AppDestination.NEW_REQUEST -> NewRequestScreen(
                    onBack = { navigate(AppDestination.MAINTENANCE) },
                    onSubmit = {
                        requests.add(0, it)
                        navigate(AppDestination.MAINTENANCE)
                        message("Maintenance request submitted.")
                    }
                )
            }
        }
    }
}

@Composable
private fun CondoBottomNavigation(
    role: UserRole,
    current: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    val items = when (role) {
        UserRole.RESIDENT -> listOf(
            NavItem(AppDestination.HOME, "Home") to GlyphType.HOME,
            NavItem(AppDestination.MAINTENANCE, "Requests") to GlyphType.LIST,
            NavItem(AppDestination.BOOKINGS, "Bookings") to GlyphType.CALENDAR,
            NavItem(AppDestination.LOST_FOUND, "Community") to GlyphType.PEOPLE,
            NavItem(AppDestination.ACCESS, "Account") to GlyphType.ACCOUNT
        )

        UserRole.MANAGEMENT -> listOf(
            NavItem(AppDestination.HOME, "Overview") to GlyphType.HOME,
            NavItem(AppDestination.ACCESS, "Access") to GlyphType.SHIELD,
            NavItem(AppDestination.MAINTENANCE, "Work") to GlyphType.WORK,
            NavItem(AppDestination.BOOKINGS, "Bookings") to GlyphType.CALENDAR,
            NavItem(AppDestination.ANNOUNCEMENTS, "Notices") to GlyphType.BELL
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.forEach { (item, glyph) ->
                val selected = current == item.destination
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable { onNavigate(item.destination) }
                        .background(
                            if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
                            else Color.Transparent
                        )
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Glyph(
                        glyph,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = if (selected) 2.4.dp else 1.8.dp
                    )
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
