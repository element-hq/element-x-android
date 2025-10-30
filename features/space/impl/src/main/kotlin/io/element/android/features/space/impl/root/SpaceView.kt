/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.molecules.InviteButtonsRowMolecule
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.SimpleModalBottomSheet
import io.element.android.libraries.designsystem.components.async.AsyncIndicator
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.components.JoinButton
import io.element.android.libraries.matrix.ui.components.SpaceHeaderView
import io.element.android.libraries.matrix.ui.components.SpaceRoomItemView
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceView(
    state: SpaceState,
    onBackClick: () -> Unit,
    onRoomClick: (spaceRoom: SpaceRoom) -> Unit,
    onShareSpace: () -> Unit,
    onLeaveSpaceClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onViewMembersClick: () -> Unit,
    modifier: Modifier = Modifier,
    acceptDeclineInviteView: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SpaceViewTopBar(
                currentSpace = state.currentSpace,
                onBackClick = onBackClick,
                onLeaveSpaceClick = onLeaveSpaceClick,
                onShareSpace = onShareSpace,
                onDetailsClick = onDetailsClick,
                onViewMembersClick = onViewMembersClick,
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                SpaceViewContent(
                    state = state,
                    onRoomClick = onRoomClick,
                    onTopicClick = { topic ->
                        state.eventSink(SpaceEvents.ShowTopicViewer(topic))
                    }
                )
                JoinRoomFailureEffect(
                    hasAnyFailure = state.hasAnyFailure,
                    eventSink = state.eventSink
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
private fun JoinRoomFailureEffect(
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
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        val currentSpace = state.currentSpace
        if (currentSpace != null) {
            item {
                SpaceHeaderView(
                    avatarData = currentSpace.getAvatarData(AvatarSize.SpaceHeader),
                    name = currentSpace.displayName,
                    topic = currentSpace.topic,
                    topicMaxLines = 2,
                    visibility = currentSpace.visibility,
                    heroes = currentSpace.heroes.toImmutableList(),
                    numberOfMembers = currentSpace.numJoinedMembers,
                    onTopicClick = onTopicClick
                )
            }
            item {
                HorizontalDivider()
            }
        }
        itemsIndexed(
            items = state.children,
            key = { _, spaceRoom -> spaceRoom.roomId }
        ) { index, spaceRoom ->
            val isInvitation = spaceRoom.state == CurrentUserMembership.INVITED
            val isCurrentlyJoining = state.isJoining(spaceRoom.roomId)
            SpaceRoomItemView(
                spaceRoom = spaceRoom,
                showUnreadIndicator = isInvitation && spaceRoom.roomId !in state.seenSpaceInvites,
                hideAvatars = isInvitation && state.hideInvitesAvatar,
                onClick = {
                    onRoomClick(spaceRoom)
                },
                onLongClick = {
                    // TODO
                },
                trailingAction = spaceRoom.trailingAction(isCurrentlyJoining = isCurrentlyJoining) {
                    state.eventSink(SpaceEvents.Join(spaceRoom))
                },
                bottomAction = spaceRoom.inviteButtons(
                    onAcceptClick = {
                        state.eventSink(SpaceEvents.AcceptInvite(spaceRoom))
                    },
                    onDeclineClick = {
                        state.eventSink(SpaceEvents.DeclineInvite(spaceRoom))
                    }
                )
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
    currentSpace: SpaceRoom?,
    onBackClick: () -> Unit,
    onLeaveSpaceClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onShareSpace: () -> Unit,
    onViewMembersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            if (currentSpace != null) {
                val roundedCornerShape = RoundedCornerShape(8.dp)
                SpaceAvatarAndNameRow(
                    name = currentSpace.displayName,
                    avatarData = currentSpace.getAvatarData(AvatarSize.TimelineRoom),
                    modifier = Modifier
                        .clip(roundedCornerShape)
                        // TODO enable when screen ready for space
                        .clickable(enabled = false, onClick = onDetailsClick)
                )
            }
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
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onShareSpace()
                    },
                    text = { Text(stringResource(id = CommonStrings.action_share)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.ShareAndroid(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onViewMembersClick()
                    },
                    text = { Text(stringResource(id = CommonStrings.screen_space_menu_action_members)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.User(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onLeaveSpaceClick()
                    },
                    text = {
                        Text(
                            text = stringResource(id = CommonStrings.action_leave),
                            color = ElementTheme.colors.textCriticalPrimary,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.Leave(),
                            tint = ElementTheme.colors.iconCriticalPrimary,
                            contentDescription = null,
                        )
                    }
                )
            }
        },
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
        onDetailsClick = {},
        onViewMembersClick = {},
        onBackClick = {},
    )
}
