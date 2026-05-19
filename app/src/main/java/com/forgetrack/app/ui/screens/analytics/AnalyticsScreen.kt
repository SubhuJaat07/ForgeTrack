package com.forgetrack.app.ui.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.ui.theme.ForgeTrackColors
import com.forgetrack.app.ui.theme.PrimaryPurple
import com.forgetrack.app.util.Double.Companion.toCurrency
import com.forgetrack.app.util.Long.Companion.formatDuration

private enum class AnalyticsPeriod(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

private val chartColors = listOf(
    Color(0xFF6C5CE7),
    Color(0xFF00b894),
    Color(0xFFe17055),
    Color(0xFF0984e3),
    Color(0xFFfdcb6e),
    Color(0xFFe84393),
    Color(0xFF00cec9)
)

private val statusColors = mapOf(
    JobStatus.COMPLETED to Color(0xFF00b894),
    JobStatus.IN_PROGRESS to Color(0xFF0984e3),
    JobStatus.CANCELLED to Color(0xFFff6b6b),
    JobStatus.PENDING to Color(0xFFfdcb6e),
    JobStatus.SCHEDULED to Color(0xFF6C5CE7)
)

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val kpiData by viewModel.kpiData.collectAsState()
    val weeklyRevenue by viewModel.weeklyRevenue.collectAsState()
    val statusDistribution by viewModel.statusDistribution.collectAsState()
    val completionRate by viewModel.completionRate.collectAsState()
    val revenueByPriority by viewModel.revenueByPriority.collectAsState()
    val performanceMetrics by viewModel.performanceMetrics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp
            )
        ) {
            // Period selector
            item {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.onPeriodChanged(it) }
                )
            }

            // KPI Cards
            item {
                KPICardsSection(kpiData = kpiData)
            }

            // Weekly Revenue Bar Chart
            item {
                SectionCard(title = "Weekly Revenue") {
                    BarChart(
                        data = weeklyRevenue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }

            // Job Status Donut Chart
            item {
                SectionCard(title = "Job Status Distribution") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DonutChart(
                            data = statusDistribution,
                            modifier = Modifier
                                .size(160.dp)
                                .padding(8.dp)
                        )
                        // Legend
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusDistribution.forEach { (status, value) ->
                                if (value > 0f) {
                                    LegendItem(
                                        color = statusColors[status] ?: Color.Gray,
                                        label = status.name,
                                        value = "${(value * 100).toInt()}%"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Completion Rate
            item {
                SectionCard(title = "Completion Rate") {
                    CompletionRateBar(
                        rate = completionRate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            // Revenue by Priority
            item {
                SectionCard(title = "Revenue by Priority") {
                    RevenueByPriorityList(
                        data = revenueByPriority,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Performance Metrics
            item {
                SectionCard(title = "Performance Metrics") {
                    PerformanceMetricsSection(
                        metrics = performanceMetrics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: AnalyticsPeriod,
    onPeriodSelected: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalyticsPeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        period.label,
                        fontWeight = if (selectedPeriod == period) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun KPICardsSection(kpiData: KpiData) {
    val cards = listOf(
        Triple("Revenue", kpiData.revenue, Color(0xFF6C5CE7)),
        Triple("Jobs Completed", kpiData.jobsCompleted, Color(0xFF00b894)),
        Triple("Hours", kpiData.hours, Color(0xFF0984e3)),
        Triple("Profit Margin", kpiData.profitMargin, Color(0xFFe17055))
    )

    // Two rows of two
    cards.chunked(2).forEach { rowCards ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowCards.forEach { (title, value, color) ->
                KPICard(
                    title = title,
                    value = value,
                    color = color,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (rowCards.size < 2) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun KPICard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Icon(
                        Icons.Default.ArrowUpward,
                        contentDescription = "Trending",
                        tint = Color(0xFF00b894),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun BarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No data available",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.second } ?: 1f
    val barWidth = 28.dp

    Canvas(modifier = modifier) {
        val chartLeft = 40f
        val chartRight = size.width - 16f
        val chartTop = 16f
        val chartBottom = size.height - 32f
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop
        val barCount = data.size
        val groupWidth = chartWidth / barCount
        val barW = groupWidth * 0.55f

        // Grid lines
        for (i in 0..4) {
            val y = chartBottom - (chartHeight * i / 4f)
            drawLine(
                color = Color(0xFFe2e8f0),
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1f
            )
            // Y-axis labels
            val label = if (maxValue >= 1000) {
                "$${((maxValue * i / 4f) / 1000).toInt()}k"
            } else {
                "$${(maxValue * i / 4f).toInt()}"
            }
            drawContext.canvas.nativeCanvas.drawText(
                label,
                4f,
                y + 4f,
                android.graphics.Paint().apply {
                    textSize = 28f
                    color = android.graphics.Color.parseColor("#94a3b8")
                }
            )
        }

        // Bars
        data.forEachIndexed { index, (label, value) ->
            val x = chartLeft + groupWidth * index + (groupWidth - barW) / 2f
            val barHeight = (value / maxValue) * chartHeight
            val y = chartBottom - barHeight

            // Bar with gradient
            drawRoundRect(
                topLeft = Offset(x, y),
                size = Size(barW, barHeight),
                cornerRadius = CornerRadius(6f, 6f),
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6C5CE7),
                        Color(0xFFa29bfe)
                    )
                )
            )

            // Bar value on top
            drawContext.canvas.nativeCanvas.drawText(
                "$${value.toInt()}",
                x + barW / 2f,
                y - 8f,
                android.graphics.Paint().apply {
                    textSize = 26f
                    color = android.graphics.Color.parseColor("#6C5CE7")
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }
            )

            // X-axis labels
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barW / 2f,
                chartBottom + 20f,
                android.graphics.Paint().apply {
                    textSize = 28f
                    color = android.graphics.Color.parseColor("#94a3b8")
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
private fun DonutChart(
    data: Map<JobStatus, Float>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total <= 0f) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
        return
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 28f
        val arcSize = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        var startAngle = -90f

        data.forEach { (status, fraction) ->
            if (fraction > 0f) {
                val sweep = fraction * 360f
                drawArc(
                    color = statusColors[status] ?: Color.Gray,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, pathEffect = null)
                )
                startAngle += sweep
            }
        }

        // Center text
        drawContext.canvas.nativeCanvas.drawText(
            "${data.values.sumOf { it.toDouble() }.toInt()}",
            size.width / 2f,
            size.height / 2f - 8f,
            android.graphics.Paint().apply {
                textSize = 36f
                color = android.graphics.Color.parseColor("#1e293b")
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
        )
        drawContext.canvas.nativeCanvas.drawText(
            "Total",
            size.width / 2f,
            size.height / 2f + 20f,
            android.graphics.Paint().apply {
                textSize = 28f
                color = android.graphics.Color.parseColor("#94a3b8")
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
    }
}

@Composable
private fun CompletionRateBar(
    rate: Float,
    modifier: Modifier = Modifier
) {
    val animatedRate by animateFloatAsState(
        targetValue = rate,
        label = "completionRate"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Overall Completion",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(animatedRate * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (animatedRate >= 0.7f) Color(0xFF00b894) else if (animatedRate >= 0.4f) Color(0xFFfdcb6e) else Color(0xFFff6b6b)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRate)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = if (animatedRate >= 0.7f)
                                listOf(Color(0xFF00b894), Color(0xFF55efc4))
                            else if (animatedRate >= 0.4f)
                                listOf(Color(0xFFfdcb6e), Color(0xFFffeaa7))
                            else
                                listOf(Color(0xFFff6b6b), Color(0xFFfab1a0))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Milestone markers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0%", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("50%", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            Text("100%", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun RevenueByPriorityList(
    data: List<RevenueByPriorityItem>,
    modifier: Modifier = Modifier
) {
    val maxRevenue = data.maxOfOrNull { it.revenue } ?: 1f

    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        data.forEach { item ->
            val barFraction = item.revenue / maxRevenue
            val priorityColor = when (item.priority) {
                "HIGH" -> Color(0xFFff6b6b)
                "MEDIUM" -> Color(0xFFfdcb6e)
                "LOW" -> Color(0xFF00b894)
                else -> Color(0xFF6C5CE7)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.priority,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(70.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(barFraction)
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.8f))
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$${"%,.0f".format(item.revenue)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(70.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetricsSection(
    metrics: PerformanceMetrics,
    modifier: Modifier = Modifier
) {
    val metricItems = listOf(
        Triple("Avg Job Value", metrics.avgJobValue, Color(0xFF6C5CE7)),
        Triple("Avg Duration", metrics.avgDuration, Color(0xFF0984e3)),
        Triple("Best Day", metrics.bestDay, Color(0xFF00b894)),
        Triple("Active Clients", metrics.activeClients, Color(0xFFe17055))
    )

    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        metricItems.forEachIndexed { index, (label, value, color) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (index < metricItems.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 20.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

// Data classes
data class KpiData(
    val revenue: String = "$0",
    val jobsCompleted: String = "0",
    val hours: String = "0h",
    val profitMargin: String = "0%"
)

data class RevenueByPriorityItem(
    val priority: String,
    val revenue: Float
)

data class PerformanceMetrics(
    val avgJobValue: String = "$0",
    val avgDuration: String = "0h",
    val bestDay: String = "N/A",
    val activeClients: String = "0"
)
