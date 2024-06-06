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

package io.element.android.features.call.impl.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.call.impl.R
import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.libraries.designsystem.background.OnboardingBackground
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun IncomingCallScreen(
    notificationData: CallNotificationData,
    onAnswer: (CallNotificationData) -> Unit,
    onCancel: () -> Unit,
) {
    ElementTheme {
        OnboardingBackground()
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 124.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(
                    avatarData = AvatarData(
                        id = notificationData.senderId.value,
                        name = notificationData.senderName,
                        url = notificationData.avatarUrl,
                        size = AvatarSize.IncomingCall,
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = notificationData.senderName ?: notificationData.senderId.value,
                    style = ElementTheme.typography.fontHeadingMdBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.screen_incoming_call_subtitle_android),
                    style = ElementTheme.typography.fontBodyLgRegular,
                    color = ElementTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    size = 64.dp,
                    onClick = { onAnswer(notificationData) },
                    icon = CompoundIcons.VoiceCall(),
                    title = stringResource(CommonStrings.action_accept),
                    backgroundColor = ElementTheme.colors.iconSuccessPrimary,
                    borderColor = ElementTheme.colors.borderSuccessSubtle
                )

                ActionButton(
                    size = 64.dp,
                    onClick = onCancel,
                    icon = CompoundIcons.EndCall(),
                    title = stringResource(CommonStrings.action_reject),
                    backgroundColor = ElementTheme.colors.iconCriticalPrimary,
                    borderColor = ElementTheme.colors.borderCriticalSubtle
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    size: Dp,
    onClick: () -> Unit,
    icon: ImageVector,
    title: String,
    backgroundColor: Color,
    borderColor: Color,
    contentDescription: String? = title,
    borderSize: Dp = 1.33.dp,
) {
    Column(
        modifier = Modifier.width(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            modifier = Modifier.size(size + borderSize)
                .border(borderSize, borderColor, CircleShape),
            onClick = onClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = backgroundColor,
                contentColor = Color.White,
            )
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyLgMedium,
            color = ElementTheme.colors.textPrimary,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun IncomingCallScreenPreview() {
    ElementPreview {
        IncomingCallScreen(
            notificationData = CallNotificationData(
                sessionId = SessionId("@alice:matrix.org"),
                roomId = RoomId("!1234:matrix.org"),
                eventId = EventId("\$asdadadsad:matrix.org"),
                senderId = UserId("@bob:matrix.org"),
                roomName = "A room",
                senderName = "Bob",
                avatarUrl = null,
                notificationChannelId = "incoming_call",
                timestamp = 0L,
            ),
            onAnswer = {},
            onCancel = {},
        )
    }
}
