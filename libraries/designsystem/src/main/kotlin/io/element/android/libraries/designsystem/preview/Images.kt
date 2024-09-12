/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.preview

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import io.element.android.libraries.designsystem.R

/**
 * I wanted to set up a FakeImageLoader as per https://github.com/coil-kt/coil/issues/1327
 * but it does not render in preview. In the meantime, you can use this trick to have image.
 */
@Composable
fun debugPlaceholder(
    @DrawableRes debugPreview: Int,
    nonDebugPainter: Painter? = null,
) = if (LocalInspectionMode.current) {
    painterResource(id = debugPreview)
} else {
    nonDebugPainter
}

@Composable
fun debugPlaceholderBackground(nonDebugPainter: Painter? = null): Painter? {
    return debugPlaceholder(debugPreview = R.drawable.sample_background, nonDebugPainter)
}

@Composable
fun debugPlaceholderAvatar(nonDebugPainter: Painter? = null): Painter? {
    return debugPlaceholder(debugPreview = R.drawable.sample_avatar, nonDebugPainter)
}
