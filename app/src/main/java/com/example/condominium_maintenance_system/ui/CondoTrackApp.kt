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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.condominium_maintenance_system.ui.components.Glyph
import com.example.condominium_maintenance_system.ui.components.GlyphType
import com.example.condominium_maintenance_system.ui.screens.account.AccountScreen
import com.example.condominium_maintenance_system.ui.screens.account.AddVisitorScreen
import com.example.condominium_maintenance_system.ui.screens.account.EditProfileScreen
import com.example.condominium_maintenance_system.ui.screens.announcements.AnnouncementsScreen
import com.example.condominium_maintenance_system.ui.screens.auth.AuthScreen
import com.example.condominium_maintenance_system.ui.screens.billing.BillingScreen
import com.example.condominium_maintenance_system.ui.screens.booking.BookingScreen
import com.example.condominium_maintenance_system.ui.screens.community.CommunityScreen
import com.example.condominium_maintenance_system.ui.screens.home.HomeScreen
import com.example.condominium_maintenance_system.ui.screens.requests.NewRequestScreen
import com.example.condominium_maintenance_system.ui.screens.requests.RequestsScreen
import com.example.condominium_maintenance_system.ui.screens.settings.SettingsScreen
import com.example.condominium_maintenance_system.ui.theme.BlueDeep
import com.example.condominium_maintenance_system.ui.theme.Canvas
import com.example.condominium_maintenance_system.ui.viewmodel.AuthViewModel
import com.example.condominium_maintenance_system.ui.viewmodel.BillingViewModel
import com.example.condominium_maintenance_system.ui.viewmodel.ProfileViewModel
import com.example.condominium_maintenance_system.ui.viewmodel.VisitorViewModel
import kotlinx.coroutines.launch

@Composable
fun CondoTrackApp(
    notificationsEnabled: Boolean = true,
    darkMode: Boolean = false,
    onNotificationsChanged: (Boolean) -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit = {}
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.state.collectAsState()

    when {
        authState.loading -> LoadingScreen()
        authState.profile == null -> AuthScreen(
            state = authState,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::beginRegistration,
            onVerifyRegistration = authViewModel::verifyRegistration,
            onResendRegistration = authViewModel::resendRegistrationCode,
            onRequestPasswordReset = authViewModel::requestPasswordReset,
            onVerifyRecovery = authViewModel::verifyRecoveryCode,
            onResendRecovery = authViewModel::resendRecoveryCode,
            onResetPassword = authViewModel::setNewPassword,
            onCancelFlow = authViewModel::cancelPendingFlow,
            onClearFeedback = authViewModel::clearFeedback
        )

        else -> {
            val profile = authState.profile ?: return
            AuthenticatedCondoTrack(
                signedInProfile = profile,
                onSignedOut = authViewModel::signOut,
                notificationsEnabled = notificationsEnabled,
                darkMode = darkMode,
                onNotificationsChanged = onNotificationsChanged,
                onDarkModeChanged = onDarkModeChanged
            )
        }
    }
}

@Composable
private fun AuthenticatedCondoTrack(
    signedInProfile: UserProfile,
    onSignedOut: () -> Unit,
    notificationsEnabled: Boolean,
    darkMode: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit
) {
    val profileViewModel: ProfileViewModel = viewModel(key = "profile:${signedInProfile.id}")
    val visitorViewModel: VisitorViewModel = viewModel(key = "visitor:${signedInProfile.id}")
    val billingViewModel: BillingViewModel = viewModel(key = "billing:${signedInProfile.id}")

    val profileState by profileViewModel.state.collectAsState()
    val visitorState by visitorViewModel.state.collectAsState()
    val billingState by billingViewModel.state.collectAsState()
    val currentProfile = profileState.profile
        ?.takeIf { it.id == signedInProfile.id }
        ?: signedInProfile
    val role = signedInProfile.role

    var destination by remember { mutableStateOf(AppDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
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
    val facilityBookings = remember {
        mutableStateListOf(
            FacilityBooking("Function room", currentProfile.unitCode ?: "Unit", 14, "3:00 PM – 6:00 PM", "Pending"),
            FacilityBooking("Sports court", currentProfile.unitCode ?: "Unit", 15, "6:00 PM – 9:00 PM", "Approved")
        )
    }
    val lostItems = remember {
        mutableStateListOf(
            LostFoundItem("Set of keys with blue tag", "Found at Level 2 lobby · Today", "Unclaimed", "Found"),
            LostFoundItem("Black compact umbrella", "Found at Tower B lift lobby · Yesterday", "Unclaimed", "Found")
        )
    }
    val notices = remember {
        mutableStateListOf(
            CommunityNotice(
                title = "Water supply interruption on 8 November",
                body = "Water will be unavailable from 10:00 AM to 2:00 PM.",
                time = "Pinned",
                pinned = true
            ),
            CommunityNotice(
                title = "Fire alarm inspection this Friday",
                body = "Access to each unit is required for about 15 minutes.",
                time = "Community"
            )
        )
    }

    LaunchedEffect(signedInProfile.id, role, currentProfile.unitId) {
        profileViewModel.load(signedInProfile.id, role)
        visitorViewModel.load(role, signedInProfile.id)
        if (role == UserRole.RESIDENT && currentProfile.unitId != null) {
            billingViewModel.load(currentProfile.unitId)
        }
    }

    LaunchedEffect(profileState.message, profileState.error) {
        (profileState.error ?: profileState.message)?.let { showMessage(it) }
    }
    LaunchedEffect(visitorState.message, visitorState.error) {
        (visitorState.error ?: visitorState.message)?.let { showMessage(it) }
    }
    LaunchedEffect(billingState.message, billingState.error) {
        (billingState.error ?: billingState.message)?.let { showMessage(it) }
    }

    fun navigate(next: AppDestination) {
        if (next == AppDestination.BILLING && role != UserRole.RESIDENT) return
        destination = next
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (destination !in setOf(
                    AppDestination.NEW_REQUEST,
                    AppDestination.ADD_VISITOR,
                    AppDestination.SETTINGS,
                    AppDestination.EDIT_PROFILE
                )
            ) {
                CondoBottomNavigation(
                    role = role,
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
                    role = role,
                    notificationsEnabled = notificationsEnabled,
                    onNavigate = ::navigate,
                    onOpenNotifications = {
                        if (role == UserRole.RESIDENT) navigate(AppDestination.ANNOUNCEMENTS)
                        else showMessage("No urgent management notifications right now.")
                    }
                )

                AppDestination.ACCESS -> AccountScreen(
                    role = role,
                    profile = currentProfile,
                    profiles = profileState.profiles,
                    visitors = visitorState.requests,
                    loading = profileState.loading || visitorState.loading,
                    processingId = visitorState.processingId,
                    activePass = visitorState.activePass,
                    unpaidBillCount = billingState.bills.count {
                        it.status.equals("UNPAID", ignoreCase = true)
                    },
                    onBack = goHome,
                    onOpenBilling = { navigate(AppDestination.BILLING) },
                    onOpenAnnouncements = { navigate(AppDestination.ANNOUNCEMENTS) },
                    onOpenFacilityBooking = { navigate(AppDestination.BOOKINGS) },
                    onOpenSettings = { navigate(AppDestination.SETTINGS) },
                    onOpenVisitorForm = { navigate(AppDestination.ADD_VISITOR) },
                    onReviewVisitor = { visitor, status ->
                        visitorViewModel.review(visitor, status, role, currentProfile.id)
                    },
                    onOpenPass = { visitor -> visitorViewModel.openPass(visitor, currentProfile.id) },
                    onClosePass = visitorViewModel::closePass,
                    onSignOut = onSignedOut
                )

                AppDestination.SETTINGS -> SettingsScreen(
                    profile = currentProfile,
                    notificationsEnabled = notificationsEnabled,
                    darkMode = darkMode,
                    onBack = { navigate(AppDestination.ACCESS) },
                    onEditProfile = { navigate(AppDestination.EDIT_PROFILE) },
                    onNotificationsChanged = onNotificationsChanged,
                    onDarkModeChanged = onDarkModeChanged
                )

                AppDestination.EDIT_PROFILE -> EditProfileScreen(
                    profile = currentProfile,
                    saving = profileState.saving,
                    onBack = { navigate(AppDestination.SETTINGS) },
                    onUploadAvatar = { uri, resolver ->
                        profileViewModel.uploadAvatar(signedInProfile.id, uri, resolver)
                    },
                    onSave = { update ->
                        profileViewModel.save(
                            userId = signedInProfile.id,
                            update = update
                        ) { saved ->
                            if (saved) navigate(AppDestination.SETTINGS)
                        }
                    }
                )

                AppDestination.MAINTENANCE -> RequestsScreen(
                    role = role,
                    requests = requests,
                    workOrders = workOrders,
                    onBack = goHome,
                    onNewRequest = { navigate(AppDestination.NEW_REQUEST) },
                    onAction = showMessage
                )

                AppDestination.BOOKINGS -> BookingScreen(
                    role = role,
                    bookings = facilityBookings,
                    onBack = goHome,
                    onAddBooking = { facilityBookings.add(0, it) },
                    onBookingAction = { target, status ->
                        val index = facilityBookings.indexOf(target)
                        if (index >= 0) facilityBookings[index] = target.copy(status = status)
                    },
                    onAction = showMessage
                )

                AppDestination.LOST_FOUND -> CommunityScreen(
                    role = role,
                    items = lostItems,
                    onBack = goHome,
                    onAddItem = { lostItems.add(0, it) },
                    onItemAction = { target, status ->
                        val index = lostItems.indexOf(target)
                        if (index >= 0) lostItems[index] = target.copy(status = status)
                    },
                    onAction = showMessage
                )

                AppDestination.ANNOUNCEMENTS -> AnnouncementsScreen(
                    role = role,
                    notices = notices,
                    onBack = goHome,
                    onAddNotice = { notices.add(0, it) },
                    onMarkRead = {
                        notices.indices.forEach { index ->
                            notices[index] = notices[index].copy(read = true)
                        }
                    },
                    onAction = showMessage
                )

                AppDestination.BILLING -> {
                    if (role == UserRole.RESIDENT) {
                        BillingScreen(
                            bills = billingState.bills,
                            payments = billingState.payments,
                            loading = billingState.loading,
                            paying = billingState.paying,
                            onBack = goHome,
                            onPay = { bill, method ->
                                currentProfile.unitId?.let { unitId ->
                                    billingViewModel.pay(bill, unitId, method)
                                }
                            }
                        )
                    }
                }

                AppDestination.NEW_REQUEST -> NewRequestScreen(
                    onBack = { navigate(AppDestination.MAINTENANCE) },
                    onSubmit = {
                        requests.add(0, it)
                        navigate(AppDestination.MAINTENANCE)
                    }
                )

                AppDestination.ADD_VISITOR -> AddVisitorScreen(
                    submitting = visitorState.loading,
                    onBack = { navigate(AppDestination.ACCESS) },
                    onSubmit = {
                        visitorViewModel.add(currentProfile, it) {
                            navigate(AppDestination.ACCESS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, Canvas)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BlueDeep)
            Text(
                "Restoring your CondoTrack session…",
                modifier = Modifier.padding(top = 14.dp),
                style = MaterialTheme.typography.labelLarge
            )
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
            NavItem(AppDestination.MAINTENANCE, "Work") to GlyphType.WORK,
            NavItem(AppDestination.ANNOUNCEMENTS, "Notices") to GlyphType.BELL,
            NavItem(AppDestination.BOOKINGS, "Bookings") to GlyphType.CALENDAR,
            NavItem(AppDestination.ACCESS, "Profile") to GlyphType.ACCOUNT
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
