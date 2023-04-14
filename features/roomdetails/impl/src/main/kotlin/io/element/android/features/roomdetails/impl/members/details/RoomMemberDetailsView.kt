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

package io.element.android.features.roomdetails.impl.members.details

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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.R
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
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomMemberDetailsView(
    state: RoomMemberDetailsState,
    onShareUser: () -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { }, navigationIcon = { BackButton(onClick = goBack) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(
                avatarUrl = state.avatarUrl,
                userId = state.userId,
                userName = state.userName,
            )

            ShareSection(onShareUser = onShareUser)

            SendMessageSection(onSendMessage = {
                // TODO implement send DM
            })

            BlockSection(isBlocked = state.isBlocked, onToggleBlock = {
                // TODO implement block & unblock
            })
        }
    }
}

@Composable
internal fun HeaderSection(
    avatarUrl: String?,
    userId: String,
    userName: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(70.dp)) {
            Avatar(
                avatarData = AvatarData(userId, userName, avatarUrl, AvatarSize.HUGE),
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(30.dp))
        if (userName != null) {
            Text(userName, style = ElementTextStyles.Bold.title1)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Text(userId, style = ElementTextStyles.Regular.body, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
internal fun ShareSection(onShareUser: () -> Unit, modifier: Modifier = Modifier) {
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(StringR.string.action_share),
            icon = Icons.Outlined.Share,
            onClick = onShareUser,
        )
    }
}

@Composable
internal fun SendMessageSection(onSendMessage: () -> Unit, modifier: Modifier = Modifier) {
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(StringR.string.action_send_message),
            icon = Icons.Outlined.ChatBubbleOutline,
            onClick = onSendMessage,
        )
    }
}

@Composable
internal fun BlockSection(isBlocked: Boolean, onToggleBlock: () -> Unit, modifier: Modifier = Modifier) {
    PreferenceCategory(showDivider = false, modifier = modifier) {
        if (isBlocked) {
            PreferenceText(
                title = stringResource(R.string.screen_dm_details_unblock_user),
                icon = Icons.Outlined.Block,
            )
        } else {
            PreferenceText(
                title = stringResource(R.string.screen_dm_details_block_user),
                icon = Icons.Outlined.Block,
                tintColor = LocalColors.current.textActionCritical,
            )
        }
    }
}

@Preview
@Composable
fun RoomMemberDetailsViewLightPreview(@PreviewParameter(RoomMemberDetailsStateProvider::class) state: RoomMemberDetailsState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun RoomMemberDetailsViewDarkPreview(@PreviewParameter(RoomMemberDetailsStateProvider::class) state: RoomMemberDetailsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomMemberDetailsState) {
    RoomMemberDetailsView(
        state = state,
        onShareUser = {},
        goBack = {},
    )
}
