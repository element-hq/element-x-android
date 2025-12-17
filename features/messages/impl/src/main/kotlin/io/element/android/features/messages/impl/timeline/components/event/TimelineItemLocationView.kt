/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.location.api.StaticMapView
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun TimelineItemLocationView(
    content: TimelineItemLocationContent,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        content.description?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            )
        }

        StaticMapView(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 188.dp),
            lat = content.location.lat,
            lon = content.location.lon,
            zoom = 15.0,
            contentDescription = content.body
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemLocationViewPreview(@PreviewParameter(TimelineItemLocationContentProvider::class) content: TimelineItemLocationContent) =
    ElementPreview {
        TimelineItemLocationView(
            content = content,
        )
    }
