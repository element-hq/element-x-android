/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    count: Long? = null,
    color: Color = ElementTheme.colors.unreadIndicator,
    isVisible: Boolean = true,
    contentDescription: String? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues.Zero,
) {
    when {
        !isVisible -> Spacer(modifier = modifier.size(size))
        else -> CounterAtom(
            count = count?.toInt(),
            modifier = modifier
                .semantics {
                    contentDescription?.let { this.contentDescription = it }
                }
                .then(if (border != null) Modifier.border(border, RoundedCornerShape(percent = 50)) else Modifier),
            containerColor = color,
            contentColor = ElementTheme.colors.bgCanvasDefault,
            textStyle = ElementTheme.typography.fontBodySmMedium,
            contentPadding = contentPadding,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun UnreadIndicatorAtomPreview() = ElementPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        UnreadIndicatorAtom(count = null)
        UnreadIndicatorAtom(count = 0)
        UnreadIndicatorAtom(count = 1)
        UnreadIndicatorAtom(count = 10)
        UnreadIndicatorAtom(count = 99)
        UnreadIndicatorAtom(count = 999)
    }
}
