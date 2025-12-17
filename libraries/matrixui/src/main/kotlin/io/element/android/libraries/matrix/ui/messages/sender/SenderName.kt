/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages.sender

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails

// https://www.figma.com/file/Ni6Ii8YKtmXCKYNE90cC67/Timeline-(new)?type=design&node-id=917-80169&mode=design&t=A0CJCBbMqR8NOwUQ-0
@Composable
fun SenderName(
    senderId: UserId,
    senderProfile: ProfileDetails,
    senderNameMode: SenderNameMode,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (senderProfile) {
            is ProfileDetails.Error,
            ProfileDetails.Pending,
            ProfileDetails.Unavailable -> {
                MainText(text = senderId.value, mode = senderNameMode)
            }
            is ProfileDetails.Ready -> {
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
        SenderNameMode.Reply -> ElementTheme.colors.textPrimary
    }
    Text(
        modifier = modifier.clipToBounds(),
        text = text,
        style = style,
        color = color,
        maxLines = 1,
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
        color = ElementTheme.colors.textSecondary,
        maxLines = 1,
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
        senderProfile = senderNameData.profileDetails,
        senderNameMode = senderNameData.senderNameMode,
    )
}
