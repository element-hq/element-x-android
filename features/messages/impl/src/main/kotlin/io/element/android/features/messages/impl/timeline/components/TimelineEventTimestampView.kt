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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.isEdited
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineEventTimestampView(
    event: TimelineItem.Event,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedTime = event.sentTime
    val hasMessageSendingFailed = event.localSendState is LocalEventSendState.SendingFailed
    val isMessageEdited = event.content.isEdited()
    val tint = if (hasMessageSendingFailed) MaterialTheme.colorScheme.error else null
    val clickModifier = if (hasMessageSendingFailed) {
        Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            indication = rememberRipple(bounded = false),
            interactionSource = remember { MutableInteractionSource() }
        )
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .then(clickModifier)
            .padding(TimelineEventTimestampViewDefaults.padding) // Add extra padding for touch target size
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isMessageEdited) {
            Text(
                stringResource(CommonStrings.common_edited_suffix),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = tint ?: MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            formattedTime,
            style = ElementTheme.typography.fontBodyXsRegular,
            color = tint ?: MaterialTheme.colorScheme.secondary,
        )
        if (hasMessageSendingFailed && tint != null) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = CompoundIcons.Error,
                contentDescription = stringResource(id = CommonStrings.common_sending_failed),
                tint = tint,
                modifier = Modifier.size(15.dp, 18.dp),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineEventTimestampViewPreview(@PreviewParameter(TimelineItemEventForTimestampViewProvider::class) event: TimelineItem.Event) = ElementPreview {
    TimelineEventTimestampView(
        event = event,
        onClick = {},
        onLongClick = {},
    )
}

object TimelineEventTimestampViewDefaults {
    val padding = PaddingValues(start = 16.dp)
}
