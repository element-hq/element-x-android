/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.virtual

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
internal fun TimelineItemReadMarkerView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 18.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(id = R.string.screen_room_timeline_read_marker_title).uppercase(),
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textActionAccent,
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            color = ElementTheme.colors.textActionAccent,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemReadMarkerViewPreview() = ElementPreview {
    TimelineItemReadMarkerView()
}
