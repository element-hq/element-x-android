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

package io.element.android.features.invitelist.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.invitelist.impl.R
import io.element.android.features.invitelist.impl.model.InviteListInviteSummary
import io.element.android.features.invitelist.impl.model.InviteListInviteSummaryProvider
import io.element.android.features.invitelist.impl.model.InviteSender
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.roomListUnreadIndicator
import kotlinx.collections.immutable.persistentMapOf
import io.element.android.libraries.ui.strings.R as StringR

private val minHeight = 72.dp

@Composable
internal fun InviteSummaryRow(
    invite: InviteListInviteSummary,
    modifier: Modifier = Modifier,
    onAcceptClicked: () -> Unit = {},
    onDeclineClicked: () -> Unit = {},
) {
    Box(
        modifier = modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
    ) {
        DefaultInviteSummaryRow(
            invite = invite,
            onAcceptClicked = onAcceptClicked,
            onDeclineClicked = onDeclineClicked,
        )
    }
}

@Composable
internal fun DefaultInviteSummaryRow(
    invite: InviteListInviteSummary,
    onAcceptClicked: () -> Unit = {},
    onDeclineClicked: () -> Unit = {},
) {
    Row(
        modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Avatar(
            invite.roomAvatarData,
        )

        Column(
            modifier = Modifier
                    .padding(start = 12.dp, end = 4.dp)
                    .alignByBaseline()
                    .weight(1f)
        ) {
            // Name
            Text(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                text = invite.roomName,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // ID or Alias
            invite.roomAlias?.let {
                Text(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    text = it,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sender
            invite.sender?.let { sender ->
                SenderRow(sender = sender)
            }

            // CTAs
            Row(Modifier.padding(top = 12.dp)) {
                OutlinedButton(
                    content = { Text(stringResource(StringR.string.action_decline), style = ElementTextStyles.Button) },
                    onClick = onDeclineClicked,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 7.dp),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    content = { Text(stringResource(StringR.string.action_accept), style = ElementTextStyles.Button) },
                    onClick = onAcceptClicked,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 7.dp),
                )
            }
        }

        val unreadIndicatorColor = if (invite.isNew) MaterialTheme.roomListUnreadIndicator() else Color.Transparent

        Box(
            modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(unreadIndicatorColor)
        )
    }
}

@Composable
private fun SenderRow(sender: InviteSender) {
    Text(
        text = buildAnnotatedString {
            val placeholder = "$"
            val text = stringResource(R.string.screen_invites_invited_you, placeholder)
            val nameIndex = text.indexOf(placeholder)

            // Text before the placeholder
            append(text.take(nameIndex))

            // Avatar and display name
            appendInlineContent("avatar")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                append(sender.displayName)
            }

            // Text after the placeholder
            append(text.drop(nameIndex + placeholder.length))
        },
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(top = 6.dp),
        inlineContent = persistentMapOf(
            "avatar" to InlineTextContent(
                with(LocalDensity.current) {
                    Placeholder(20.dp.toSp(), 20.dp.toSp(), PlaceholderVerticalAlign.Center)
                }
            ) {
                Box(
                        Modifier
                                .fillMaxHeight()
                                .padding(end = 4.dp)
                ) {
                    Avatar(
                        avatarData = sender.avatarData.copy(size = AvatarSize.Custom(16.dp)),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        )
    )
}

@Preview
@Composable
internal fun InviteSummaryRowLightPreview(@PreviewParameter(InviteListInviteSummaryProvider::class) data: InviteListInviteSummary) =
    ElementPreviewLight { ContentToPreview(data) }

@Preview
@Composable
internal fun InviteSummaryRowDarkPreview(@PreviewParameter(InviteListInviteSummaryProvider::class) data: InviteListInviteSummary) =
    ElementPreviewDark { ContentToPreview(data) }

@Composable
private fun ContentToPreview(data: InviteListInviteSummary) {
    InviteSummaryRow(data)
}
