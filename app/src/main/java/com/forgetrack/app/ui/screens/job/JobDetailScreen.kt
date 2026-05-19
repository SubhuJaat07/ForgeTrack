package com.forgetrack.app.ui.screens.job

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.forgetrack.app.data.model.JobPhoto
import com.forgetrack.app.data.model.JobStatus
import com.forgetrack.app.data.model.PhotoType
import com.forgetrack.app.data.model.VoiceNote
import com.forgetrack.app.util.formatCurrency
import com.forgetrack.app.util.formatDate
import com.forgetrack.app.util.formatTimer

private val detailTabs = listOf("Details", "Photos", "Notes")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    viewModel: JobDetailViewModel = hiltViewModel(),
    jobId: String,
    onNavigateBack: () -> Unit = {}
) {
    val job by viewModel.job.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val voiceNotes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val showSignature by viewModel.showSignature.collectAsStateWithLifecycle()
    val signatureSaved by viewModel.signatureSaved.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf("") }

    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
    }

    // Auto-start timer if job was in progress
    LaunchedEffect(job?.status) {
        if (job?.status == JobStatus.IN_PROGRESS && !isTimerRunning) {
            viewModel.startTimer()
        }
    }

    val currentJob = job ?: return

    val statusColor = when (currentJob.status) {
        JobStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        JobStatus.IN_PROGRESS -> Color(0xFFFF9800)
        JobStatus.COMPLETED -> Color(0xFF4CAF50)
        JobStatus.CANCELLED -> Color(0xFFE53935)
    }

    val priorityColor = when (currentJob.priority.name) {
        "URGENT" -> Color(0xFFD32F2F)
        "HIGH" -> Color(0xFFFF9800)
        "MEDIUM" -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentJob.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = currentJob.status.name.replace("_", " "),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = priorityColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = currentJob.priority.name,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = priorityColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Timer Section ───────────────────────────────────────────────
            if (isTimerRunning || currentJob.totalDuration > 0) {
                item {
                    TimerSection(
                        elapsedSeconds = elapsedSeconds,
                        isRunning = isTimerRunning
                    )
                }
            }

            // ── Action Buttons ──────────────────────────────────────────────
            item {
                ActionButtons(
                    status = currentJob.status,
                    isTimerRunning = isTimerRunning,
                    onStart = viewModel::startJob,
                    onComplete = viewModel::stopTimer,
                    onCancel = { showCancelDialog = true }
                )
            }

            // ── GPS Check-in/out Section ────────────────────────────────────
            item {
                GpsCheckSection(
                    hasCheckedIn = currentJob.checkInTime > 0,
                    hasCheckedOut = currentJob.checkOutTime > 0,
                    address = currentJob.address,
                    latitude = currentJob.latitude,
                    longitude = currentJob.longitude,
                    onCheckIn = { /* Request GPS and call viewModel.checkIn */ },
                    onCheckOut = viewModel::checkOut
                )
            }

            // ── Quick Actions Grid ──────────────────────────────────────────
            item {
                QuickActionsGrid(
                    onBeforePhoto = { /* viewModel.addPhoto(uri, PhotoType.BEFORE) */ },
                    onAfterPhoto = { /* viewModel.addPhoto(uri, PhotoType.AFTER) */ },
                    onVoiceNote = { /* Record voice note */ },
                    onSignature = viewModel::toggleSignatureDialog
                )
            }

            // ── Signature Preview ───────────────────────────────────────────
            if (currentJob.signaturePath.isNotBlank()) {
                item {
                    SignaturePreview(path = currentJob.signaturePath)
                }
            }

            // ── Tabbed Content ──────────────────────────────────────────────
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    detailTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal) }
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        DetailsTabContent(job = currentJob)
                    }
                }
                1 -> {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        PhotosTabContent(photos = photos)
                    }
                }
                2 -> {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        NotesTabContent(
                            notes = notes,
                            voiceNotes = voiceNotes,
                            noteInput = noteInput,
                            onNoteInputChange = { noteInput = it },
                            onAddNote = {
                                if (noteInput.isNotBlank()) {
                                    viewModel.addNote(noteInput.trim())
                                    noteInput = ""
                                }
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ── Cancel dialog ───────────────────────────────────────────────────────
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Job?") },
            text = { Text("Are you sure you want to cancel this job? This action can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelJob()
                    showCancelDialog = false
                }) {
                    Text("Cancel Job", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep")
                }
            }
        )
    }

    // ── Delete dialog ───────────────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Job?") },
            text = { Text("This will permanently delete this job and all associated data. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteJob()
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Signature dialog ────────────────────────────────────────────────────
    if (showSignature) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleSignatureDialog() },
            title = { Text("Capture Signature") },
            text = {
                Column {
                    Text("Client will sign here on the drawing pad.")
                    Spacer(modifier = Modifier.height(12.dp))
                    // Signature canvas placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✍️ Sign here",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // In production, save the bitmap path
                    viewModel.saveSignature("/signatures/${currentJob.id}.png")
                }) {
                    Text("Save Signature")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleSignatureDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Timer Section ────────────────────────────────────────────────────────────

@Composable
private fun TimerSection(elapsedSeconds: Long, isRunning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) Color(0xFFFF9800).copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRunning) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFFFF9800).copy(alpha = pulseAlpha), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isRunning) "Timer Running" else "Time Tracked",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isRunning) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTimer(elapsedSeconds),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ── Action Buttons ───────────────────────────────────────────────────────────

@Composable
private fun ActionButtons(
    status: JobStatus,
    isTimerRunning: Boolean,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (status) {
            JobStatus.SCHEDULED -> {
                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Job", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
            JobStatus.IN_PROGRESS -> {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complete Job", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
            else -> {
                // COMPLETED or CANCELLED — show status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (status == JobStatus.COMPLETED)
                            Color(0xFF4CAF50).copy(alpha = 0.08f)
                        else
                            Color(0xFFE53935).copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (status == JobStatus.COMPLETED) Icons.Outlined.CheckCircle
                            else Icons.Outlined.Cancel,
                            contentDescription = null,
                            tint = if (status == JobStatus.COMPLETED) Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (status == JobStatus.COMPLETED) "Job Completed"
                            else "Job Cancelled",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (status == JobStatus.COMPLETED) Color(0xFF4CAF50) else Color(0xFFE53935)
                        )
                    }
                }
            }
        }
    }
}

// ── GPS Check-in/out ─────────────────────────────────────────────────────────

@Composable
private fun GpsCheckSection(
    hasCheckedIn: Boolean,
    hasCheckedOut: Boolean,
    address: String,
    latitude: Double,
    longitude: Double,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.GpsFixed,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GPS Check-in",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Check-in status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (hasCheckedIn) Icons.Outlined.CheckCircle
                    else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (hasCheckedIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasCheckedIn) "Checked in" else "Not checked in",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasCheckedIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (!hasCheckedIn) {
                    OutlinedButton(
                        onClick = onCheckIn,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check In", fontSize = 12.sp)
                    }
                }
            }

            if (hasCheckedIn) {
                Spacer(modifier = Modifier.height(4.dp))
                if (address.isNotBlank()) {
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
                if (latitude != 0.0 && longitude != 0.0) {
                    Text(
                        text = "📍 %.6f, %.6f".format(latitude, longitude),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Check-out
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (hasCheckedOut) Icons.Outlined.CheckCircle
                        else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (hasCheckedOut) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasCheckedOut) "Checked out" else "Not checked out",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasCheckedOut) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (!hasCheckedOut) {
                        OutlinedButton(
                            onClick = onCheckOut,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Check Out", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Quick Actions Grid ───────────────────────────────────────────────────────

@Composable
private fun QuickActionsGrid(
    onBeforePhoto: () -> Unit,
    onAfterPhoto: () -> Unit,
    onVoiceNote: () -> Unit,
    onSignature: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionItem(
                    icon = Icons.Outlined.PhotoCamera,
                    label = "Before Photo",
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onBeforePhoto
                )
                QuickActionItem(
                    icon = Icons.Outlined.PhotoCameraBack,
                    label = "After Photo",
                    color = Color(0xFF4CAF50),
                    onClick = onAfterPhoto
                )
                QuickActionItem(
                    icon = Icons.Outlined.Mic,
                    label = "Voice Note",
                    color = Color(0xFFFF9800),
                    onClick = onVoiceNote
                )
                QuickActionItem(
                    icon = Icons.Outlined.Draw,
                    label = "Signature",
                    color = Color(0xFF9C27B0),
                    onClick = onSignature
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Signature Preview ────────────────────────────────────────────────────────

@Composable
private fun SignaturePreview(path: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Client Signature Captured",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✅ Signature on file",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Details Tab Content ──────────────────────────────────────────────────────

@Composable
private fun DetailsTabContent(job: com.forgetrack.app.data.model.Job) {
    val profit = job.revenue - job.cost
    val profitMargin = if (job.revenue > 0) (profit / job.revenue * 100).coerceIn(0f, 100f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Description
            DetailRow(
                icon = Icons.Outlined.Description,
                label = "Description",
                value = job.description.ifBlank { "No description" }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Client
            DetailRow(
                icon = Icons.Outlined.PersonOutline,
                label = "Client",
                value = job.clientName
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Scheduled Date/Time
            DetailRow(
                icon = Icons.Outlined.CalendarMonth,
                label = "Scheduled",
                value = "${formatDate(job.scheduledDate)} at ${java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(job.scheduledTime))}"
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Revenue
            DetailRow(
                icon = Icons.Outlined.AttachMoney,
                label = "Revenue",
                value = formatCurrency(job.revenue),
                valueColor = Color(0xFF4CAF50)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Cost
            DetailRow(
                icon = Icons.Outlined.MoneyOff,
                label = "Cost",
                value = formatCurrency(job.cost),
                valueColor = if (job.cost > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Profit margin bar
            if (job.revenue > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profit Margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "%.1f%%".format(profitMargin),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (profit >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
                LinearProgressIndicator(
                    progress = { profitMargin / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (profit >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "Profit: ${formatCurrency(profit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (profit >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Tags
            DetailRow(
                icon = Icons.Outlined.LabelOutline,
                label = "Tags",
                value = job.tags.ifBlank { "No tags" }
            )
            if (job.tags.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    job.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Address
            if (job.address.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                DetailRow(
                    icon = Icons.Outlined.LocationOn,
                    label = "Address",
                    value = job.address
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            modifier = Modifier.padding(start = 22.dp, top = 2.dp)
        )
    }
}

// ── Photos Tab Content ───────────────────────────────────────────────────────

@Composable
private fun PhotosTabContent(photos: List<JobPhoto>) {
    if (photos.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No photos yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Use Quick Actions to add before/after photos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .shadow(2.dp, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (photo.type) {
                            PhotoType.BEFORE -> MaterialTheme.colorScheme.primaryContainer
                            PhotoType.AFTER -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (photo.type == PhotoType.BEFORE) Icons.Outlined.PhotoCamera
                                else Icons.Outlined.PhotoCameraBack,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = when (photo.type) {
                                    PhotoType.BEFORE -> MaterialTheme.colorScheme.primary
                                    PhotoType.AFTER -> Color(0xFF4CAF50)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = photo.type.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Notes Tab Content ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesTabContent(
    notes: List<String>,
    voiceNotes: List<VoiceNote>,
    noteInput: String,
    onNoteInputChange: (String) -> Unit,
    onAddNote: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Add note input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = noteInput,
                onValueChange = onNoteInputChange,
                placeholder = { Text("Add a note...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onAddNote,
                enabled = noteInput.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .background(
                        if (noteInput.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Outlined.Send,
                    contentDescription = "Send",
                    tint = if (noteInput.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Notes list
        if (notes.isEmpty() && voiceNotes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Note,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No notes yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            notes.forEachIndexed { index, note ->
                NoteBubble(text = note, index = index)
            }
            voiceNotes.forEach { vn ->
                VoiceNoteBubble(voiceNote = vn)
            }
        }
    }
}

@Composable
private fun NoteBubble(text: String, index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(
                Icons.Outlined.StickyNote2,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun VoiceNoteBubble(voiceNote: VoiceNote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Mic,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = voiceNote.transcription.ifBlank { "Voice Note" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTimer(voiceNote.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Outlined.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
