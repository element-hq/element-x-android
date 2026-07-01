/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@Composable
fun TimelineItemDangerousFileView(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.background(ElementTheme.colors.bgCriticalSubtle),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = CompoundIcons.Error(),
            contentDescription = null,
            tint = ElementTheme.colors.iconCriticalPrimary
        )
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "The file is not safe",
                color = ElementTheme.colors.textCriticalPrimary,
                style = ElementTheme.typography.fontBodyLgMedium,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Download has been disabled.",
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodyMdRegular,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemDangerousFileViewPreview() = ElementPreview {
    TimelineItemDangerousFileView()
}
