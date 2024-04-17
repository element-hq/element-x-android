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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.PlaceholderAtom
import io.element.android.libraries.designsystem.atomic.molecules.ButtonRowMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun JoinRoomView(
    state: JoinRoomState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier,
        paddingValues = PaddingValues(16.dp),
        topBar = {
            JoinRoomTopBar(onBackClicked = onBackPressed)
        },
        content = {
            JoinRoomContent(contentState = state.contentState)
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
                onRetry = {
                    state.eventSink(JoinRoomEvents.RetryFetchingContent)
                }
            )
        }
    )
}

@Composable
private fun JoinRoomFooter(
    state: JoinRoomState,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit,
    onJoinRoom: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.contentState is ContentState.Failure) {
        Button(
            text = stringResource(CommonStrings.action_retry),
            onClick = onRetry,
            modifier = modifier.fillMaxWidth(),
            size = ButtonSize.Medium,
        )
    } else {
        val joinAuthorisationStatus = state.joinAuthorisationStatus
        when (joinAuthorisationStatus) {
            JoinAuthorisationStatus.IsInvited -> {
                ButtonRowMolecule(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    OutlinedButton(
                        text = stringResource(CommonStrings.action_decline),
                        onClick = onDeclineInvite,
                        modifier = Modifier.weight(1f),
                        size = ButtonSize.Medium,
                    )
                    Button(
                        text = stringResource(CommonStrings.action_accept),
                        onClick = onAcceptInvite,
                        modifier = Modifier.weight(1f),
                        size = ButtonSize.Medium,
                    )
                }
            }
            JoinAuthorisationStatus.CanJoin -> {
                Button(
                    text = stringResource(R.string.screen_join_room_join_action),
                    onClick = onJoinRoom,
                    modifier = modifier.fillMaxWidth(),
                    size = ButtonSize.Medium,
                )
            }
            JoinAuthorisationStatus.CanKnock -> {
                Button(
                    text = stringResource(R.string.screen_join_room_knock_action),
                    onClick = onJoinRoom,
                    modifier = modifier.fillMaxWidth(),
                    size = ButtonSize.Medium,
                )
            }
            JoinAuthorisationStatus.Unknown -> Unit
        }
    }
}

@Composable
private fun JoinRoomContent(
    contentState: ContentState,
    modifier: Modifier = Modifier,
) {
    when (contentState) {
        is ContentState.Loaded -> {
            ContentScaffold(
                modifier = modifier,
                avatar = {
                    Avatar(contentState.avatarData(AvatarSize.RoomHeader))
                },
                title = {
                    Title(contentState.computedTitle)
                },
                subtitle = {
                    Subtitle(contentState.computedSubtitle)
                },
                description = {
                    Description(contentState.topic ?: "")
                },
                memberCount = {
                    if (contentState.showMemberCount) {
                        MembersCount(memberCount = contentState.numberOfMembers ?: 0)
                    }
                }
            )
        }
        is ContentState.UnknownRoom -> {
            ContentScaffold(
                modifier = modifier,
                avatar = {
                    PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
                },
                title = {
                    Title(stringResource(R.string.screen_join_room_title_no_preview))
                },
                subtitle = {
                    Subtitle(stringResource(R.string.screen_join_room_subtitle_no_preview))
                },
            )
        }
        is ContentState.Loading -> {
            ContentScaffold(
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
            ContentScaffold(
                modifier = modifier,
                avatar = {
                    PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
                },
                title = {
                    when (contentState.roomIdOrAlias) {
                        is RoomIdOrAlias.Alias -> {
                            Title(contentState.roomIdOrAlias.identifier)
                        }
                        is RoomIdOrAlias.Id -> {
                            PlaceholderAtom(width = 200.dp, height = 22.dp)
                        }
                    }
                },
                subtitle = {
                    Text(
                        text = "Failed to get information about the room",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}

@Composable
private fun ContentScaffold(
    avatar: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: @Composable (() -> Unit)? = null,
    memberCount: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        avatar()
        Spacer(modifier = Modifier.height(16.dp))
        title()
        Spacer(modifier = Modifier.height(8.dp))
        subtitle()
        Spacer(modifier = Modifier.height(8.dp))
        if (memberCount != null) {
            memberCount()
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (description != null) {
            description()
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Title(title: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = title,
        style = ElementTheme.typography.fontHeadingMdBold,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textPrimary,
    )
}

@Composable
private fun Subtitle(subtitle: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = subtitle,
        style = ElementTheme.typography.fontBodyLgRegular,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textSecondary,
    )
}

@Composable
private fun Description(description: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = description,
        style = ElementTheme.typography.fontBodySmRegular,
        textAlign = TextAlign.Center,
        color = ElementTheme.colors.textSecondary,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun MembersCount(memberCount: Long) {
    Row(
        modifier = Modifier
                .background(color = ElementTheme.colors.bgSubtleSecondary, shape = CircleShape)
                .widthIn(min = 48.dp)
                .padding(all = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = CompoundIcons.UserProfile(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
        )
        Text(
            text = "$memberCount",
            style = ElementTheme.typography.fontBodySmMedium,
            color = ElementTheme.colors.textSecondary,
        )
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

@PreviewLightDark
@Composable
internal fun JoinRoomViewPreview(@PreviewParameter(JoinRoomStateProvider::class) state: JoinRoomState) = ElementPreview {
    JoinRoomView(
        state = state,
        onBackPressed = { }
    )
}
