package com.example.nbnfiberchecker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nbnfiberchecker.data.AddressDetail
import com.example.nbnfiberchecker.data.AddressSuggestion
import com.example.nbnfiberchecker.ui.theme.LocalExtendedColors
import com.example.nbnfiberchecker.ui.theme.NBNFiberCheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDark = when (mainViewModel.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            NBNFiberCheckerTheme(darkTheme = isDark) {
                MainScreen(viewModel = mainViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var showSearchDialog by remember { mutableStateOf(false) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? ComponentActivity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    if (showSearchDialog) {
        SearchDialog(
            onDismiss = { showSearchDialog = false },
            onSuggestionSelected = { suggestion ->
                viewModel.addLocation(suggestion.id)
                showSearchDialog = false
            },
            viewModel = viewModel
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "NBN Fiber",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Rollout Tracker",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // 3-Way Theme Switcher Segmented Pill
                    ThemeSegmentedPill(
                        currentMode = viewModel.themeMode,
                        onSelectMode = { viewModel.updateThemeMode(it) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSearchDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Track Address", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            )
        }
    ) { innerPadding ->
        val pullRefreshState = rememberPullToRefreshState()
        if (pullRefreshState.isRefreshing) {
            LaunchedEffect(true) {
                viewModel.refresh()
            }
        }

        LaunchedEffect(viewModel.isRefreshing) {
            if (viewModel.isRefreshing) {
                pullRefreshState.startRefresh()
            } else {
                pullRefreshState.endRefresh()
            }
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            if (viewModel.results.isEmpty() && !viewModel.isRefreshing) {
                EmptyStateView(onAddClick = { showSearchDialog = true })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary status banner
                    item {
                        RolloutSummaryBanner(
                            total = viewModel.results.size,
                            eligibleCount = viewModel.eligibleCount
                        )
                    }

                    // Address detail cards
                    items(viewModel.results, key = { it.id ?: it.formattedAddress ?: it.hashCode().toString() }) { detail ->
                        ExpressiveNbnCard(
                            detail = detail,
                            onDelete = { detail.id?.let { viewModel.removeLocationById(it) } }
                        )
                    }
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullRefreshState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ThemeSegmentedPill(
    currentMode: ThemeMode,
    onSelectMode: (ThemeMode) -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, LocalExtendedColors.current.cardBorder),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ThemeOptionIcon(
                icon = Icons.Outlined.BrightnessAuto,
                isSelected = currentMode == ThemeMode.SYSTEM,
                onClick = { onSelectMode(ThemeMode.SYSTEM) },
                label = "System"
            )
            ThemeOptionIcon(
                icon = Icons.Outlined.LightMode,
                isSelected = currentMode == ThemeMode.LIGHT,
                onClick = { onSelectMode(ThemeMode.LIGHT) },
                label = "Light"
            )
            ThemeOptionIcon(
                icon = Icons.Outlined.DarkMode,
                isSelected = currentMode == ThemeMode.DARK,
                onClick = { onSelectMode(ThemeMode.DARK) },
                label = "Dark"
            )
        }
    }
}

@Composable
fun ThemeOptionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    val tintColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun RolloutSummaryBanner(
    total: Int,
    eligibleCount: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, LocalExtendedColors.current.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$total Monitored ${if (total == 1) "Property" else "Properties"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (eligibleCount > 0) "$eligibleCount ready for free FTTP upgrade" else "Checking upgrade schedules...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = CircleShape,
                color = if (eligibleCount > 0) LocalExtendedColors.current.eligibleStatus else MaterialTheme.colorScheme.primary,
                contentColor = if (eligibleCount > 0) Color(0xFF032115) else MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = if (eligibleCount > 0) "$eligibleCount ELIGIBLE" else "ACTIVE",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ExpressiveNbnCard(
    detail: AddressDetail,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val extendedColors = LocalExtendedColors.current

    val isFttpReady = detail.techType?.equals("FTTP", ignoreCase = true) == true ||
            detail.techChangeStatus?.contains("ELIGIBLE", ignoreCase = true) == true ||
            detail.techFlip?.contains("Eligible", ignoreCase = true) == true

    val isCommenced = detail.reasonCode?.contains("COMMENCED", ignoreCase = true) == true ||
            detail.techChangeStatus?.contains("COMMENCED", ignoreCase = true) == true ||
            detail.techChangeStatus?.contains("BUILD", ignoreCase = true) == true

    val isPlanned = detail.targetEligibilityQuarter != null ||
            detail.techChangeStatus?.contains("PLAN", ignoreCase = true) == true

    // Milestone active step: 1 (Current), 2 (Design), 3 (Build), 4 (FTTP Ready)
    val currentStep = when {
        isFttpReady -> 4
        isCommenced -> 3
        isPlanned -> 2
        else -> 1
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, extendedColors.cardBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Address & Delete Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.formattedAddress ?: "Address unavailable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            append(detail.id ?: "Unknown LOC")
                            if (detail.mduFibreEligibility == true) {
                                append(" · MDU Multi-dwelling")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(34.dp)
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Status Hero Banner
            val statusBg = when {
                isFttpReady -> extendedColors.eligibleStatus.copy(alpha = 0.15f)
                isCommenced || isPlanned -> extendedColors.plannedStatus.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val statusColor = when {
                isFttpReady -> extendedColors.eligibleStatus
                isCommenced || isPlanned -> extendedColors.plannedStatus
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = statusBg,
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = when {
                                isFttpReady -> "Upgrade Available (Eligible)"
                                isCommenced -> "Civil Works In Progress"
                                isPlanned -> "Planned Upgrade"
                                else -> detail.techChangeStatus ?: "Current Technology"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }

                    if (detail.targetEligibilityQuarter != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = detail.targetEligibilityQuarter,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isFttpReady) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = "Ready Now",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Milestone Pipeline Stepper
            MilestoneStepper(
                currentStep = currentStep,
                currentTech = detail.techType ?: "Copper"
            )

            // 2x2 Metric Specs Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricSpecBox(
                        label = "Technology",
                        value = if (isFttpReady) "${detail.techType ?: "Copper"} ➔ FTTP" else (detail.techType ?: "Unknown"),
                        accentColor = extendedColors.activeAccent,
                        modifier = Modifier.weight(1f)
                    )
                    MetricSpecBox(
                        label = "Max Speed",
                        value = if (isFttpReady) "1,000 Mbps" else "Standard",
                        accentColor = if (isFttpReady) extendedColors.eligibleStatus else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricSpecBox(
                        label = "Program Type",
                        value = detail.programType ?: "NBN Rollout",
                        modifier = Modifier.weight(1f)
                    )
                    MetricSpecBox(
                        label = "Tech Flip",
                        value = detail.techFlip ?: "Standard",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Optional CTA Button for Eligible Properties
            if (isFttpReady) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nbnco.com.au/residential/upgrades/fttp-upgrade-with-higher-speed-tiers"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = extendedColors.activeAccent,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "Order Free FTTP Upgrade",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MilestoneStepper(
    currentStep: Int,
    currentTech: String
) {
    val steps = listOf(
        currentTech,
        "Design",
        "Build",
        "FTTP"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Track Line
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(top = 10.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Step Nodes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, label ->
                    val stepNum = index + 1
                    val isCompleted = stepNum < currentStep
                    val isActive = stepNum == currentStep

                    val circleBg = when {
                        isCompleted -> LocalExtendedColors.current.eligibleStatus
                        isActive -> LocalExtendedColors.current.activeAccent
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val iconColor = when {
                        isCompleted -> Color(0xFF032115)
                        isActive -> Color(0xFF031520)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = circleBg,
                            contentColor = iconColor,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isCompleted) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else if (isActive && stepNum == 4) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$stepNum",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isActive) LocalExtendedColors.current.activeAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricSpecBox(
    label: String,
    value: String,
    accentColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, LocalExtendedColors.current.cardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateView(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "No Monitored Addresses",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Search and track addresses to receive real-time FTTP upgrade timelines.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search Address", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    onSuggestionSelected: (AddressSuggestion) -> Unit,
    viewModel: MainViewModel
) {
    var query by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Search NBN Address",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.search(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. 124 George St, Sydney...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                viewModel.search("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                if (viewModel.searchResults.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(viewModel.searchResults) { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSuggestionSelected(suggestion) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = suggestion.formattedAddress,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                            }
                        }
                    }
                } else if (query.isNotBlank()) {
                    Text(
                        "Searching NBN locations...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
