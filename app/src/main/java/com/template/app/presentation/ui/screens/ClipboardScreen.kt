package com.template.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.ClipboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen(
    viewModel: ClipboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CLIPBOARD",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontSize = 18.sp,
                        color = colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = colorScheme.secondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Remote Content Card
            VelaCard(title = "Device Clipboard") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).align(Alignment.Center),
                            color = colorScheme.secondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = state.content.ifBlank { "Clipboard is empty" },
                            color = if (state.content.isBlank()) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                            fontSize = 15.sp,
                            minLines = 3
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { clipboardManager.setText(AnnotatedString(state.content)) },
                        enabled = state.content.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy to Phone", fontSize = 13.sp)
                    }
                    
                    IconButton(
                        onClick = { viewModel.clearClipboard() },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorScheme.error.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, tint = colorScheme.error)
                    }
                }
            }

            // Write Card
            VelaCard(title = "Send to Device") {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter text to sync...", color = colorScheme.onSurfaceVariant) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface,
                        cursorColor = colorScheme.secondary,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        viewModel.writeClipboard(inputText)
                        inputText = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inputText.isNotBlank() && !state.isUpdating,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorScheme.onSurfaceVariant)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isUpdating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Update Remote Clipboard", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            state.error?.let {
                ErrorMessage(it)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun VelaCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colorScheme.surface)
            .padding(20.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.secondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}
