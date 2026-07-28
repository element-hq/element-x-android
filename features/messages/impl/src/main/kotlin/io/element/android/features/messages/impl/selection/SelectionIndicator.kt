/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon

private val INDICATOR_SIZE = 40.dp
private val INDICATOR_ICON_SIZE = 22.dp

@Composable
internal fun SelectionIndicator(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        // The row itself is the toggle surface, so this must not be a second focus stop.
        modifier = modifier
            .size(INDICATOR_SIZE)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(INDICATOR_ICON_SIZE),
            imageVector = if (checked) CompoundIcons.CheckCircleSolid() else CompoundIcons.Circle(),
            contentDescription = null,
            tint = if (checked) ElementTheme.colors.iconAccentPrimary else ElementTheme.colors.iconTertiary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SelectionIndicatorPreview() = ElementPreview {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SelectionIndicator(checked = false)
        SelectionIndicator(checked = true)
    }
}
