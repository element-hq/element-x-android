/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.invite.api.InviteData
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewDescriptionAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewSubtitleAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewTitleAtom
import io.element.android.libraries.designsystem.atomic.molecules.ButtonRowMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitlePlaceholdersRowMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.molecules.MembersCountMolecule
import io.element.android.libraries.designsystem.atomic.organisms.RoomPreviewOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.SuperButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.RetryDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.theme.placeholderBackground
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.spaces.SpaceRoomVisibility
import io.element.android.libraries.matrix.ui.components.SpaceInfoRow
import io.element.android.libraries.matrix.ui.components.SpaceMembersView
import io.element.android.libraries.matrix.ui.model.InviteSender
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun JoinRoomView(
    state: JoinRoomState,
    onBackClick: () -> Unit,
    onJoinSuccess: () -> Unit,
    onKnockSuccess: () -> Unit,
    onForgetSuccess: () -> Unit,
    onCancelKnockSuccess: () -> Unit,
    onDeclineInviteAndBlockUser: (InviteData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        HeaderFooterPage(
            containerColor = Color.Transparent,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 24.dp
            ),
            topBar = {
                JoinRoomTopBar(
                    contentState = state.contentState,
                    hideAvatarImage = state.hideAvatarsImages,
                    onBackClick = onBackClick,
                )
            },
            content = {
                JoinRoomContent(
                    roomIdOrAlias = state.roomIdOrAlias,
                    contentState = state.contentState,
                    knockMessage = state.knockMessage,
                    hideAvatarsImages = state.hideAvatarsImages,
                    onKnockMessageUpdate = { state.eventSink(JoinRoomEvents.UpdateKnockMessage(it)) },
                )
            },
            footer = {
                JoinRoomFooter(
                    joinAuthorisationStatus = state.joinAuthorisationStatus,
                    onAcceptInvite = { inviteData ->
                        state.eventSink(JoinRoomEvents.AcceptInvite(inviteData))
                    },
                    onDeclineInvite = { inviteData, blockUser ->
                        if (state.canReportRoom && blockUser) {
                            onDeclineInviteAndBlockUser(inviteData)
                        } else {
                            state.eventSink(JoinRoomEvents.DeclineInvite(inviteData, blockUser = blockUser))
                        }
                    },
                    onJoinRoom = {
                        state.eventSink(JoinRoomEvents.JoinRoom)
                    },
                    onKnockRoom = {
                        state.eventSink(JoinRoomEvents.KnockRoom)
                    },
                    onCancelKnock = {
                        state.eventSink(JoinRoomEvents.CancelKnock(requiresConfirmation = true))
                    },
                    onForgetRoom = {
                        state.eventSink(JoinRoomEvents.ForgetRoom)
                    },
                    onGoBack = onBackClick,
                )
            }
        )
    }
    if (state.contentState is ContentState.Failure) {
        RetryDialog(
            title = stringResource(R.string.screen_join_room_loading_alert_title),
            content = stringResource(CommonStrings.error_network_or_server_issue),
            onRetry = { state.eventSink(JoinRoomEvents.RetryFetchingContent) },
            onDismiss = {
                state.eventSink(JoinRoomEvents.DismissErrorAndHideContent)
                onBackClick()
            }
        )
    }
    // This particular error is shown directly in the footer
    if (!state.isJoinActionUnauthorized) {
        AsyncActionView(
            async = state.joinAction,
            errorTitle = { stringResource(CommonStrings.common_something_went_wrong) },
            errorMessage = { stringResource(CommonStrings.error_network_or_server_issue) },
            onSuccess = { onJoinSuccess() },
            onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
        )
    }
    AsyncActionView(
        async = state.knockAction,
        errorTitle = { stringResource(CommonStrings.common_something_went_wrong) },
        errorMessage = { stringResource(CommonStrings.error_network_or_server_issue) },
        onSuccess = { onKnockSuccess() },
        onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
    )
    AsyncActionView(
        async = state.forgetAction,
        errorTitle = { stringResource(CommonStrings.common_something_went_wrong) },
        errorMessage = { stringResource(CommonStrings.error_network_or_server_issue) },
        onSuccess = { onForgetSuccess() },
        onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
    )
    AsyncActionView(
        async = state.cancelKnockAction,
        onSuccess = { onCancelKnockSuccess() },
        onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
        errorTitle = { stringResource(CommonStrings.common_something_went_wrong) },
        errorMessage = { stringResource(CommonStrings.error_network_or_server_issue) },
        confirmationDialog = {
            ConfirmationDialog(
                content = stringResource(R.string.screen_join_room_cancel_knock_alert_description),
                title = stringResource(R.string.screen_join_room_cancel_knock_alert_title),
                submitText = stringResource(R.string.screen_join_room_cancel_knock_alert_confirmation),
                cancelText = stringResource(CommonStrings.action_no),
                onSubmitClick = { state.eventSink(JoinRoomEvents.CancelKnock(requiresConfirmation = false)) },
                onDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
            )
        },
    )
}

@Composable
private fun JoinRoomFooter(
    joinAuthorisationStatus: JoinAuthorisationStatus,
    onAcceptInvite: (InviteData) -> Unit,
    onDeclineInvite: (InviteData, Boolean) -> Unit,
    onJoinRoom: () -> Unit,
    onKnockRoom: () -> Unit,
    onCancelKnock: () -> Unit,
    onForgetRoom: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        when (joinAuthorisationStatus) {
            is JoinAuthorisationStatus.IsInvited -> {
                Column {
                    ButtonRowMolecule(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        OutlinedButton(
                            text = stringResource(CommonStrings.action_decline),
                            onClick = { onDeclineInvite(joinAuthorisationStatus.inviteData, false) },
                            modifier = Modifier.weight(1f),
                            size = ButtonSize.LargeLowPadding,
                            leadingIcon = IconSource.Vector(CompoundIcons.Close())
                        )
                        Button(
                            text = stringResource(CommonStrings.action_accept),
                            onClick = { onAcceptInvite(joinAuthorisationStatus.inviteData) },
                            modifier = Modifier.weight(1f),
                            size = ButtonSize.LargeLowPadding,
                            leadingIcon = IconSource.Vector(CompoundIcons.Check())
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        text = stringResource(R.string.screen_join_room_decline_and_block_button_title),
                        onClick = { onDeclineInvite(joinAuthorisationStatus.inviteData, true) },
                        modifier = Modifier.fillMaxWidth(),
                        destructive = true
                    )
                }
            }
            JoinAuthorisationStatus.CanJoin -> {
                SuperButton(
                    onClick = onJoinRoom,
                    modifier = Modifier.fillMaxWidth(),
                    buttonSize = ButtonSize.Large,
                ) {
                    Text(
                        text = stringResource(R.string.screen_join_room_join_action),
                    )
                }
            }
            JoinAuthorisationStatus.CanKnock -> {
                SuperButton(
                    onClick = onKnockRoom,
                    modifier = Modifier.fillMaxWidth(),
                    buttonSize = ButtonSize.Large,
                ) {
                    Text(
                        text = stringResource(R.string.screen_join_room_knock_action),
                    )
                }
            }
            JoinAuthorisationStatus.IsKnocked -> {
                OutlinedButton(
                    text = stringResource(R.string.screen_join_room_cancel_knock_action),
                    onClick = onCancelKnock,
                    modifier = Modifier.fillMaxWidth(),
                    size = ButtonSize.Large,
                )
            }
            JoinAuthorisationStatus.NeedInvite -> {
                Announcement(
                    title = stringResource(R.string.screen_join_room_invite_required_message),
                    description = null,
                    type = AnnouncementType.Informative(isCritical = false),
                )
            }
            is JoinAuthorisationStatus.IsBanned -> JoinBannedFooter(joinAuthorisationStatus, onForgetRoom)
            JoinAuthorisationStatus.Unknown -> JoinRestrictedFooter(onJoinRoom)
            JoinAuthorisationStatus.Restricted -> JoinRestrictedFooter(onJoinRoom)
            JoinAuthorisationStatus.Unauthorized -> JoinUnauthorizedFooter(onGoBack)
            JoinAuthorisationStatus.None -> Unit
        }
    }
}

@Composable
private fun JoinUnauthorizedFooter(
    onOkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Announcement(
            title = stringResource(R.string.screen_join_room_fail_message),
            description = stringResource(R.string.screen_join_room_fail_reason),
            type = AnnouncementType.Informative(isCritical = true),
        )
        Spacer(Modifier.height(24.dp))
        Button(
            text = stringResource(CommonStrings.action_ok),
            onClick = onOkClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun JoinBannedFooter(
    status: JoinAuthorisationStatus.IsBanned,
    onForgetRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val banReason = status.reason?.let {
            stringResource(R.string.screen_join_room_ban_reason, it.removeSuffix("."))
        }
        val title = if (status.banSender != null) {
            stringResource(R.string.screen_join_room_ban_by_message, status.banSender.displayName)
        } else {
            stringResource(R.string.screen_join_room_ban_message)
        }
        Announcement(
            title = title,
            description = banReason,
            type = AnnouncementType.Informative(isCritical = true),
        )
        Spacer(Modifier.height(24.dp))
        Button(
            text = stringResource(R.string.screen_join_room_forget_action),
            onClick = onForgetRoom,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
    }
}

@Composable
private fun JoinRestrictedFooter(
    onJoinRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Announcement(
            title = stringResource(R.string.screen_join_room_join_restricted_message),
            description = null,
            type = AnnouncementType.Informative(),
        )
        Spacer(Modifier.height(24.dp))
        SuperButton(
            onClick = onJoinRoom,
            modifier = Modifier.fillMaxWidth(),
            buttonSize = ButtonSize.Large,
        ) {
            Text(
                text = stringResource(R.string.screen_join_room_join_action),
            )
        }
    }
}

@Composable
private fun JoinRoomContent(
    roomIdOrAlias: RoomIdOrAlias,
    contentState: ContentState,
    knockMessage: String,
    hideAvatarsImages: Boolean,
    onKnockMessageUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (contentState) {
            is ContentState.Loaded -> {
                when (contentState.joinAuthorisationStatus) {
                    is JoinAuthorisationStatus.IsKnocked -> {
                        IsKnockedLoadedContent()
                    }
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            DefaultLoadedContent(
                                contentState = contentState,
                                hideAvatarImage = hideAvatarsImages,
                            )
                            when (contentState.joinAuthorisationStatus) {
                                is JoinAuthorisationStatus.IsInvited -> {
                                    val inviteSender = contentState.joinAuthorisationStatus.inviteSender
                                    if (inviteSender != null) {
                                        Spacer(Modifier.height(16.dp))
                                        InvitedByView(inviteSender, hideAvatarsImages)
                                    }
                                }
                                is JoinAuthorisationStatus.CanKnock -> {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    val supportingText = if (knockMessage.isNotEmpty()) {
                                        "${knockMessage.length}/$MAX_KNOCK_MESSAGE_LENGTH"
                                    } else {
                                        stringResource(R.string.screen_join_room_knock_message_description)
                                    }
                                    TextField(
                                        value = knockMessage,
                                        onValueChange = onKnockMessageUpdate,
                                        maxLines = 3,
                                        minLines = 3,
                                        modifier = Modifier.fillMaxWidth(),
                                        supportingText = supportingText
                                    )
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
            is ContentState.UnknownRoom -> UnknownRoomContent()
            is ContentState.Loading -> IncompleteContent(roomIdOrAlias, isLoading = true)
            is ContentState.Dismissing -> IncompleteContent(roomIdOrAlias, isLoading = false)
            is ContentState.Failure -> IncompleteContent(roomIdOrAlias, isLoading = false)
        }
    }
}

@Composable
private fun InvitedByView(
    sender: InviteSender,
    hideAvatarImage: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.screen_join_room_invited_by),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary
        )
        Spacer(Modifier.height(8.dp))
        Avatar(
            avatarData = sender.avatarData,
            avatarType = AvatarType.User,
            hideImage = hideAvatarImage,
            forcedAvatarSize = AvatarSize.RoomPreviewInviter.dp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = sender.displayName,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = sender.userId.value,
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary
        )
    }
}

@Composable
private fun UnknownRoomContent(
    modifier: Modifier = Modifier
) {
    RoomPreviewOrganism(
        modifier = modifier,
        avatar = {
            Box(
                modifier = Modifier
                    .size(AvatarSize.RoomPreviewHeader.dp)
                    .background(
                        color = ElementTheme.colors.placeholderBackground,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    tint = ElementTheme.colors.iconPrimary,
                    imageVector = CompoundIcons.VisibilityOff(),
                    contentDescription = null,
                )
            }
        },
        title = {
            RoomPreviewTitleAtom(stringResource(R.string.screen_join_room_title_no_preview))
        },
        subtitle = {
        },
    )
}

@Composable
private fun IncompleteContent(
    roomIdOrAlias: RoomIdOrAlias,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    RoomPreviewOrganism(
        modifier = modifier,
        avatar = {
            PlaceholderAtom(width = AvatarSize.RoomPreviewHeader.dp, height = AvatarSize.RoomPreviewHeader.dp)
        },
        title = {
            when (roomIdOrAlias) {
                is RoomIdOrAlias.Alias -> {
                    RoomPreviewSubtitleAtom(roomIdOrAlias.identifier)
                }
                is RoomIdOrAlias.Id -> {
                    PlaceholderAtom(width = 200.dp, height = 22.dp)
                }
            }
        },
        subtitle = {
            if (isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }
        },
    )
}

@Composable
private fun IsKnockedLoadedContent(modifier: Modifier = Modifier) {
    IconTitleSubtitleMolecule(
        modifier = modifier.padding(horizontal = 8.dp),
        iconStyle = BigIcon.Style.SuccessSolid,
        title = stringResource(R.string.screen_join_room_knock_sent_title),
        subTitle = stringResource(R.string.screen_join_room_knock_sent_description),
    )
}

@Composable
private fun DefaultLoadedContent(
    contentState: ContentState.Loaded,
    hideAvatarImage: Boolean,
    modifier: Modifier = Modifier,
) {
    RoomPreviewOrganism(
        modifier = modifier,
        avatar = {
            Avatar(
                contentState.avatarData(AvatarSize.RoomPreviewHeader),
                hideImage = hideAvatarImage,
                avatarType = if (contentState.isSpace) AvatarType.Space() else AvatarType.Room(),
            )
        },
        title = {
            if (contentState.name != null) {
                RoomPreviewTitleAtom(title = contentState.name)
            } else {
                RoomPreviewTitleAtom(
                    title = stringResource(id = CommonStrings.common_no_room_name),
                    fontStyle = FontStyle.Italic
                )
            }
        },
        subtitle = {
            when {
                contentState.details is LoadedDetails.Space -> {
                    SpaceInfoRow(visibility = SpaceRoomVisibility.fromJoinRule(contentState.joinRule))
                }
                contentState.alias != null -> {
                    RoomPreviewSubtitleAtom(contentState.alias.value)
                }
            }
        },
        description = {
            RoomPreviewDescriptionAtom(
                contentState.topic ?: "",
                maxLines = if (contentState.joinAuthorisationStatus is JoinAuthorisationStatus.CanJoin) Int.MAX_VALUE else 2
            )
        },
        memberCount = {
            if (contentState.showMemberCount) {
                val membersCount = contentState.numberOfMembers?.toInt() ?: 0
                if (contentState.isSpace) {
                    SpaceMembersView(persistentListOf(), membersCount)
                } else {
                    MembersCountMolecule(memberCount = membersCount)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinRoomTopBar(
    contentState: ContentState,
    hideAvatarImage: Boolean,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            if (contentState is ContentState.Loaded && contentState.joinAuthorisationStatus is JoinAuthorisationStatus.IsKnocked) {
                val roundedCornerShape = RoundedCornerShape(8.dp)
                val titleModifier = Modifier
                    .clip(roundedCornerShape)
                if (contentState.name != null) {
                    Row(
                        modifier = titleModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(
                            avatarData = contentState.avatarData(AvatarSize.TimelineRoom),
                            hideImage = hideAvatarImage,
                            avatarType = AvatarType.Room(),
                        )
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .semantics {
                                    heading()
                                },
                            text = contentState.name,
                            style = ElementTheme.typography.fontBodyLgMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    IconTitlePlaceholdersRowMolecule(
                        iconSize = AvatarSize.TimelineRoom.dp,
                        modifier = titleModifier
                    )
                }
            }
        },
    )
}

@PreviewsDayNight
@Composable
internal fun JoinRoomViewPreview(@PreviewParameter(JoinRoomStateProvider::class) state: JoinRoomState) = ElementPreview {
    JoinRoomView(
        state = state,
        onBackClick = { },
        onJoinSuccess = { },
        onKnockSuccess = { },
        onForgetSuccess = { },
        onCancelKnockSuccess = { },
        onDeclineInviteAndBlockUser = { },
    )
}
