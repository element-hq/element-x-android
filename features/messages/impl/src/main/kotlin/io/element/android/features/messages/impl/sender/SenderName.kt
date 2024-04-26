/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.messages.impl.sender

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails

// https://www.figma.com/file/Ni6Ii8YKtmXCKYNE90cC67/Timeline-(new)?type=design&node-id=917-80169&mode=design&t=A0CJCBbMqR8NOwUQ-0
@Composable
fun SenderName(
    senderId: UserId,
    senderProfile: ProfileTimelineDetails,
    senderNameMode: SenderNameMode,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (senderProfile) {
            is ProfileTimelineDetails.Error,
            ProfileTimelineDetails.Pending,
            ProfileTimelineDetails.Unavailable -> {
                MainText(text = senderId.value, mode = senderNameMode)
            }
            is ProfileTimelineDetails.Ready -> {
                val displayName = senderProfile.displayName
                if (displayName.isNullOrEmpty()) {
                    MainText(text = senderId.value, mode = senderNameMode)
                } else {
                    MainText(text = displayName, mode = senderNameMode)
                    if (senderProfile.displayNameAmbiguous) {
                        SecondaryText(text = senderId.value, mode = senderNameMode)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.MainText(
    text: String,
    mode: SenderNameMode,
) {
    val style = when (mode) {
        is SenderNameMode.Timeline -> ElementTheme.typography.fontBodyMdMedium
        SenderNameMode.ActionList,
        SenderNameMode.Reply -> ElementTheme.typography.fontBodySmMedium
    }
    val modifier = when (mode) {
        is SenderNameMode.Timeline -> Modifier.alignByBaseline()
        SenderNameMode.ActionList,
        SenderNameMode.Reply -> Modifier
    }
    val color = when (mode) {
        is SenderNameMode.Timeline -> mode.mainColor
        SenderNameMode.ActionList,
        SenderNameMode.Reply -> MaterialTheme.colorScheme.primary
    }
    Text(
        modifier = modifier.clipToBounds(),
        text = text,
        style = style,
        color = color,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun RowScope.SecondaryText(
    text: String,
    mode: SenderNameMode,
) {
    val style = when (mode) {
        is SenderNameMode.Timeline -> ElementTheme.typography.fontBodySmRegular
        SenderNameMode.ActionList,
        SenderNameMode.Reply -> ElementTheme.typography.fontBodyXsRegular
    }
    val modifier = when (mode) {
        is SenderNameMode.Timeline -> Modifier.alignByBaseline()
        SenderNameMode.ActionList,
        SenderNameMode.Reply -> Modifier
    }
    Text(
        modifier = modifier.clipToBounds(),
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.secondary,
        overflow = TextOverflow.Ellipsis,
    )
}

@PreviewsDayNight
@Composable
internal fun SenderNamePreview(
    @PreviewParameter(SenderNameDataProvider::class) senderNameData: SenderNameData,
) = ElementPreview {
    SenderName(
        senderId = senderNameData.userId,
        senderProfile = senderNameData.profileTimelineDetails,
        senderNameMode = senderNameData.senderNameMode,
    )
}
