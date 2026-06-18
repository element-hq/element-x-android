/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.components.receipt.aReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.RtcNotificationState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.libraries.dateformatter.api.toHumanReadableDuration
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarRow
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.USER_NAME_ALICE
import io.element.android.libraries.designsystem.preview.USER_NAME_BOB
import io.element.android.libraries.designsystem.preview.USER_NAME_CAROL
import io.element.android.libraries.designsystem.preview.USER_NAME_CHARLIE
import io.element.android.libraries.designsystem.preview.USER_NAME_DAVID
import io.element.android.libraries.designsystem.preview.USER_NAME_JOHN_DOE
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun ActiveCallTimelineItemView(
    timelineRoomInfo: TimelineRoomInfo,
    event: TimelineItem.Event,
    state: RtcNotificationState.Active,
    isLastOutgoingMessage: Boolean,
    onLongClick: (TimelineItem.Event) -> Unit,
    onReadReceiptsClick: (TimelineItem.Event) -> Unit,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)
            currentTime = System.currentTimeMillis()
        }
    }

    val elapsedMillis = (currentTime - state.callStartTsMillis).coerceAtLeast(0)
    val formattedDuration = elapsedMillis.toHumanReadableDuration()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                .border(1.dp, ElementTheme.colors.borderInteractivePrimary, RoundedCornerShape(8.dp))
                .combinedClickable(
                    enabled = true,
                    onClick = {},
                    onLongClick = { onLongClick(event) },
                    onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
                )
                .onKeyboardContextMenuAction { onLongClick(event) }
                .padding(start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 12.dp)
                    .size(40.dp)
                    .background(ElementTheme.colors.bgSubtleSecondary, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                imageVector = if (state.callIntent == CallIntent.AUDIO) CompoundIcons.VoiceCallSolid() else CompoundIcons.VideoCallSolid(),
                contentDescription = null,
                tint = ElementTheme.colors.iconPrimary,
            )

            if (timelineRoomInfo.isDm) {
                DirectMessageCallBody(state, Modifier.weight(1f))
            } else {
                GroupCallBody(
                    event,
                    state,
                    Modifier.weight(1f)
                        .padding(top = 12.dp, bottom = 12.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
            ) {
                if (!state.isJoined) {
                    Button(
                        text = "Join",
                        size = ButtonSize.Small,
                        onClick = { onJoinCallClick(state.callIntent == CallIntent.AUDIO) }
                    )
                }
                Text(
                    text = "($formattedDuration)",
                    style = ElementTheme.typography.fontBodyXsRegular,
                    color = ElementTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        TimelineItemReadReceiptView(
            state = ReadReceiptViewState(
                sendState = event.localSendState,
                isLastOutgoingMessage = isLastOutgoingMessage,
                receipts = event.readReceiptState.receipts,
            ),
            onReadReceiptsClick = { onReadReceiptsClick(event) },
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun DirectMessageCallBody(state: RtcNotificationState.Active, modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
    ) {
        if (state.joinedMembers.size == 1) {
            val caller = state.joinedMembers.first()
            Avatar(
                avatarData = AvatarData(
                    id = caller.userId.value,
                    name = caller.displayName,
                    url = caller.avatarUrl,
                    size = AvatarSize.ActiveCallItem,
                ),
                avatarType = AvatarType.User
            )
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = "${caller.displayName ?: caller.userId} started a call",
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textPrimary,
            )
        } else {
            AvatarRow(
                avatarDataList = state.joinedMembers
                    .map { user ->
                        AvatarData(
                            id = user.userId.value,
                            name = user.displayName,
                            url = user.avatarUrl,
                            size = AvatarSize.ActiveCallItem
                        )
                    }.toImmutableList(),
                avatarType = AvatarType.User,
                overlapRatio = 0.5f,
                lastOnTop = true,
            )
            Text(
                modifier = Modifier.padding(start = 6.dp),
                text = stringResource(CommonStrings.common_call_in_progress),
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textPrimary,
            )
        }
    }
}

@Composable
private fun GroupCallBody(event: TimelineItem.Event, state: RtcNotificationState.Active, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(CommonStrings.common_group_call_in_progress),
            style = ElementTheme.typography.fontBodyMdMedium,
            color = ElementTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.joinedMembers.size == 1) {
                val caller = state.joinedMembers.first()
                Avatar(
                    avatarData = AvatarData(
                        id = caller.userId.value,
                        name = caller.displayName,
                        url = caller.avatarUrl,
                        size = AvatarSize.ActiveCallItem,
                    ),
                    avatarType = AvatarType.User
                )
                // Only show started a call if the last remaining user in the call
                // is really the one who started the call
                if (caller.userId == event.senderId) {
                    Text(
                        modifier = Modifier.padding(start = 6.dp),
                        text = stringResource(CommonStrings.common_user_started_a_call, caller.displayName ?: caller.userId),
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            } else {
                val membersToShowCount = 4
                // Keep the one with avatar first but maintain
                // the first user as it might be the call creator
                val sortedMembers = remember(state.joinedMembers) {
                    if (state.joinedMembers.isEmpty()) return@remember emptyList()
                    val first = state.joinedMembers.first()
                    val rest = state.joinedMembers.drop(1).sortedByDescending { it.avatarUrl != null }
                    listOf(first) + rest
                }
                val displayMembers = sortedMembers.take(membersToShowCount)
                val extraCount = state.joinedMembers.size - membersToShowCount

                AvatarRow(
                    avatarDataList = displayMembers
                        .map { user ->
                            AvatarData(
                                id = user.userId.value,
                                name = user.displayName,
                                url = user.avatarUrl,
                                size = AvatarSize.ActiveCallItem
                            )
                        }.toImmutableList(),
                    avatarType = AvatarType.User,
                    overlapRatio = 0.5f,
                    lastOnTop = true,
                )
                if (extraCount > 0) {
                    Text(
                        text = pluralStringResource(CommonPlurals.screen_timeline_active_call_extra_joined_count, count = state.joinedMembers.size, extraCount),
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@PreviewWithLargeHeight
@Composable
internal fun ActiveCallTimelineItemViewPreview() = ElementPreview(
    drawableFallbackForImages = CommonDrawables.sample_avatar
) {
    val readReceiptState = mutableListOf(
        aTimelineItemReadReceipts(
            receipts = List(3) { aReadReceiptData(it) },
        )
    )
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        listOf(CallIntent.AUDIO, CallIntent.VIDEO).forEach { callIntent ->
            listOf(
                RtcNotificationState.Active(
                    listOf(aMatrixUser(displayName = USER_NAME_ALICE, avatarUrl = "anUrl")),
                    isJoined = true,
                    callStartTsMillis = System.currentTimeMillis() - 60_000,
                    callIntent = callIntent
                ) to true,
                RtcNotificationState.Active(
                    listOf(
                        aMatrixUser(displayName = USER_NAME_ALICE, avatarUrl = "anUrl"),
                        aMatrixUser(displayName = USER_NAME_BOB)
                    ),
                    isJoined = true,
                    callStartTsMillis = System.currentTimeMillis() - 60_000,
                    callIntent = callIntent
                ) to true,
                RtcNotificationState.Active(
                    listOf(
                        aMatrixUser(displayName = USER_NAME_ALICE),
                        aMatrixUser(displayName = USER_NAME_BOB, avatarUrl = "anUrlB"),
                        aMatrixUser(displayName = USER_NAME_CAROL),
                        aMatrixUser(displayName = USER_NAME_DAVID),
                        aMatrixUser(displayName = USER_NAME_CHARLIE),
                        aMatrixUser(displayName = USER_NAME_JOHN_DOE, avatarUrl = "anUrlC"),
                    ),
                    isJoined = true,
                    callStartTsMillis = System.currentTimeMillis(),
                    callIntent = callIntent
                ) to false,
                // Group call only one member still in call, but it is not the creator
                RtcNotificationState.Active(
                    listOf(
                        aMatrixUser(displayName = USER_NAME_ALICE, avatarUrl = "aliceURl"),
                    ),
                    isJoined = true,
                    callStartTsMillis = System.currentTimeMillis(),
                    callIntent = callIntent
                ) to false,
                // Group call only one member that is the call creator
                RtcNotificationState.Active(
                    listOf(
                        aMatrixUser(
                            id = "@senderId:domain",
                            displayName = "Sender"
                        ),
                    ),
                    isJoined = true,
                    callStartTsMillis = System.currentTimeMillis(),
                    callIntent = callIntent
                ) to false,
            ).forEach { (state, isDm) ->
                val content = TimelineItemRtcNotificationContent(callIntent, state)
                ActiveCallTimelineItemView(
                    timelineRoomInfo = aTimelineRoomInfo(isDm = isDm),
                    event = aTimelineItemEvent(
                        content = content,
                        // Only display read receipts for the first item
                        readReceiptState = readReceiptState.removeFirstOrNull() ?: aTimelineItemReadReceipts(),
                    ),
                    state,
                    isLastOutgoingMessage = false,
                    onLongClick = {},
                    onReadReceiptsClick = {},
                    onJoinCallClick = {},
                )
            }
        }
    }
}
