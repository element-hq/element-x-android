/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.virtual

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModelProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
internal fun TimelineItemDaySeparatorView(
    model: TimelineItemDaySeparatorModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = model.formattedDate,
            style = ElementTheme.typography.fontBodyMdMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemDaySeparatorViewPreview(
    @PreviewParameter(TimelineItemDaySeparatorModelProvider::class) model: TimelineItemDaySeparatorModel
) = ElementPreview {
    TimelineItemDaySeparatorView(
        model = model,
    )
}
