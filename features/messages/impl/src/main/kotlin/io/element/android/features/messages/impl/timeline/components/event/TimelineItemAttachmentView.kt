/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue

/**
 * package-private, you should only use TimelineItemFileView and TimelineItemAudioView.
 * https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2019-8180
 */
@Composable
fun TimelineItemAttachmentView(
    icon: ImageVector,
    iconContentDescription: String?,
    filename: String,
    fileExtensionAndSize: String,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
    contentValidationValue: ContentValidationValue = ContentValidationValue.Valid,
) {
    Column(
        modifier = modifier,
    ) {
        TimelineItemAttachmentHeaderView(
            icon = icon,
            iconContentDescription = iconContentDescription,
            filename = filename,
            fileExtensionAndSize = fileExtensionAndSize,
            isLoading = !contentValidationValue.isValidated(),
            onContentLayoutChange = onContentLayoutChange,
        )
    }
}

@Composable
private fun TimelineItemAttachmentHeaderView(
    icon: ImageVector,
    iconContentDescription: String?,
    filename: String,
    fileExtensionAndSize: String,
    isLoading: Boolean,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconSize = 36.dp
    val spacing = 8.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .background(ElementTheme.colors.bgCanvasDefault, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = ElementTheme.colors.iconPrimary,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = ElementTheme.colors.iconPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Column {
            Text(
                text = filename,
                color = ElementTheme.colors.textPrimary,
                maxLines = 2,
                style = ElementTheme.typography.fontBodyLgRegular,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = fileExtensionAndSize,
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodySmRegular,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = ContentAvoidingLayout.measureLastTextLine(
                    onContentLayoutChange = onContentLayoutChange,
                    extraWidth = iconSize + spacing
                ),
            )
        }
    }
}
