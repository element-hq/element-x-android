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

package io.element.android.features.messages.impl.timeline.components.virtual

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModelProvider
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
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
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = model.formattedDate,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Preview
@Composable
internal fun TimelineItemDaySeparatorViewLightPreview(
    @PreviewParameter(TimelineItemDaySeparatorModelProvider::class) model: TimelineItemDaySeparatorModel
) =
    ElementPreviewLight { ContentToPreview(model) }

@Preview
@Composable
internal fun TimelineItemDaySeparatorViewDarkPreview(
    @PreviewParameter(TimelineItemDaySeparatorModelProvider::class) model: TimelineItemDaySeparatorModel
) =
    ElementPreviewDark { ContentToPreview(model) }

@Composable
private fun ContentToPreview(model: TimelineItemDaySeparatorModel) {
    TimelineItemDaySeparatorView(
        model = model,
    )
}
