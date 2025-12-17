/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemInformativeView(
    text: String,
    iconDescription: String,
    @DrawableRes iconResourceId: Int,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.onSizeChanged { size ->
            onContentLayoutChange(
                ContentAvoidingLayoutData(
                    contentWidth = size.width,
                    contentHeight = size.height,
                )
            )
        },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.height(20.dp)
        ) {
            Icon(
                resourceId = iconResourceId,
                tint = ElementTheme.colors.iconSecondary,
                contentDescription = iconDescription,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            fontStyle = FontStyle.Italic,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyMdRegular,
            text = text
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemInformativeViewPreview() = ElementPreview {
    TimelineItemInformativeView(
        text = "Info",
        iconDescription = "",
        iconResourceId = CompoundDrawables.ic_compound_delete,
        onContentLayoutChange = {},
    )
}
