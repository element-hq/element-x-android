/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.moderation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncIndicator
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.getBestName
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun RoomMembersModerationView(
    state: RoomMembersModerationState,
    onDisplayMemberProfile: (UserId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (state.selectedRoomMember != null && state.actions.isNotEmpty()) {
            RoomMemberActionsBottomSheet(
                roomMember = state.selectedRoomMember,
                actions = state.actions,
                onSelectAction = { action ->
                    when (action) {
                        is ModerationAction.DisplayProfile -> {
                            onDisplayMemberProfile(action.userId)
                        }
                        is ModerationAction.KickUser -> {
                            state.eventSink(RoomMembersModerationEvents.KickUser)
                        }
                        is ModerationAction.BanUser -> {
                            state.eventSink(RoomMembersModerationEvents.BanUser)
                        }
                    }
                },
                onDismiss = { state.eventSink(RoomMembersModerationEvents.Reset) },
            )
        }

        val asyncIndicatorState = rememberAsyncIndicatorState()
        AsyncIndicatorHost(modifier = Modifier.statusBarsPadding(), state = asyncIndicatorState)

        when (val action = state.kickUserAsyncAction) {
            is AsyncAction.Loading -> {
                LaunchedEffect(action) {
                    val userDisplayName = state.selectedRoomMember?.getBestName().orEmpty()
                    asyncIndicatorState.enqueue {
                        AsyncIndicator.Loading(text = stringResource(R.string.screen_room_member_list_removing_user, userDisplayName))
                    }
                }
            }
            is AsyncAction.Failure -> {
                Timber.e(action.error, "Failed to kick user.")
                LaunchedEffect(action) {
                    asyncIndicatorState.enqueue(AsyncIndicator.DURATION_SHORT) {
                        AsyncIndicator.Failure(
                            text = stringResource(CommonStrings.common_failed),
                        )
                    }
                }
            }
            is AsyncAction.Success -> {
                LaunchedEffect(action) { asyncIndicatorState.clear() }
            }
            else -> Unit
        }

        when (val action = state.banUserAsyncAction) {
            is AsyncAction.Confirming -> {
                ConfirmationDialog(
                    title = stringResource(R.string.screen_room_member_list_ban_member_confirmation_title),
                    content = stringResource(R.string.screen_room_member_list_ban_member_confirmation_description),
                    submitText = stringResource(R.string.screen_room_member_list_ban_member_confirmation_action),
                    onSubmitClick = { state.eventSink(RoomMembersModerationEvents.BanUser) },
                    onDismiss = { state.eventSink(RoomMembersModerationEvents.Reset) }
                )
            }
            is AsyncAction.Loading -> {
                LaunchedEffect(action) {
                    val userDisplayName = state.selectedRoomMember?.getBestName().orEmpty()
                    asyncIndicatorState.enqueue {
                        AsyncIndicator.Loading(text = stringResource(R.string.screen_room_member_list_banning_user, userDisplayName))
                    }
                }
            }
            is AsyncAction.Failure -> {
                Timber.e(action.error, "Failed to ban user.")
                LaunchedEffect(action) {
                    asyncIndicatorState.enqueue(AsyncIndicator.DURATION_SHORT) {
                        AsyncIndicator.Failure(
                            text = stringResource(CommonStrings.common_failed),
                        )
                    }
                }
            }
            is AsyncAction.Success -> {
                LaunchedEffect(action) { asyncIndicatorState.clear() }
            }
            else -> Unit
        }

        when (val action = state.unbanUserAsyncAction) {
            is AsyncAction.Confirming -> {
                if (action is ConfirmingRoomMemberAction) {
                    ConfirmationDialog(
                        title = stringResource(R.string.screen_room_member_list_manage_member_unban_title),
                        content = stringResource(R.string.screen_room_member_list_manage_member_unban_message),
                        submitText = stringResource(R.string.screen_room_member_list_manage_member_unban_action),
                        onSubmitClick = {
                            val userDisplayName = action.roomMember.getBestName()
                            asyncIndicatorState.enqueue {
                                AsyncIndicator.Loading(text = stringResource(R.string.screen_room_member_list_unbanning_user, userDisplayName))
                            }
                            state.eventSink(RoomMembersModerationEvents.UnbanUser(action.roomMember.userId))
                        },
                        onDismiss = { state.eventSink(RoomMembersModerationEvents.Reset) },
                    )
                }
            }
            is AsyncAction.Failure -> {
                Timber.e(action.error, "Failed to unban user.")
                LaunchedEffect(action) {
                    asyncIndicatorState.enqueue(AsyncIndicator.DURATION_SHORT) {
                        AsyncIndicator.Failure(
                            text = stringResource(CommonStrings.common_failed),
                        )
                    }
                }
            }
            is AsyncAction.Success -> {
                LaunchedEffect(action) { asyncIndicatorState.clear() }
            }
            is AsyncAction.Loading,
            AsyncAction.Uninitialized -> Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomMemberActionsBottomSheet(
    roomMember: RoomMember,
    actions: ImmutableList<ModerationAction>,
    onSelectAction: (ModerationAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        modifier = Modifier.systemBarsPadding(),
        sheetState = bottomSheetState,
        onDismissRequest = {
            coroutineScope.launch {
                bottomSheetState.hide()
                onDismiss()
            }
        },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Avatar(
                avatarData = roomMember.getAvatarData(size = AvatarSize.RoomListManageUser),
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .align(Alignment.CenterHorizontally)
            )
            roomMember.displayName?.let {
                Text(
                    text = it,
                    style = ElementTheme.typography.fontHeadingLgBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .fillMaxWidth()
                )
            }
            Text(
                text = roomMember.userId.toString(),
                style = ElementTheme.typography.fontBodyLgRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            for (action in actions) {
                when (action) {
                    is ModerationAction.DisplayProfile -> {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.screen_room_member_list_manage_member_user_info)) },
                            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Info())),
                            onClick = {
                                coroutineScope.launch {
                                    onSelectAction(action)
                                    bottomSheetState.hide()
                                }
                            }
                        )
                    }
                    is ModerationAction.KickUser -> {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.screen_room_member_list_manage_member_remove)) },
                            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
                            onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    onSelectAction(action)
                                }
                            }
                        )
                    }
                    is ModerationAction.BanUser -> {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.screen_room_member_list_manage_member_remove_confirmation_ban)) },
                            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Block())),
                            style = ListItemStyle.Destructive,
                            onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.hide()
                                    onSelectAction(action)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomMembersModerationViewPreview(@PreviewParameter(RoomMembersModerationStateProvider::class) state: RoomMembersModerationState) {
    ElementPreview {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
        ) {
            RoomMembersModerationView(
                state = state,
                onDisplayMemberProfile = {},
            )
        }
    }
}
