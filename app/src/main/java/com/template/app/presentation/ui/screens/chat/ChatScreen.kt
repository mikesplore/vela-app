package com.template.app.presentation.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.template.app.presentation.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll logic
    LaunchedEffect(state.messages.size, state.isLoading) {
        val totalItems = state.messages.size + (if (state.isLoading) 1 else 0)
        if (totalItems == 0) return@LaunchedEffect
        
        // If the last message was from the user, always scroll to bottom
        val lastIsUser = state.messages.lastOrNull()?.isUser == true
        
        // Check if we're already near the bottom
        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val isNearBottom = lastVisibleItem >= totalItems - 3

        if (lastIsUser || isNearBottom) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = state.messages.size + (if (state.isLoading) 1 else 0)
            totalItems > 5 && lastVisible < totalItems - 2
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (state.messages.isEmpty() && !state.isLoading) {
                    EmptyState(
                        onSuggestionClick = { suggestion ->
                            viewModel.onInputTextChanged(suggestion)
                            viewModel.sendMessage()
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, 
                            end = 16.dp, 
                            top = 16.dp, 
                            bottom = 80.dp // Extra padding for the input bar
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(
                            items = state.messages, 
                            key = { _, item -> item.id }
                        ) { _, message ->
                            MessageBubble(
                                message = message,
                                onConfirm = { viewModel.confirmAction(true) },
                                onCancel = { viewModel.confirmAction(false) },
                                onPinSubmit = { viewModel.submitPin(it) },
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(220),
                                    placementSpec = tween(220)
                                )
                            )
                        }

                        if (state.isLoading) {
                            item(key = "typing-indicator") {
                                TypingIndicator(
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(220),
                                        placementSpec = tween(220)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            ChatInputBar(
                text = state.inputText,
                onTextChanged = viewModel::onInputTextChanged,
                onSend = viewModel::sendMessage,
                isLoading = state.isLoading,
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            )
        }

        AnimatedVisibility(
            visible = showScrollToBottom,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        ) {
            ScrollToBottomButton {
                coroutineScope.launch {
                    val totalItems = state.messages.size + (if (state.isLoading) 1 else 0)
                    if (totalItems > 0) {
                        listState.animateScrollToItem(totalItems - 1)
                    }
                }
            }
        }
    }
}
