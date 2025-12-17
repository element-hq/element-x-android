/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.preview

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.utils.CommonDrawables

@Composable
@Suppress("ModifierMissing")
fun ElementThemedPreview(
    showBackground: Boolean = true,
    vertical: Boolean = true,
    @DrawableRes
    drawableFallbackForImages: Int = CommonDrawables.sample_background,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(Color.Gray)
            .padding(4.dp)
    ) {
        if (vertical) {
            Column {
                ElementPreview(
                    darkTheme = false,
                    showBackground = showBackground,
                    drawableFallbackForImages = drawableFallbackForImages,
                    content = content,
                )
                Spacer(modifier = Modifier.height(4.dp))
                ElementPreview(
                    darkTheme = true,
                    showBackground = showBackground,
                    drawableFallbackForImages = drawableFallbackForImages,
                    content = content
                )
            }
        } else {
            Row {
                ElementPreview(
                    darkTheme = false,
                    showBackground = showBackground,
                    drawableFallbackForImages = drawableFallbackForImages,
                    content = content,
                )
                Spacer(modifier = Modifier.width(4.dp))
                ElementPreview(
                    darkTheme = true,
                    showBackground = showBackground,
                    drawableFallbackForImages = drawableFallbackForImages,
                    content = content
                )
            }
        }
    }
}
