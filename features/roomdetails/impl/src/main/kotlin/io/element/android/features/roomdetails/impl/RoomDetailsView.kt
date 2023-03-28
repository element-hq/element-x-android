/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.roomdetails.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailsView(
    state: RoomDetailsState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit,
    onShareRoom: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { }, navigationIcon = { BackButton(onClick = goBack) })
        },
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(
                avatarUrl = state.roomAvatarUrl,
                roomId = state.roomId,
                roomName = state.roomName,
                roomAlias = state.roomAlias
            )

            ShareSection(onShareRoom = onShareRoom)

            if (state.roomTopic != null) {
                TopicSection(roomTopic = state.roomTopic)
            }

            MembersSection(memberCount = state.memberCount)

            if (state.isEncrypted) {
                SecuritySection()
            }

            OtherActionsSection()
        }
    }
}

@Composable
internal fun ShareSection(onShareRoom: () -> Unit, modifier: Modifier = Modifier) {
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_share_room_title),
            icon = Icons.Outlined.Share,
            onClick = onShareRoom,
        )
    }
}

@Composable
internal fun HeaderSection(
    avatarUrl: String?,
    roomId: String,
    roomName: String,
    roomAlias: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(70.dp)) {
            Avatar(
                avatarData = AvatarData(roomId, roomName, avatarUrl, AvatarSize.HUGE),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(roomName, style = ElementTextStyles.Bold.title1)
        if (roomAlias != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(roomAlias, style = ElementTextStyles.Regular.body, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
internal fun TopicSection(roomTopic: String, modifier: Modifier = Modifier) {
    PreferenceCategory(title = stringResource(R.string.screen_room_details_topic_title), modifier = modifier) {
        Text(
            roomTopic,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }

}

@Composable
internal fun MembersSection(memberCount: Int, modifier: Modifier = Modifier) {
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_people_title),
            icon = Icons.Outlined.Person,
            currentValue = memberCount.toString(),
        )
        PreferenceText(
            title = stringResource(R.string.screen_room_details_invite_people_title),
            icon = Icons.Outlined.PersonAddAlt,
        )
    }
}

@Composable
internal fun SecuritySection(modifier: Modifier = Modifier) {
    PreferenceCategory(title = stringResource(R.string.screen_room_details_security_title), modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_encryption_enabled_title),
            subtitle = stringResource(R.string.screen_room_details_encryption_enabled_subtitle),
            icon = Icons.Outlined.Lock,
        )
    }
}

@Composable
internal fun OtherActionsSection(modifier: Modifier = Modifier) {
    PreferenceCategory(showDivider = false, modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_leave_room_title),
            icon = ImageVector.vectorResource(R.drawable.ic_door_open),
            tintColor = LocalColors.current.textActionCritical,
        )
    }
}

@Preview
@Composable
fun RoomDetailsLightPreview(@PreviewParameter(RoomDetailsStateProvider::class) state: RoomDetailsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun RoomDetailsDarkPreview(@PreviewParameter(RoomDetailsStateProvider::class) state: RoomDetailsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomDetailsState) {
    RoomDetailsView(
        state = state,
        goBack = {},
        onShareRoom = {},
    )
}
