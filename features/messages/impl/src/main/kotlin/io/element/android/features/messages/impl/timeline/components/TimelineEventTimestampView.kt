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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.timeline.item.event.EventSendState
import io.element.android.libraries.ui.strings.R

@Composable
fun TimelineEventTimestampView(
    event: TimelineItem.Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTime = event.sentTime
    val hasMessageSendingFailed = event.sendState is EventSendState.SendingFailed
    val isMessageEdited = (event.content as? TimelineItemTextBasedContent)?.isEdited.orFalse()
    val tint = if (hasMessageSendingFailed) ElementTheme.colors.textActionCritical else null
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isMessageEdited) {
            Text(
                stringResource(R.string.common_edited_suffix),
                style = ElementTextStyles.Regular.caption2,
                color = tint ?: MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            formattedTime,
            style = ElementTextStyles.Regular.caption2,
            color = tint ?: MaterialTheme.colorScheme.secondary,
        )
        if (hasMessageSendingFailed && tint != null) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(imageVector = Icons.Default.Error, contentDescription = "Error sending message", tint = tint, modifier = Modifier.size(15.dp, 18.dp))
        }
    }
}

@Preview
@Composable
internal fun TimelineEventTimestampViewLightPreview(@PreviewParameter(TimelineItemEventForTimestampViewProvider::class) event: TimelineItem.Event) =
    ElementPreviewLight { ContentToPreview(event) }

@Preview
@Composable
internal fun TimelineEventTimestampViewDarkPreview(@PreviewParameter(TimelineItemEventForTimestampViewProvider::class) event: TimelineItem.Event) =
    ElementPreviewDark { ContentToPreview(event) }

@Composable
private fun ContentToPreview(event: TimelineItem.Event) {
    TimelineEventTimestampView(
        event = event,
        onClick = {}
    )
}
