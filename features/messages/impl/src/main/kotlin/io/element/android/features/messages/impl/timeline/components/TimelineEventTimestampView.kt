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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.isEdited
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.api.timeline.item.event.isCritical
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineEventTimestampView(
    event: TimelineItem.Event,
    onShieldClick: (MessageShield) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedTime = event.sentTime
    val hasUnrecoverableError = event.localSendState is LocalEventSendState.SendingFailed.Unrecoverable
    val hasEncryptionCritical = event.messageShield?.isCritical().orFalse()
    val isMessageEdited = event.content.isEdited()
    val tint = if (hasUnrecoverableError || hasEncryptionCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    Row(
        modifier = Modifier
            .padding(PaddingValues(start = TimelineEventTimestampViewDefaults.spacing))
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isMessageEdited) {
            Text(
                stringResource(CommonStrings.common_edited_suffix),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = tint,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            formattedTime,
            style = ElementTheme.typography.fontBodyXsRegular,
            color = tint,
        )
        if (hasUnrecoverableError) {
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = CompoundIcons.Error(),
                contentDescription = stringResource(id = CommonStrings.common_sending_failed),
                tint = tint,
                modifier = Modifier.size(15.dp, 18.dp),
            )
        }
        event.messageShield?.let { shield ->
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = shield.toIcon(),
                contentDescription = null,
                modifier = Modifier
                    .size(15.dp)
                    .clickable { onShieldClick(shield) },
                tint = shield.toIconColor(),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineEventTimestampViewPreview(@PreviewParameter(TimelineItemEventForTimestampViewProvider::class) event: TimelineItem.Event) = ElementPreview {
    TimelineEventTimestampView(
        event = event,
        onShieldClick = {},
    )
}

object TimelineEventTimestampViewDefaults {
    val spacing = 16.dp
}
