/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.unreadIndicator

@Composable
fun UnreadIndicatorAtom(
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    color: Color = ElementTheme.colors.unreadIndicator,
    isVisible: Boolean = true,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
            .size(size)
            .clip(CircleShape)
            .background(if (isVisible) color else Color.Transparent)
    )
}

@PreviewsDayNight
@Composable
internal fun UnreadIndicatorAtomPreview() = ElementPreview {
    UnreadIndicatorAtom()
}
