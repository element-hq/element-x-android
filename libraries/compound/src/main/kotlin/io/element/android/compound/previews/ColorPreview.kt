/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.utils.toHrf

@Composable
fun ColorPreview(
    backgroundColor: Color,
    foregroundColor: Color,
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = name + " " + color.toHrf(),
            fontSize = 6.sp,
            color = foregroundColor,
        )
        val backgroundBrush = Brush.linearGradient(
            listOf(
                backgroundColor,
                foregroundColor,
            )
        )
        Row(
            modifier = Modifier.background(backgroundBrush)
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .padding(1.dp)
                        .background(Color.White)
                        .background(color = color)
                        .height(10.dp)
                        .weight(1f)
                )
            }
        }
    }
}
