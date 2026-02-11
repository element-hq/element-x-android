/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.space.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.molecules.InviteButtonsRowMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.SimpleModalBottomSheet
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncIndicator
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomVisibility
import io.element.android.libraries.matrix.ui.components.JoinButton
import io.element.android.libraries.matrix.ui.components.SpaceHeaderView
import io.element.android.libraries.matrix.ui.components.SpaceRoomItemView
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceView(
    state: SpaceState,
    onBackClick: () -> Unit,
    onRoomClick: (spaceRoom: SpaceRoom) -> Unit,
    onShareSpace: () -> Unit,
    onLeaveSpaceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onViewMembersClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onAddRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
    acceptDeclineInviteView: @Composable () -> Unit,
) {
    var handledBack by remember { mutableStateOf(false) }
    BackHandler(enabled = !handledBack) {
        if (state.isManageMode) {
            state.eventSink(SpaceEvents.ExitManageMode)
        } else {
            handledBack = true
            onBackClick()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Box {
                AnimatedVisibility(
                    visible = state.isManageMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ManageModeTopBar(
                        selectedCount = state.selectedCount,
                        isRemoveButtonEnabled = state.isRemoveButtonEnabled,
                        onCancelClick = { state.eventSink(SpaceEvents.ExitManageMode) },
                        onRemoveClick = { state.eventSink(SpaceEvents.RemoveSelectedRooms) },
                    )
                }
                AnimatedVisibility(
                    visible = !state.isManageMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SpaceViewTopBar(
                        spaceInfo = state.spaceInfo,
                        canAccessSpaceSettings = state.canAccessSpaceSettings,
                        canEditSpaceGraph = state.canEditSpaceGraph,
                        showManageRoomsAction = state.showManageRoomsAction,
                        onBackClick = onBackClick,
                        onLeaveSpaceClick = onLeaveSpaceClick,
                        onSettingsClick = onSettingsClick,
                        onShareSpace = onShareSpace,
                        onViewMembersClick = onViewMembersClick,
                        onManageRoomsClick = { state.eventSink(SpaceEvents.EnterManageMode) },
                        onAddRoomClick = onAddRoomClick,
                        onCreateRoomClick = onCreateRoomClick,
                    )
                }
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                SpaceViewContent(
                    state = state,
                    onRoomClick = { spaceRoom ->
                        if (state.isManageMode) {
                            state.eventSink(SpaceEvents.ToggleRoomSelection(spaceRoom.roomId))
                        } else {
                            onRoomClick(spaceRoom)
                        }
                    },
                    onTopicClick = { topic ->
                        state.eventSink(SpaceEvents.ShowTopicViewer(topic))
                    },
                    onCreateRoomClick = onCreateRoomClick,
                    onAddRoomClick = onAddRoomClick,
                )
                JoinFailuresEffect(
                    hasAnyFailure = state.hasAnyJoinFailures,
                    eventSink = state.eventSink
                )
                RemoveRoomsActionView(
                    spaceDisplayName = state.spaceInfo.name ?: state.spaceInfo.id.value,
                    removeRoomsAction = state.removeRoomsAction,
                    selectedCount = state.selectedCount,
                    onConfirm = { state.eventSink(SpaceEvents.ConfirmRoomRemoval) },
                    onDismiss = { state.eventSink(SpaceEvents.ClearRemoveAction) },
                )
                acceptDeclineInviteView()
            }
        },
    )
    if (state.topicViewerState is TopicViewerState.Shown) {
        TopicViewerBottomSheet(
            topicViewerState = state.topicViewerState,
            onDismiss = {
                state.eventSink(SpaceEvents.HideTopicViewer)
            }
        )
    }
}

@Composable
private fun JoinFailuresEffect(
    hasAnyFailure: Boolean,
    eventSink: (SpaceEvents) -> Unit,
) {
    val asyncIndicatorState = rememberAsyncIndicatorState()
    val updatedEventSink by rememberUpdatedState(eventSink)
    AsyncIndicatorHost(modifier = Modifier, asyncIndicatorState)
    LaunchedEffect(hasAnyFailure) {
        if (hasAnyFailure) {
            asyncIndicatorState.enqueue {
                AsyncIndicator.Failure(text = stringResource(CommonStrings.common_something_went_wrong))
            }
            delay(AsyncIndicator.DURATION_SHORT)
            updatedEventSink(SpaceEvents.ClearFailures)
        } else {
            asyncIndicatorState.clear()
        }
    }
}

@Composable
private fun TopicViewerBottomSheet(
    topicViewerState: TopicViewerState.Shown,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SimpleModalBottomSheet(
        title = stringResource(CommonStrings.common_description),
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        ClickableLinkText(
            text = topicViewerState.topic,
            interactionSource = remember { MutableInteractionSource() },
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun SpaceViewContent(
    state: SpaceState,
    onRoomClick: (spaceRoom: SpaceRoom) -> Unit,
    onTopicClick: (String) -> Unit,
    onCreateRoomClick: () -> Unit,
    onAddRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        val spaceInfo = state.spaceInfo
        item(key = "space_header") {
            AnimatedVisibility(
                !state.isManageMode,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    SpaceHeaderView(
                        avatarData = spaceInfo.getAvatarData(AvatarSize.SpaceHeader),
                        alias = spaceInfo.canonicalAlias,
                        name = spaceInfo.name,
                        topic = spaceInfo.topic,
                        topicMaxLines = 2,
                        visibility = SpaceRoomVisibility.fromJoinRule(spaceInfo.joinRule),
                        heroes = spaceInfo.heroes,
                        numberOfMembers = spaceInfo.joinedMembersCount.toInt(),
                        onTopicClick = onTopicClick
                    )
                    HorizontalDivider()
                }
            }
        }

        if (state.children.isEmpty() && state.canEditSpaceGraph && !state.hasMoreToLoad) {
            item {
                EmptySpaceView(
                    onCreateRoomClick = onCreateRoomClick,
                    onAddRoomClick = onAddRoomClick,
                )
            }
        } else {
            itemsIndexed(
                items = state.children,
                key = { _, spaceRoom -> spaceRoom.roomId }
            ) { index, spaceRoom ->
                val isInvitation = spaceRoom.state == CurrentUserMembership.INVITED
                val isCurrentlyJoining = state.isJoining(spaceRoom.roomId)
                val isSelected = state.isSelected(spaceRoom.roomId)
                val showUnreadIndicator = isInvitation && spaceRoom.roomId !in state.seenSpaceInvites && !state.isManageMode
                SpaceRoomItemView(
                    spaceRoom = spaceRoom,
                    showUnreadIndicator = showUnreadIndicator,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {
                        onRoomClick(spaceRoom)
                    },
                    onLongClick = {
                        // TODO
                    },
                    trailingAction = if (state.isManageMode) {
                        {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                            )
                        }
                    } else {
                        spaceRoom.trailingAction(isCurrentlyJoining = isCurrentlyJoining) {
                            state.eventSink(SpaceEvents.Join(spaceRoom))
                        }
                    },
                    bottomAction = if (state.isManageMode) {
                        null
                    } else {
                        spaceRoom.inviteButtons(
                            onAcceptClick = {
                                state.eventSink(SpaceEvents.AcceptInvite(spaceRoom))
                            },
                            onDeclineClick = {
                                state.eventSink(SpaceEvents.DeclineInvite(spaceRoom))
                            }
                        )
                    }
                )
                if (index != state.children.lastIndex) {
                    HorizontalDivider()
                }
            }

            if (state.hasMoreToLoad) {
                item {
                    LoadingMoreIndicator(eventSink = state.eventSink)
                }
            }
        }
    }
}

@Composable
private fun EmptySpaceView(
    onCreateRoomClick: () -> Unit,
    onAddRoomClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 24.dp),
    ) {
        IconTitleSubtitleMolecule(
            title = stringResource(R.string.screen_space_empty_state_title),
            subTitle = null,
            iconStyle = BigIcon.Style.Default(vectorIcon = CompoundIcons.Room(), usePrimaryTint = true),
            modifier = Modifier.fillMaxWidth()
                .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
        )
        ButtonColumnMolecule(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Button(
                text = stringResource(CommonStrings.action_add_existing_rooms),
                leadingIcon = IconSource.Vector(CompoundIcons.Plus()),
                onClick = onAddRoomClick,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedButton(
                text = stringResource(CommonStrings.action_create_room),
                onClick = onCreateRoomClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LoadingMoreIndicator(
    eventSink: (SpaceEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        val latestEventSink by rememberUpdatedState(eventSink)
        LaunchedEffect(Unit) {
            latestEventSink(SpaceEvents.LoadMore)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceViewTopBar(
    spaceInfo: RoomInfo,
    canAccessSpaceSettings: Boolean,
    canEditSpaceGraph: Boolean,
    showManageRoomsAction: Boolean,
    onBackClick: () -> Unit,
    onLeaveSpaceClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShareSpace: () -> Unit,
    onViewMembersClick: () -> Unit,
    onManageRoomsClick: () -> Unit,
    onAddRoomClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            val roundedCornerShape = RoundedCornerShape(8.dp)
            SpaceAvatarAndNameRow(
                name = spaceInfo.name,
                avatarData = spaceInfo.getAvatarData(AvatarSize.TimelineRoom),
                modifier = Modifier
                    .clip(roundedCornerShape)
                    .clickable(enabled = canAccessSpaceSettings, onClick = onSettingsClick)
            )
        },
        actions = {
            var showMenu by remember { mutableStateOf(false) }
            IconButton(
                onClick = { showMenu = !showMenu }
            ) {
                Icon(
                    imageVector = CompoundIcons.OverflowVertical(),
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                if (canEditSpaceGraph) {
                    SpaceMenuItem(
                        titleRes = CommonStrings.action_create_room,
                        icon = CompoundIcons.Plus(),
                        onClick = {
                            showMenu = false
                            onCreateRoomClick()
                        }
                    )
                    SpaceMenuItem(
                        titleRes = CommonStrings.action_add_existing_rooms,
                        icon = CompoundIcons.Room(),
                        onClick = {
                            showMenu = false
                            onAddRoomClick()
                        }
                    )
                    if (showManageRoomsAction) {
                        SpaceMenuItem(
                            titleRes = CommonStrings.action_manage_rooms,
                            icon = CompoundIcons.Edit(),
                            onClick = {
                                showMenu = false
                                onManageRoomsClick()
                            }
                        )
                    }
                    HorizontalDivider()
                }
                SpaceMenuItem(
                    titleRes = R.string.screen_space_menu_action_members,
                    icon = CompoundIcons.User(),
                    onClick = {
                        showMenu = false
                        onViewMembersClick()
                    }
                )
                SpaceMenuItem(
                    titleRes = CommonStrings.action_share,
                    icon = CompoundIcons.ShareAndroid(),
                    onClick = {
                        showMenu = false
                        onShareSpace()
                    }
                )
                if (canAccessSpaceSettings) {
                    SpaceMenuItem(
                        titleRes = CommonStrings.common_settings,
                        icon = CompoundIcons.Settings(),
                        onClick = {
                            showMenu = false
                            onSettingsClick()
                        }
                    )
                }
                HorizontalDivider()
                SpaceMenuItem(
                    titleRes = CommonStrings.action_leave_space,
                    icon = CompoundIcons.Leave(),
                    isCritical = true,
                    onClick = {
                        showMenu = false
                        onLeaveSpaceClick()
                    }
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageModeTopBar(
    selectedCount: Int,
    isRemoveButtonEnabled: Boolean,
    onCancelClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(
                onClick = onCancelClick,
                imageVector = CompoundIcons.Close()
            )
        },
        title = {
            Text(
                text = pluralStringResource(CommonPlurals.common_selected_count, selectedCount, selectedCount),
                style = ElementTheme.typography.fontBodyLgMedium,
            )
        },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_remove),
                onClick = onRemoveClick,
                enabled = isRemoveButtonEnabled,
            )
        },
    )
}

@Composable
private fun SpaceMenuItem(
    @StringRes titleRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    isCritical: Boolean = false,
) {
    DropdownMenuItem(
        onClick = onClick,
        text = {
            Text(
                text = stringResource(titleRes),
                color = if (isCritical) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textPrimary,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                tint = if (isCritical) ElementTheme.colors.iconCriticalPrimary else ElementTheme.colors.iconSecondary,
                contentDescription = null,
            )
        }
    )
}

@Composable
private fun SpaceAvatarAndNameRow(
    name: String?,
    avatarData: AvatarData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.Space(),
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics {
                    heading()
                },
            text = name ?: stringResource(CommonStrings.common_no_space_name),
            style = ElementTheme.typography.fontBodyLgMedium,
            fontStyle = FontStyle.Italic.takeIf { name == null },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun SpaceRoom.trailingAction(
    isCurrentlyJoining: Boolean,
    onClick: () -> Unit
): @Composable (() -> Unit)? {
    return when (state) {
        null, CurrentUserMembership.LEFT -> {
            {
                JoinButton(
                    showProgress = isCurrentlyJoining,
                    onClick = onClick,
                )
            }
        }
        else -> null
    }
}

private fun SpaceRoom.inviteButtons(
    onAcceptClick: () -> Unit,
    onDeclineClick: () -> Unit,
): @Composable (() -> Unit)? {
    return when (state) {
        CurrentUserMembership.INVITED -> {
            @Composable {
                InviteButtonsRowMolecule(
                    onAcceptClick = onAcceptClick,
                    onDeclineClick = onDeclineClick,
                )
            }
        }
        else -> null
    }
}

@Composable
private fun RemoveRoomsActionView(
    spaceDisplayName: String,
    removeRoomsAction: AsyncAction<Unit>,
    selectedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AsyncActionView(
        async = removeRoomsAction,
        confirmationDialog = {
            ConfirmationDialog(
                title = pluralStringResource(R.plurals.screen_space_remove_rooms_confirmation_title, selectedCount, selectedCount, spaceDisplayName),
                content = stringResource(R.string.screen_space_remove_rooms_confirmation_content),
                submitText = stringResource(CommonStrings.action_remove),
                onSubmitClick = onConfirm,
                onDismiss = onDismiss,
                destructiveSubmit = true,
                icon = {
                    Icon(
                        imageVector = CompoundIcons.Error(),
                        tint = ElementTheme.colors.textCriticalPrimary,
                        contentDescription = null
                    )
                }
            )
        },
        onRetry = onConfirm,
        errorTitle = {
            stringResource(CommonStrings.common_something_went_wrong)
        },
        errorMessage = {
            stringResource(CommonStrings.error_network_or_server_issue)
        },
        onSuccess = { onDismiss() },
        onErrorDismiss = onDismiss,
    )
}

@PreviewsDayNight
@Composable
internal fun SpaceViewPreview(
    @PreviewParameter(SpaceStateProvider::class) state: SpaceState
) = ElementPreview {
    SpaceView(
        state = state,
        onRoomClick = {},
        onShareSpace = {},
        onLeaveSpaceClick = {},
        acceptDeclineInviteView = {},
        onSettingsClick = {},
        onViewMembersClick = {},
        onCreateRoomClick = {},
        onAddRoomClick = {},
        onBackClick = {},
    )
}
