package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LaundryOrder
import com.example.model.ServiceType
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Slate100
import com.example.ui.theme.TealDark
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.viewmodel.LaundryViewModel

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    BOOK("Book", Icons.Default.AddCircleOutline),
    TRACK("Track", Icons.Default.LocationOn),
    ORDERS("Orders", Icons.Default.ReceiptLong)
}

class MainActivity : ComponentActivity() {

    private val viewModel: LaundryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                FreshFoldApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FreshFoldApp(viewModel: LaundryViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var showNotificationsScreen by remember { mutableStateOf(false) }

    val activeOrder by viewModel.activeOrder.collectAsState()
    val orderHistory by viewModel.orderHistory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val unreadNotifsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    // Handle back button on sub-screens
    BackHandler(enabled = showNotificationsScreen || currentTab != MainTab.HOME) {
        if (showNotificationsScreen) {
            showNotificationsScreen = false
        } else {
            currentTab = MainTab.HOME
        }
    }

    Scaffold(
        bottomBar = {
            if (!showNotificationsScreen) {
                NavigationBar(
                    containerColor = PureWhite,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    MainTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                if (tab == MainTab.TRACK && activeOrder != null && !activeOrder!!.status.isCompleted) {
                                    BadgedBox(
                                        badge = {
                                            Badge(containerColor = TealPrimary) {
                                                Text("1", fontSize = 10.sp)
                                            }
                                        }
                                    ) {
                                        Icon(tab.icon, contentDescription = tab.title)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.title)
                                }
                            },
                            label = {
                                Text(
                                    tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealDark,
                                selectedTextColor = TealDark,
                                indicatorColor = TealLight
                            ),
                            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (showNotificationsScreen) {
                NotificationsScreen(
                    notifications = notifications,
                    onBack = { showNotificationsScreen = false },
                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                    onSendTestPush = { viewModel.sendTestPushNotification() },
                    onSelectNotification = {
                        showNotificationsScreen = false
                        currentTab = MainTab.TRACK
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            activeOrder = activeOrder,
                            unreadNotificationCount = unreadNotifsCount,
                            onNavigateToBooking = { service ->
                                if (service != null) {
                                    viewModel.incrementItem(service)
                                }
                                currentTab = MainTab.BOOK
                            },
                            onNavigateToTracking = { currentTab = MainTab.TRACK },
                            onNavigateToNotifications = { showNotificationsScreen = true },
                            onAdvanceOrderStatus = { viewModel.advanceActiveOrderStatus() },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    MainTab.BOOK -> {
                        BookingScreen(
                            viewModel = viewModel,
                            onBack = { currentTab = MainTab.HOME },
                            onBookingSuccess = {
                                currentTab = MainTab.TRACK
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    MainTab.TRACK -> {
                        TrackingScreen(
                            order = activeOrder,
                            onBack = { currentTab = MainTab.HOME },
                            onAdvanceStatus = { viewModel.advanceActiveOrderStatus() },
                            onSendNotification = { viewModel.sendTestPushNotification() },
                            onNavigateToBooking = { currentTab = MainTab.BOOK },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    MainTab.ORDERS -> {
                        OrdersHistoryScreen(
                            orders = orderHistory,
                            activeOrder = activeOrder,
                            onTrackOrder = { currentTab = MainTab.TRACK },
                            onReorder = { reordered ->
                                reordered.items.forEach { item ->
                                    repeat(item.quantity) {
                                        viewModel.incrementItem(item.type)
                                    }
                                }
                                currentTab = MainTab.BOOK
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
