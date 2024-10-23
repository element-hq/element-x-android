/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewDescriptionAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewSubtitleAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewTitleAtom
import io.element.android.libraries.designsystem.atomic.molecules.ButtonRowMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitlePlaceholdersRowMolecule
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.molecules.RoomPreviewMembersCountMolecule
import io.element.android.libraries.designsystem.atomic.organisms.RoomPreviewOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.background.LightGradientBackground
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.SuperButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.ui.components.InviteSenderView
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun JoinRoomView(
    state: JoinRoomState,
    onBackClick: () -> Unit,
    onJoinSuccess: () -> Unit,
    onKnockSuccess: () -> Unit,
    onCancelKnockSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LightGradientBackground()
        HeaderFooterPage(
            containerColor = Color.Transparent,
            paddingValues = PaddingValues(16.dp),
            topBar = {
                JoinRoomTopBar(contentState = state.contentState, onBackClick = onBackClick)
            },
            content = {
                JoinRoomContent(
                    contentState = state.contentState,
                    applicationName = state.applicationName,
                    knockMessage = state.knockMessage,
                    onKnockMessageUpdate = { state.eventSink(JoinRoomEvents.UpdateKnockMessage(it)) },
                )
            },
            footer = {
                JoinRoomFooter(
                    state = state,
                    onAcceptInvite = {
                        state.eventSink(JoinRoomEvents.AcceptInvite)
                    },
                    onDeclineInvite = {
                        state.eventSink(JoinRoomEvents.DeclineInvite)
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
                    onRetry = {
                        state.eventSink(JoinRoomEvents.RetryFetchingContent)
                    },
                    onGoBack = onBackClick,
                )
            }
        )
    }
    AsyncActionView(
        async = state.joinAction,
        onSuccess = { onJoinSuccess() },
        onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
    )
    AsyncActionView(
        async = state.knockAction,
        onSuccess = { onKnockSuccess() },
        onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
    )
    AsyncActionView(
        async = state.cancelKnockAction,
        onSuccess = { onCancelKnockSuccess() },
        onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearActionStates) },
        errorMessage = {
            stringResource(CommonStrings.error_unknown)
        },
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
    state: JoinRoomState,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit,
    onJoinRoom: () -> Unit,
    onKnockRoom: () -> Unit,
    onCancelKnock: () -> Unit,
    onRetry: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
    ) {
        if (state.contentState is ContentState.Failure) {
            Button(
                text = stringResource(CommonStrings.action_retry),
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                size = ButtonSize.Large,
            )
        } else if (state.contentState is ContentState.Loaded && state.contentState.roomType == RoomType.Space) {
            Button(
                text = stringResource(CommonStrings.action_go_back),
                onClick = onGoBack,
                modifier = Modifier.fillMaxWidth(),
                size = ButtonSize.Large,
            )
        } else {
            val joinAuthorisationStatus = state.joinAuthorisationStatus
            when (joinAuthorisationStatus) {
                is JoinAuthorisationStatus.IsInvited -> {
                    ButtonRowMolecule(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        OutlinedButton(
                            text = stringResource(CommonStrings.action_decline),
                            onClick = onDeclineInvite,
                            modifier = Modifier.weight(1f),
                            size = ButtonSize.LargeLowPadding,
                        )
                        Button(
                            text = stringResource(CommonStrings.action_accept),
                            onClick = onAcceptInvite,
                            modifier = Modifier.weight(1f),
                            size = ButtonSize.LargeLowPadding,
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
                JoinAuthorisationStatus.Unknown -> Unit
            }
        }
    }
}

@Composable
private fun JoinRoomContent(
    contentState: ContentState,
    applicationName: String,
    knockMessage: String,
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
                        DefaultLoadedContent(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            contentState = contentState,
                            applicationName = applicationName,
                            knockMessage = knockMessage,
                            onKnockMessageUpdate = onKnockMessageUpdate
                        )
                    }
                }
            }
            is ContentState.UnknownRoom -> {
                RoomPreviewOrganism(
                    avatar = {
                        PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
                    },
                    title = {
                        RoomPreviewTitleAtom(stringResource(R.string.screen_join_room_title_no_preview))
                    },
                    subtitle = {
                        RoomPreviewSubtitleAtom(stringResource(R.string.screen_join_room_subtitle_no_preview))
                    },
                )
            }
            is ContentState.Loading -> {
                RoomPreviewOrganism(
                    avatar = {
                        PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
                    },
                    title = {
                        PlaceholderAtom(width = 200.dp, height = 22.dp)
                    },
                    subtitle = {
                        PlaceholderAtom(width = 140.dp, height = 20.dp)
                    },
                )
            }
            is ContentState.Failure -> {
                RoomPreviewOrganism(
                    avatar = {
                        PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
                    },
                    title = {
                        when (contentState.roomIdOrAlias) {
                            is RoomIdOrAlias.Alias -> {
                                RoomPreviewTitleAtom(contentState.roomIdOrAlias.identifier)
                            }
                            is RoomIdOrAlias.Id -> {
                                PlaceholderAtom(width = 200.dp, height = 22.dp)
                            }
                        }
                    },
                    subtitle = {
                        Text(
                            text = stringResource(id = CommonStrings.error_unknown),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun IsKnockedLoadedContent(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconTitleSubtitleMolecule(
            modifier = Modifier.sizeIn(minHeight = maxHeight * 0.7f),
            iconStyle = BigIcon.Style.SuccessSolid,
            title = stringResource(R.string.screen_join_room_knock_sent_title),
            subTitle = stringResource(R.string.screen_join_room_knock_sent_description),
        )
    }
}

@Composable
private fun DefaultLoadedContent(
    contentState: ContentState.Loaded,
    applicationName: String,
    knockMessage: String,
    onKnockMessageUpdate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    RoomPreviewOrganism(
        modifier = modifier,
        avatar = {
            Avatar(contentState.avatarData(AvatarSize.RoomHeader))
        },
        title = {
            if (contentState.name != null) {
                RoomPreviewTitleAtom(
                    title = contentState.name,
                )
            } else {
                RoomPreviewTitleAtom(
                    title = stringResource(id = CommonStrings.common_no_room_name),
                    fontStyle = FontStyle.Italic
                )
            }
        },
        subtitle = {
            if (contentState.alias != null) {
                RoomPreviewSubtitleAtom(contentState.alias.value)
            }
        },
        description = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val inviteSender = (contentState.joinAuthorisationStatus as? JoinAuthorisationStatus.IsInvited)?.inviteSender
                if (inviteSender != null) {
                    InviteSenderView(inviteSender = inviteSender)
                }
                RoomPreviewDescriptionAtom(contentState.topic ?: "")
                if (contentState.roomType == RoomType.Space) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.screen_join_room_space_not_supported_title),
                        textAlign = TextAlign.Center,
                        style = ElementTheme.typography.fontBodyLgMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.screen_join_room_space_not_supported_description, applicationName),
                        textAlign = TextAlign.Center,
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                } else if (contentState.joinAuthorisationStatus is JoinAuthorisationStatus.CanKnock) {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = knockMessage,
                        onValueChange = onKnockMessageUpdate,
                        maxLines = 3,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.screen_join_room_knock_message_description),
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textPlaceholder,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        memberCount = {
            if (contentState.showMemberCount) {
                RoomPreviewMembersCountMolecule(memberCount = contentState.numberOfMembers ?: 0)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinRoomTopBar(
    contentState: ContentState,
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
                        Avatar(avatarData = contentState.avatarData(AvatarSize.TimelineRoom))
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
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
        onCancelKnockSuccess = { },
    )
}
