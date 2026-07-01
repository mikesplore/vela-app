package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.domain.model.*
import com.template.app.presentation.ui.components.SectionHeader
import com.template.app.presentation.viewmodel.NetworkViewModel

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    var showWifiDialog by remember { mutableStateOf<VelaWifiNetwork?>(null) }
    var wifiPassword by remember { mutableStateOf("") }

    if (showWifiDialog != null) {
        AlertDialog(
            onDismissRequest = { showWifiDialog = null },
            title = { Text("Connect to ${showWifiDialog?.ssid}") },
            text = {
                OutlinedTextField(
                    value = wifiPassword,
                    onValueChange = { wifiPassword = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.connectWifi(showWifiDialog!!.ssid, wifiPassword)
                    showWifiDialog = null
                    wifiPassword = ""
                }) { Text("Connect") }
            },
            dismissButton = {
                TextButton(onClick = { showWifiDialog = null }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // IP & Location Section
        item {
            NetworkSection(label = "IP & LOCATION") {
                InfoRow(label = "Local IP", value = state.networkInfo?.localIp ?: "---")
                InfoRow(label = "Public IP", value = state.networkInfo?.publicIp ?: "---")
                val loc = state.networkInfo?.location
                if (loc != null) {
                    InfoRow(label = "Location", value = "${loc.city}, ${loc.country}")
                    InfoRow(label = "ISP", value = loc.isp ?: "---")
                    InfoRow(label = "Coordinates", value = "${loc.lat}, ${loc.lon}")
                } else {
                    InfoRow(label = "Location", value = "---")
                }
            }
        }

        item { SectionDivider() }

        // Usage Section
        item {
            NetworkSection(label = "DATA USAGE") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("hour", "day", "month").forEach { period ->
                        FilterChip(
                            selected = state.selectedPeriod == period,
                            onClick = { viewModel.fetchNetworkUsage(period) },
                            label = { Text(period.replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (state.isUsageLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }

                state.netUsage?.let { usage ->
                    UsageRow(label = "Received", value = usage.received, icon = Icons.Default.Download, color = cs.primary)
                    UsageRow(label = "Transmitted", value = usage.transmitted, icon = Icons.Default.Upload, color = cs.secondary)
                    Text(
                        text = "Interface: ${usage.interfaceName}",
                        fontSize = 11.sp,
                        color = cs.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } ?: run {
                    if (!state.isUsageLoading) {
                        Text("Usage data unavailable", fontSize = 13.sp, color = cs.onSurfaceVariant)
                    }
                }
            }
        }

        item { SectionDivider() }

        // Wi-Fi Section
        item {
            NetworkSection(label = "WI-FI") {
                val wifiEnabled = state.wifiStatus?.isEnabled ?: true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (wifiEnabled) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                            contentDescription = null,
                            tint = if (wifiEnabled) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (wifiEnabled) "Wi-Fi Enabled" else "Wi-Fi Disabled",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.onSurface.copy(alpha = 0.85f)
                        )
                    }
                    Switch(
                        checked = wifiEnabled,
                        onCheckedChange = { viewModel.toggleWifi(it) },
                        enabled = !state.isWifiToggling
                    )
                }

                if (wifiEnabled) {
                    state.wifiStatus?.let { status ->
                        if (status.connected && status.ssid != null) {
                            WifiItem(
                                ssid = status.ssid!!,
                                signal = status.signal ?: 0,
                                isConnected = true,
                                onClick = {}
                            )
                        }
                        status.availableNetworks.forEach { net ->
                            if (!net.isActive) {
                                WifiItem(
                                    ssid = net.ssid,
                                    signal = net.signal ?: 0,
                                    isConnected = false,
                                    onClick = { showWifiDialog = net }
                                )
                            }
                        }
                    }
                }

                if (state.wifiStatus?.connected == true) {
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.disconnectWifi() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.error.copy(alpha = 0.1f),
                            contentColor = cs.error
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Disconnect", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { SectionDivider() }

        // Bluetooth Section
        item {
            NetworkSection(label = "BLUETOOTH") {
                val btEnabled = state.bluetoothStatus?.isEnabled ?: true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (btEnabled) Icons.Rounded.Bluetooth else Icons.Rounded.BluetoothDisabled,
                            contentDescription = null,
                            tint = if (btEnabled) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (btEnabled) "Bluetooth Enabled" else "Bluetooth Disabled",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = cs.onSurface.copy(alpha = 0.85f)
                        )
                    }
                    Switch(
                        checked = btEnabled,
                        onCheckedChange = { viewModel.toggleBluetooth(it) }
                    )
                }

                if (btEnabled) {
                    if (state.isBluetoothLoading) {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }

                    state.bluetoothStatus?.let { status ->
                        if (status.connectedDevices.isNotEmpty()) {
                            Text(
                                "Connected",
                                fontSize = 12.sp,
                                color = cs.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                            status.connectedDevices.forEach { device ->
                                BluetoothItem(
                                    device = device,
                                    onConnect = { viewModel.connectBluetooth(device.address) },
                                    onDisconnect = { viewModel.disconnectBluetooth(device.address) },
                                    onPair = { viewModel.pairBluetooth(device.address) },
                                    onUnpair = { viewModel.unpairBluetooth(device.address) }
                                )
                            }
                        }

                        if (status.pairedDevices.isNotEmpty()) {
                            Text(
                                "Paired Devices",
                                fontSize = 12.sp,
                                color = cs.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                            status.pairedDevices.forEach { device ->
                                if (!device.isConnected) {
                                    BluetoothItem(
                                        device = device,
                                        onConnect = { viewModel.connectBluetooth(device.address) },
                                        onDisconnect = { viewModel.disconnectBluetooth(device.address) },
                                        onPair = { viewModel.pairBluetooth(device.address) },
                                        onUnpair = { viewModel.unpairBluetooth(device.address) }
                                    )
                                }
                            }
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.fetchBluetoothDevices() },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.secondaryContainer,
                            contentColor = cs.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Refresh Devices", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(12.dp))
            Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun NetworkSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp)) {
        SectionHeader(label)
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = cs.onSurfaceVariant.copy(alpha = 0.6f))
        Text(text = value, fontSize = 13.sp, color = cs.onSurface.copy(alpha = 0.82f), fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WifiItem(ssid: String, signal: Int, isConnected: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(cs.surfaceVariant.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Wifi,
                contentDescription = null,
                tint = if (isConnected) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = ssid, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isConnected) cs.primary else cs.onSurface.copy(alpha = 0.85f))
            if (isConnected) Text(text = "Connected", fontSize = 11.sp, color = cs.onSurfaceVariant.copy(alpha = 0.4f))
        }
        Text(text = "$signal%", fontSize = 12.sp, color = cs.onSurfaceVariant.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun BluetoothItem(
    device: VelaBluetoothDevice,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onPair: () -> Unit,
    onUnpair: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(cs.surfaceVariant.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (device.isConnected) Icons.Rounded.BluetoothConnected else Icons.Rounded.Bluetooth,
                contentDescription = null,
                tint = if (device.isConnected) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = device.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = cs.onSurface.copy(alpha = 0.85f))
            Text(text = device.address, fontSize = 10.sp, color = cs.onSurfaceVariant.copy(alpha = 0.5f))
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (device.isPaired) {
                if (device.isConnected) {
                    Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.error, modifier = Modifier.clickable { onDisconnect() })
                } else {
                    Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.primary, modifier = Modifier.clickable { onConnect() })
                }
                Text("Unpair", fontSize = 11.sp, color = cs.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.clickable { onUnpair() })
            } else {
                Text("Pair", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.primary, modifier = Modifier.clickable { onPair() })
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
}
