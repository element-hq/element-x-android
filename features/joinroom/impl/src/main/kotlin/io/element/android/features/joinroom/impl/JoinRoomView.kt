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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncData
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
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun JoinRoomView(
    state: JoinRoomState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier,
        topBar = {
            JoinRoomTopBar(onBackClicked = onBackPressed)
        },
        content = {
            JoinRoomContent(asyncContentState = state.contentState)
        },
        footer = {
            JoinRoomFooter(
                joinAuthorisationStatus = state.joinAuthorisationStatus,
                onAcceptInvite = {
                    state.eventSink(JoinRoomEvents.AcceptInvite)
                },
                onDeclineInvite = {
                    state.eventSink(JoinRoomEvents.DeclineInvite)
                },
                onJoinRoom = {
                    state.eventSink(JoinRoomEvents.JoinRoom)
                },
            )
        }
    )
}

@Composable
private fun JoinRoomFooter(
    joinAuthorisationStatus: JoinAuthorisationStatus,
    onAcceptInvite: () -> Unit,
    onDeclineInvite: () -> Unit,
    onJoinRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = stringResource(CommonStrings.action_join),
                onClick = onJoinRoom,
                modifier = modifier.fillMaxWidth(),
                size = ButtonSize.Medium,
            )
        }
        JoinAuthorisationStatus.CanKnock -> {
            //TODO knock
            /*
            Button(
                text = stringResource(CommonStrings.action_knock),
                onClick = onJoinRoom,
                modifier = modifier.fillMaxWidth(),
                size = ButtonSize.Medium,
            )
             */
        }
        JoinAuthorisationStatus.Unknown -> Unit
    }
}

@Composable
private fun JoinRoomContent(
    asyncContentState: AsyncData<ContentState>,
    modifier: Modifier = Modifier,
) {

    @Composable
    fun ContentScaffold(
        avatar: @Composable () -> Unit,
        title: String,
        description: String,
        memberCount: @Composable (() -> Unit)? = null
    ) {
        avatar()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = ElementTheme.typography.fontHeadingMdBold,
            textAlign = TextAlign.Center,
            color = ElementTheme.colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = ElementTheme.typography.fontBodyMdRegular,
            textAlign = TextAlign.Center,
            color = ElementTheme.colors.textSecondary,
        )
        memberCount?.invoke()
    }

    Column(
        modifier = modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (asyncContentState) {
            is AsyncData.Success -> {
                val contentState = asyncContentState.data
                ContentScaffold(
                    avatar = {
                        Avatar(contentState.avatarData(AvatarSize.RoomHeader))
                    },
                    title = contentState.name,
                    description = contentState.description ?: stringResource(R.string.screen_join_room_subtitle_no_preview)
                ) {
                    if (contentState.showMemberCount) {
                        JoinRoomMembersCount(memberCount = contentState.numberOfMembers ?: 0)
                    }
                }
            }
            else -> {
                ContentScaffold(
                    avatar = {
                        PlaceholderAtom(width = AvatarSize.RoomHeader.dp, height = AvatarSize.RoomHeader.dp)
                    },
                    title = stringResource(R.string.screen_join_room_title_no_preview),
                    description = stringResource(R.string.screen_join_room_subtitle_no_preview),
                )
            }
        }
    }
}

@Composable
private fun JoinRoomMembersCount(memberCount: Long) {
    Spacer(modifier = Modifier.height(8.dp))
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
        title = {

        },
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
