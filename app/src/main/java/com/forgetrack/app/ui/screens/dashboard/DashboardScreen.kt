package com.forgetrack.app.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.model.WeeklyStats
import com.forgetrack.app.util.formatCurrency
import com.forgetrack.app.util.formatTime
import com.forgetrack.app.util.formatTimer
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// ── Bottom nav destinations ──────────────────────────────────────────────────

private data class BottomNavDest(
    val label: String,
    val icon: @Composable () -> Unit,
    val route: String
)

private val bottomNavItems = listOf(
    BottomNavDest("Dashboard", { Icon(Icons.Outlined.Dashboard, contentDescription = "Dashboard") }, "dashboard"),
    BottomNavDest("Jobs", { Icon(Icons.Outlined.WorkOutline, contentDescription = "Jobs") }, "jobs"),
    BottomNavDest("Clients", { Icon(Icons.Outlined.PeopleOutline, contentDescription = "Clients") }, "clients"),
    BottomNavDest("History", { Icon(Icons.Outlined.History, contentDescription = "History") }, "history"),
    BottomNavDest("Analytics", { Icon(Icons.Outlined.BarChart, contentDescription = "Analytics") }, "analytics")
)

// ── Main composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToCreateJob: () -> Unit = {},
    onNavigateToJobDetail: (String) -> Unit = {},
    onNavigateToJobs: () -> Unit = {},
    onNavigateToClients: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {}
) {
    val todayJobs by viewModel.todayJobs.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good ${greetingTimeOfDay()},",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ForgeTrack",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = onNavigateToCreateJob,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Job")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = item.icon,
                        label = { Text(item.label, fontSize = 11.sp) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (item.route) {
                                "jobs" -> onNavigateToJobs()
                                "clients" -> onNavigateToClients()
                                "history" -> onNavigateToHistory()
                                "analytics" -> onNavigateToAnalytics()
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardContent(
                todayJobs = todayJobs,
                weeklyStats = weeklyStats,
                onJobClick = onNavigateToJobDetail,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> PlaceholderScreen(title = "Jobs", icon = Icons.Outlined.WorkOutline, modifier = Modifier.padding(paddingValues))
            2 -> PlaceholderScreen(title = "Clients", icon = Icons.Outlined.PeopleOutline, modifier = Modifier.padding(paddingValues))
            3 -> PlaceholderScreen(title = "History", icon = Icons.Outlined.History, modifier = Modifier.padding(paddingValues))
            4 -> PlaceholderScreen(title = "Analytics", icon = Icons.Outlined.BarChart, modifier = Modifier.padding(paddingValues))
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primaryContainer)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "$title\nComing Soon", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Dashboard content ────────────────────────────────────────────────────────

@Composable
private fun DashboardContent(
    todayJobs: List<Job>,
    weeklyStats: WeeklyStats,
    onJobClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeJob = todayJobs.firstOrNull { it.status == JobStatus.IN_PROGRESS }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Active job timer banner
        if (activeJob != null) {
            item {
                ActiveJobBanner(job = activeJob, onClick = { onJobClick(activeJob.id) })
            }
        }

        // Stats grid
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            StatsGrid(weeklyStats = weeklyStats, todayJobs = todayJobs)
        }

        // Today's progress
        item {
            TodayProgressBar(todayJobs = todayJobs)
        }

        // Weekly bar chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WeeklyBarChart(weeklyStats = weeklyStats)
                }
            }
        }

        // Job status donut chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Today's Job Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    JobStatusDonutChart(jobs = todayJobs)
                }
            }
        }

        // Today's jobs list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Jobs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { /* view all */ }) {
                    Text("View All")
                }
            }
        }

        if (todayJobs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.EventAvailable,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No jobs scheduled for today",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap + to create a new job",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            items(todayJobs, key = { it.id }) { job ->
                TodayJobCard(
                    job = job,
                    onClick = { onJobClick(job.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Active job banner ────────────────────────────────────────────────────────

@Composable
private fun ActiveJobBanner(job: Job, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pulsing dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = pulse),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Job in Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = job.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ── Stats grid ───────────────────────────────────────────────────────────────

@Composable
private fun StatsGrid(weeklyStats: WeeklyStats, todayJobs: List<Job>) {
    val completedToday = todayJobs.count { it.status == JobStatus.COMPLETED }
    val totalToday = todayJobs.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "Today's Jobs",
            value = "$completedToday/$totalToday",
            icon = Icons.Outlined.WorkOutline,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Completed",
            value = "${weeklyStats.completedJobs}",
            icon = Icons.Outlined.CheckCircleOutline,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "Week Revenue",
            value = formatCurrency(weeklyStats.totalRevenue),
            icon = Icons.Outlined.AttachMoney,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Week Hours",
            value = formatTimer(weeklyStats.totalHours),
            icon = Icons.Outlined.Schedule,
            color = Color(0xFF6D4C41),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Today's progress bar ─────────────────────────────────────────────────────

@Composable
private fun TodayProgressBar(todayJobs: List<Job>) {
    val total = todayJobs.size
    val completed = todayJobs.count { it.status == JobStatus.COMPLETED }
    val progress = if (total > 0) completed.toFloat() / total else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$completed of $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ── Weekly bar chart (Canvas) ────────────────────────────────────────────────

@Composable
private fun WeeklyBarChart(weeklyStats: WeeklyStats) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    // Simulated daily distribution for visual demo
    val dailyRevenue = listOf(120.0, 340.0, 0.0, 280.0, 450.0, 190.0, 75.0)
    val maxRevenue = dailyRevenue.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val chartWidth = size.width
        val chartHeight = size.height - 24.dp.toPx() // leave room for labels
        val barCount = days.size
        val barSpacing = chartWidth / (barCount * 2f + 1f)
        val barWidth = barSpacing * 1.4f

        for (i in days.indices) {
            val fraction = (dailyRevenue[i] / maxRevenue).toFloat()
            val barHeight = (fraction * chartHeight).coerceAtLeast(2.dp.toPx())
            val x = barSpacing + i * (barWidth + barSpacing)
            val y = chartHeight - barHeight

            // Bar
            drawRoundRect(
                color = if (fraction > 0f) primaryColor else primaryContainerColor,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            // Day label
            drawContext.canvas.nativeCanvas.drawText(
                days[i],
                x + barWidth / 2f,
                chartHeight + 18.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#757575")
                    textSize = 11.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

// ── Job status donut chart (Canvas) ──────────────────────────────────────────

@Composable
private fun JobStatusDonutChart(jobs: List<Job>) {
    val statusCounts = JobStatus.values().map { status ->
        status to jobs.count { it.status == status }
    }.filter { it.second > 0 }

    val total = statusCounts.sumOf { it.second }.coerceAtLeast(1)
    val statusColors = mapOf(
        JobStatus.SCHEDULED to MaterialTheme.colorScheme.primary,
        JobStatus.IN_PROGRESS to Color(0xFFFF9800),
        JobStatus.COMPLETED to Color(0xFF4CAF50),
        JobStatus.CANCELLED to Color(0xFFE53935)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
        if (jobs.isEmpty()) {
            Canvas(modifier = Modifier.size(140.dp)) {
                drawCircle(
                    color = surfaceVariantColor,
                    radius = size.width / 2f - 16.dp.toPx(),
                    center = center,
                    style = Stroke(width = 24.dp.toPx())
                )
            }
        } else {
            Canvas(modifier = Modifier.size(140.dp)) {
                var startAngle = -90f
                val strokeWidth = 24.dp.toPx()
                val radius = size.width / 2f - strokeWidth / 2f - 4.dp.toPx()

                statusCounts.forEach { (status, count) ->
                    val sweep = (count.toFloat() / total) * 360f
                    drawArc(
                        color = statusColors[status] ?: Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }
            }
        }

        // Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statusCounts.forEach { (status, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                statusColors[status] ?: Color.Gray,
                                RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${status.name.replace("_", " ")} ($count)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (statusCounts.isEmpty()) {
                Text(
                    text = "No data yet",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Today's job card ─────────────────────────────────────────────────────────

@Composable
private fun TodayJobCard(job: Job, onClick: () -> Unit) {
    val priorityColor = when (job.priority.name) {
        "URGENT" -> Color(0xFFD32F2F)
        "HIGH" -> Color(0xFFFF9800)
        "MEDIUM" -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }

    val statusColor = when (job.status) {
        JobStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        JobStatus.IN_PROGRESS -> Color(0xFFFF9800)
        JobStatus.COMPLETED -> Color(0xFF4CAF50)
        JobStatus.CANCELLED -> Color(0xFFE53935)
        JobStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp)
        ) {
            // Priority color bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(priorityColor, RoundedCornerShape(2.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = job.status.name.replace("_", " "),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.PersonOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.clientName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTime(job.scheduledDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun greetingTimeOfDay(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Morning"
        hour < 17 -> "Afternoon"
        else -> "Evening"
    }
}
