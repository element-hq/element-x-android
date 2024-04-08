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

package io.element.android.features.invite.impl.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.invite.impl.R
import io.element.android.features.invite.impl.model.InviteListInviteSummary
import io.element.android.features.invite.impl.model.InviteListInviteSummaryProvider
import io.element.android.features.invite.impl.model.InviteSender
import io.element.android.libraries.designsystem.atomic.atoms.UnreadIndicatorAtom
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings

private val minHeight = 72.dp

@Composable
internal fun InviteSummaryRow(
    invite: InviteListInviteSummary,
    onAcceptClicked: () -> Unit,
    onDeclineClicked: () -> Unit,
    modifier: Modifier = Modifier,
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
private fun DefaultInviteSummaryRow(
    invite: InviteListInviteSummary,
    onAcceptClicked: () -> Unit,
    onDeclineClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top
    ) {
        Avatar(
            invite.roomAvatarData,
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 4.dp)
                .alignByBaseline()
                .weight(1f)
        ) {
            val bonusPadding = if (invite.isNew) 12.dp else 0.dp

            // Name
            Text(
                text = invite.roomName,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ElementTheme.typography.fontBodyLgMedium,
                modifier = Modifier.padding(end = bonusPadding),
            )

            // ID or Alias
            invite.roomAlias?.let {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = it,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = bonusPadding),
                )
            }

            // Sender
            invite.sender?.let { sender ->
                SenderRow(sender = sender)
            }

            // CTAs
            Row(Modifier.padding(top = 12.dp)) {
                OutlinedButton(
                    text = stringResource(CommonStrings.action_decline),
                    onClick = onDeclineClicked,
                    modifier = Modifier.weight(1f),
                    size = ButtonSize.Medium,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    text = stringResource(CommonStrings.action_accept),
                    onClick = onAcceptClicked,
                    modifier = Modifier.weight(1f),
                    size = ButtonSize.Medium,
                )
            }
        }

        UnreadIndicatorAtom(isVisible = invite.isNew)
    }
}

@Composable
private fun SenderRow(sender: InviteSender) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 6.dp),
    ) {
        Avatar(
            avatarData = sender.avatarData,
        )
        Text(
            text = stringResource(R.string.screen_invites_invited_you, sender.displayName, sender.userId.value).let { text ->
                val senderNameStart = LocalContext.current.getString(R.string.screen_invites_invited_you).indexOf("%1\$s")
                AnnotatedString(
                    text = text,
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            start = senderNameStart,
                            end = senderNameStart + sender.displayName.length
                        )
                    )
                )
            },
            style = ElementTheme.typography.fontBodyMdRegular,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun InviteSummaryRowPreview(@PreviewParameter(InviteListInviteSummaryProvider::class) data: InviteListInviteSummary) = ElementPreview {
    InviteSummaryRow(
        invite = data,
        onAcceptClicked = {},
        onDeclineClicked = {},
    )
}
