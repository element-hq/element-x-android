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

package io.element.android.features.joinroom.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewDescriptionAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewSubtitleAtom
import io.element.android.libraries.designsystem.atomic.atoms.RoomPreviewTitleAtom
import io.element.android.libraries.designsystem.atomic.molecules.ButtonRowMolecule
import io.element.android.libraries.designsystem.atomic.molecules.RoomPreviewMembersCountMolecule
import io.element.android.libraries.designsystem.atomic.organisms.RoomPreviewOrganism
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.background.LightGradientBackground
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.SuperButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.ui.components.InviteSenderView
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun JoinRoomView(
    state: JoinRoomState,
    onBackPressed: () -> Unit,
    onKnockSuccess: () -> Unit,
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
                JoinRoomTopBar(onBackClicked = onBackPressed)
            },
            content = {
                JoinRoomContent(
                    contentState = state.contentState,
                    applicationName = state.applicationName,
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
                    onRetry = {
                        state.eventSink(JoinRoomEvents.RetryFetchingContent)
                    },
                    onGoBack = onBackPressed,
                )
            }
        )
    }

    AsyncActionView(
        async = state.knockAction,
        onSuccess = { onKnockSuccess() },
        onErrorDismiss = { state.eventSink(JoinRoomEvents.ClearError) },
    )
}

@Composable
private fun JoinRoomFooter(
    state: JoinRoomState,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit,
    onJoinRoom: () -> Unit,
    onKnockRoom: () -> Unit,
    onRetry: () -> Unit,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.contentState is ContentState.Failure) {
        Button(
            text = stringResource(CommonStrings.action_retry),
            onClick = onRetry,
            modifier = modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
    } else if (state.contentState is ContentState.Loaded && state.contentState.roomType == RoomType.Space) {
        Button(
            text = stringResource(CommonStrings.action_go_back),
            onClick = onGoBack,
            modifier = modifier.fillMaxWidth(),
            size = ButtonSize.Large,
        )
    } else {
        val joinAuthorisationStatus = state.joinAuthorisationStatus
        when (joinAuthorisationStatus) {
            is JoinAuthorisationStatus.IsInvited -> {
                ButtonRowMolecule(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    OutlinedButton(
                        text = stringResource(CommonStrings.action_decline),
                        onClick = onDeclineInvite,
                        modifier = Modifier.weight(1f),
                        size = ButtonSize.Large,
                    )
                    Button(
                        text = stringResource(CommonStrings.action_accept),
                        onClick = onAcceptInvite,
                        modifier = Modifier.weight(1f),
                        size = ButtonSize.Large,
                    )
                }
            }
            JoinAuthorisationStatus.CanJoin -> {
                SuperButton(
                    onClick = onJoinRoom,
                    modifier = modifier.fillMaxWidth(),
                    buttonSize = ButtonSize.Large,
                ) {
                    Text(
                        text = stringResource(R.string.screen_join_room_join_action),
                    )
                }
            }
            JoinAuthorisationStatus.CanKnock -> {
                Button(
                    text = stringResource(R.string.screen_join_room_knock_action),
                    onClick = onKnockRoom,
                    modifier = modifier.fillMaxWidth(),
                    size = ButtonSize.Large,
                )
            }
            JoinAuthorisationStatus.Unknown -> Unit
        }
    }
}

@Composable
private fun JoinRoomContent(
    contentState: ContentState,
    applicationName: String,
    modifier: Modifier = Modifier,
) {
    when (contentState) {
        is ContentState.Loaded -> {
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
        is ContentState.UnknownRoom -> {
            RoomPreviewOrganism(
                modifier = modifier,
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
                modifier = modifier,
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
                modifier = modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JoinRoomTopBar(
    onBackClicked: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClicked)
        },
        title = {},
    )
}

@PreviewsDayNight
@Composable
internal fun JoinRoomViewPreview(@PreviewParameter(JoinRoomStateProvider::class) state: JoinRoomState) = ElementPreview {
    JoinRoomView(
        state = state,
        onBackPressed = { },
        onKnockSuccess = { },
    )
}
