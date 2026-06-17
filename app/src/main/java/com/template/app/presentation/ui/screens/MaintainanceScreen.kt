package com.template.app.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.template.app.domain.model.VelaPackageUpdate
import com.template.app.domain.model.VelaService
import com.template.app.presentation.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    onBack: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Maintenance", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Quick Actions
            MaintenanceSection(title = "Quick Actions") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton(
                        label = "Clear Cache",
                        icon = Icons.Default.DeleteSweep,
                        onClick = { viewModel.clearCache() },
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        label = "Sync Time",
                        icon = Icons.Default.Sync,
                        isAccent = true,
                        onClick = { viewModel.syncTime() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Updates (Using VelaMaintenanceUpdate mapping)
            if (uiState.availableUpdates.isNotEmpty()) {
                MaintenanceSection(title = "Pending Updates") {
                    UpdateCard(
                        packages = uiState.availableUpdates,
                        onRunUpdate = { viewModel.runUpdates() }
                    )
                }
            }

            // 3. Services (Using VelaService mapping)
            MaintenanceSection(title = "Core Services") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.services.forEach { service ->
                        ServiceItem(
                            service = service,
                            onStart = { viewModel.startService(service.name) },
                            onStop = { viewModel.stopService(service.name) },
                            onRestart = { viewModel.restartService(service.name) }
                        )
                    }
                }
            }

            // 4. Logs (Using VelaLogs mapping)
            MaintenanceSection(title = "System Logs") {
                LogViewer(
                    currentService = uiState.logFilter,
                    logs = uiState.recentLogs
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MaintenanceSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
}

@Composable
private fun UpdateCard(packages: List<VelaPackageUpdate>, onRunUpdate: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color(0xFFE8A440))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${packages.size} System Packages Ready",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                packages.take(3).forEach { pkg ->
                    Text(
                        text = "• ${pkg.name} → ${pkg.version}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                if (packages.size > 3) {
                    Text(
                        "and ${packages.size - 3} more...",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Button(
                onClick = onRunUpdate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8A440), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Execute Update Sequence", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ServiceItem(
    service: VelaService,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        if (service.active) Color(0xFF6FCB72) else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (service.active) "Running" else "Stopped",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (service.active) {
                    ServiceActionButton(Icons.Default.Stop, onClick = onStop, isDanger = true)
                    ServiceActionButton(Icons.Default.RestartAlt, onClick = onRestart)
                } else {
                    ServiceActionButton(Icons.Default.PlayArrow, onClick = onStart)
                }
            }
        }
    }
}

@Composable
private fun ServiceActionButton(icon: ImageVector, onClick: () -> Unit, isDanger: Boolean = false) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        colors = if (isDanger) IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error
        ) else IconButtonDefaults.filledTonalIconButtonColors()
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun LogViewer(currentService: String, logs: List<String>) {
    Column {
        Surface(
            color = Color.Black,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    logs.forEach { log ->
                        Text(
                            text = log,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Green.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }

                // Overlay label
                Text(
                    text = "LOGS: $currentService",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, isAccent: Boolean = false) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isAccent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = if (isAccent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}