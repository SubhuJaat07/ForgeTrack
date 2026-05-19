package com.forgetrack.app.ui.screens.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.forgetrack.app.data.model.Job
import com.forgetrack.app.data.model.JobStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val jobs by viewModel.filteredJobs.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val summaryStats by viewModel.summaryStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Job History",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View mode toggle (List / Map)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                listOf("List", "Map").forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index, 2),
                        onClick = {
                            viewModel.onViewModeChanged(
                                if (index == 0) ViewMode.LIST else ViewMode.MAP
                            )
                        },
                        selected = (index == 0 && viewMode == ViewMode.LIST) ||
                                (index == 1 && viewMode == ViewMode.MAP),
                        icon = {
                            if ((index == 0 && viewMode == ViewMode.LIST) ||
                                (index == 1 && viewMode == ViewMode.MAP)
                            ) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.List else Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    ) {
                        Text(label, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Summary stats
            SummaryStatsRow(
                totalRevenue = summaryStats.totalRevenue,
                totalHours = summaryStats.totalHours,
                totalJobs = summaryStats.totalJobs
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryFilter.entries) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.onFilterChanged(filter) },
                        label = {
                            Text(
                                filter.label,
                                fontWeight = if (selectedFilter == filter) FontWeight.SemiBold else FontWeight.Normal
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

            Spacer(modifier = Modifier.height(8.dp))

            // Content based on view mode
            if (viewMode == ViewMode.LIST) {
                if (jobs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📋",
                                fontSize = 56.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No jobs found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Jobs matching your filter will appear here",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp
                        )
                    ) {
                        items(
                            items = jobs,
                            key = { it.id }
                        ) { job ->
                            HistoryJobCard(job = job)
                        }
                    }
                }
            } else {
                // Map placeholder
                MapPlaceholder(jobs = jobs)
            }
        }
    }
}

@Composable
private fun SummaryStatsRow(
    totalRevenue: String,
    totalHours: String,
    totalJobs: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryStatCard(
            title = "Revenue",
            value = totalRevenue,
            icon = "💰",
            modifier = Modifier.weight(1f),
            color = Color(0xFF6C5CE7)
        )
        SummaryStatCard(
            title = "Hours",
            value = totalHours,
            icon = "⏱️",
            modifier = Modifier.weight(1f),
            color = Color(0xFF00b894)
        )
        SummaryStatCard(
            title = "Jobs",
            value = totalJobs,
            icon = "🔧",
            modifier = Modifier.weight(1f),
            color = Color(0xFFe17055)
        )
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun HistoryJobCard(job: Job) {
    val statusColor = when (job.status) {
        JobStatus.COMPLETED -> Color(0xFF00b894)
        JobStatus.IN_PROGRESS -> Color(0xFF0984e3)
        JobStatus.CANCELLED -> Color(0xFFff6b6b)
        JobStatus.PENDING -> Color(0xFFfdcb6e)
        JobStatus.SCHEDULED -> Color(0xFF6C5CE7)
    }

    val statusIcon = when (job.status) {
        JobStatus.COMPLETED -> "✅"
        JobStatus.IN_PROGRESS -> "🔄"
        JobStatus.CANCELLED -> "❌"
        JobStatus.PENDING -> "⏳"
        JobStatus.SCHEDULED -> "📅"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Status indicator dot and line
            Column(
                modifier = Modifier.padding(end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(statusColor.copy(alpha = 0.3f))
                )
            }

            // Job content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = job.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${statusIcon} ${job.status.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Client name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Work,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.clientName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date and location row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.scheduledDate.toString().take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    if (job.address.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = job.address,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Revenue row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$${"%,.2f".format(job.revenue)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00b894)
                    )
                    Text(
                        text = job.priority.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (job.priority) {
                            com.forgetrack.app.data.model.Priority.HIGH -> Color(0xFFff6b6b)
                            com.forgetrack.app.data.model.Priority.MEDIUM -> Color(0xFFfdcb6e)
                            com.forgetrack.app.data.model.Priority.LOW -> Color(0xFF00b894)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPlaceholder(jobs: List<Job>) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🗺️",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Map View",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${jobs.size} job location${if (jobs.size != 1) "s" else ""} to display",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Simulated map grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Fake map pin dots
                    jobs.take(5).forEachIndexed { index, job ->
                        val x = ((index * 73 + 17) % 280).dp
                        val y = ((index * 97 + 23) % 160).dp
                        Box(
                            modifier = Modifier
                                .padding(start = x, top = y)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    when (job.status) {
                                        JobStatus.COMPLETED -> Color(0xFF00b894)
                                        JobStatus.IN_PROGRESS -> Color(0xFF0984e3)
                                        JobStatus.CANCELLED -> Color(0xFFff6b6b)
                                        JobStatus.PENDING -> Color(0xFFfdcb6e)
                                        JobStatus.SCHEDULED -> Color(0xFF6C5CE7)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

enum class ViewMode { LIST, MAP }

enum class HistoryFilter(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}
