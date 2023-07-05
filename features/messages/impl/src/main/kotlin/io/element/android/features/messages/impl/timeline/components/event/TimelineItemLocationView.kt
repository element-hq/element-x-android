/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.location.api.StaticMapView
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight

@Composable
fun TimelineItemLocationView(
    content: TimelineItemLocationContent,
    modifier: Modifier = Modifier,
) {
    StaticMapView(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 188.dp),
        lat = content.location.lat,
        lon = content.location.lon,
        zoom = 15.0,
        contentDescription = content.body
    )
}

@Preview
@Composable
internal fun TimelineItemLocationViewLightPreview(@PreviewParameter(TimelineItemLocationContentProvider::class) content: TimelineItemLocationContent) =
    ElementPreviewLight { ContentToPreview(content) }

@Preview
@Composable
internal fun TimelineItemLocationViewDarkPreview(@PreviewParameter(TimelineItemLocationContentProvider::class) content: TimelineItemLocationContent) =
    ElementPreviewDark { ContentToPreview(content) }

@Composable
private fun ContentToPreview(content: TimelineItemLocationContent) {
    TimelineItemLocationView(content)
}
