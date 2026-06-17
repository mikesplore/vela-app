package com.template.app.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun rowDivider(cs: ColorScheme) = Modifier
    .fillMaxWidth()
    .height(0.5.dp)
    .background(cs.outlineVariant.copy(alpha = 0.3f))