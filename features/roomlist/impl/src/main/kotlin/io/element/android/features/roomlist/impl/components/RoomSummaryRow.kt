/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.RoomListEvents
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryProvider
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.designsystem.atomic.atoms.UnreadIndicatorAtom
import io.element.android.libraries.designsystem.components.avatar.CompositeAvatar
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.roomListRoomMessage
import io.element.android.libraries.designsystem.theme.roomListRoomMessageDate
import io.element.android.libraries.designsystem.theme.roomListRoomName
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.ui.components.InviteSenderView
import io.element.android.libraries.matrix.ui.model.InviteSender
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

internal val minHeight = 84.dp

@Composable
internal fun RoomSummaryRow(
    room: RoomListRoomSummary,
    onClick: (RoomListRoomSummary) -> Unit,
    eventSink: (RoomListEvents) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (room.displayType) {
            RoomSummaryDisplayType.PLACEHOLDER -> {
                RoomSummaryPlaceholderRow()
            }
            RoomSummaryDisplayType.INVITE -> {
                RoomSummaryScaffoldRow(
                    room = room,
                    onClick = onClick,
                    onLongClick = {
                        Timber.d("Long click on invite room")
                    },
                ) {
                    InviteNameAndIndicatorRow(name = room.name)
                    InviteSubtitle(isDm = room.isDm, inviteSender = room.inviteSender, canonicalAlias = room.canonicalAlias)
                    if (!room.isDm && room.inviteSender != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        InviteSenderView(
                            modifier = Modifier.fillMaxWidth(),
                            inviteSender = room.inviteSender,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    InviteButtonsRow(
                        onAcceptClick = {
                            eventSink(RoomListEvents.AcceptInvite(room))
                        },
                        onDeclineClick = {
                            eventSink(RoomListEvents.DeclineInvite(room))
                        }
                    )
                }
            }
            RoomSummaryDisplayType.ROOM -> {
                RoomSummaryScaffoldRow(
                    room = room,
                    onClick = onClick,
                    onLongClick = {
                        eventSink(RoomListEvents.ShowContextMenu(room))
                    },
                ) {
                    NameAndTimestampRow(
                        name = room.name,
                        timestamp = room.timestamp,
                        isHighlighted = room.isHighlighted
                    )
                    LastMessageAndIndicatorRow(room = room)
                }
            }
            RoomSummaryDisplayType.KNOCKED -> {
                RoomSummaryScaffoldRow(
                    room = room,
                    onClick = onClick,
                    onLongClick = {
                        Timber.d("Long click on knocked room")
                    },
                ) {
                    NameAndTimestampRow(
                        name = room.name,
                        timestamp = null,
                        isHighlighted = room.isHighlighted
                    )
                    if (room.canonicalAlias != null) {
                        Text(
                            text = room.canonicalAlias.value,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = ElementTheme.typography.fontBodyMdRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = stringResource(id = R.string.screen_join_room_knock_sent_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoomSummaryScaffoldRow(
    room: RoomListRoomSummary,
    onClick: (RoomListRoomSummary) -> Unit,
    onLongClick: (RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = Modifier.combinedClickable(
        onClick = { onClick(room) },
        onLongClick = { onLongClick(room) },
        indication = ripple(),
        interactionSource = remember { MutableInteractionSource() }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .then(clickModifier)
            .padding(horizontal = 16.dp, vertical = 11.dp)
            .height(IntrinsicSize.Min),
    ) {
        CompositeAvatar(
            avatarData = room.avatarData,
            heroes = room.heroes,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
private fun NameAndTimestampRow(
    name: String?,
    timestamp: String?,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(16.dp)
    ) {
        // Name
        Text(
            modifier = Modifier.weight(1f),
            style = ElementTheme.typography.fontBodyLgMedium,
            text = name ?: stringResource(id = CommonStrings.common_no_room_name),
            fontStyle = FontStyle.Italic.takeIf { name == null },
            color = MaterialTheme.roomListRoomName(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Timestamp
        Text(
            text = timestamp ?: "",
            style = ElementTheme.typography.fontBodySmMedium,
            color = if (isHighlighted) {
                ElementTheme.colors.unreadIndicator
            } else {
                MaterialTheme.roomListRoomMessageDate()
            },
        )
    }
}

@Composable
private fun InviteSubtitle(
    isDm: Boolean,
    inviteSender: InviteSender?,
    canonicalAlias: RoomAlias?,
    modifier: Modifier = Modifier
) {
    val subtitle = if (isDm) {
        inviteSender?.userId?.value
    } else {
        canonicalAlias?.value
    }
    if (subtitle != null) {
        Text(
            text = subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ElementTheme.typography.fontBodyMdRegular,
            color = MaterialTheme.roomListRoomMessage(),
            modifier = modifier,
        )
    }
}

@Composable
private fun LastMessageAndIndicatorRow(
    room: RoomListRoomSummary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(28.dp)
    ) {
        // Last Message
        val attributedLastMessage = room.lastMessage as? AnnotatedString
            ?: AnnotatedString(room.lastMessage.orEmpty().toString())
        Text(
            modifier = Modifier.weight(1f),
            text = attributedLastMessage,
            color = MaterialTheme.roomListRoomMessage(),
            style = ElementTheme.typography.fontBodyMdRegular,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        // Call and unread
        Row(
            modifier = Modifier.height(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val tint = if (room.isHighlighted) ElementTheme.colors.unreadIndicator else ElementTheme.colors.iconQuaternary
            if (room.hasRoomCall) {
                OnGoingCallIcon(
                    color = tint,
                )
            }
            if (room.userDefinedNotificationMode == RoomNotificationMode.MUTE) {
                NotificationOffIndicatorAtom()
            } else if (room.numberOfUnreadMentions > 0) {
                MentionIndicatorAtom()
            }
            if (room.hasNewContent) {
                UnreadIndicatorAtom(
                    color = tint
                )
            }
        }
    }
}

@Composable
private fun InviteNameAndIndicatorRow(
    name: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            style = ElementTheme.typography.fontBodyLgMedium,
            text = name ?: stringResource(id = CommonStrings.common_no_room_name),
            fontStyle = FontStyle.Italic.takeIf { name == null },
            color = MaterialTheme.roomListRoomName(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        UnreadIndicatorAtom(
            color = ElementTheme.colors.unreadIndicator
        )
    }
}

@Composable
private fun InviteButtonsRow(
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = spacedBy(12.dp)
    ) {
        OutlinedButton(
            text = stringResource(CommonStrings.action_decline),
            onClick = onDeclineClick,
            size = ButtonSize.MediumLowPadding,
            modifier = Modifier.weight(1f),
        )
        Button(
            text = stringResource(CommonStrings.action_accept),
            onClick = onAcceptClick,
            size = ButtonSize.MediumLowPadding,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OnGoingCallIcon(
    color: Color,
) {
    Icon(
        modifier = Modifier.size(16.dp),
        imageVector = CompoundIcons.VideoCallSolid(),
        contentDescription = null,
        tint = color,
    )
}

@Composable
private fun NotificationOffIndicatorAtom() {
    Icon(
        modifier = Modifier.size(16.dp),
        contentDescription = null,
        imageVector = CompoundIcons.NotificationsOffSolid(),
        tint = ElementTheme.colors.iconQuaternary,
    )
}

@Composable
private fun MentionIndicatorAtom() {
    Icon(
        modifier = Modifier.size(16.dp),
        contentDescription = null,
        imageVector = CompoundIcons.Mention(),
        tint = ElementTheme.colors.unreadIndicator,
    )
}

@PreviewsDayNight
@Composable
internal fun RoomSummaryRowPreview(@PreviewParameter(RoomListRoomSummaryProvider::class) data: RoomListRoomSummary) = ElementPreview {
    RoomSummaryRow(
        room = data,
        onClick = {},
        eventSink = {},
    )
}
