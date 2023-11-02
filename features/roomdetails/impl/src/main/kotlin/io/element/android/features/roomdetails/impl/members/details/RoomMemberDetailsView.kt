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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.blockuser.BlockUserDialogs
import io.element.android.features.roomdetails.impl.blockuser.BlockUserSection
import io.element.android.libraries.composeutils.annotations.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
        ) {
            RoomMemberHeaderSection(
                avatarUrl = state.avatarUrl,
                userId = state.userId,
                userName = state.userName,
            )

            RoomMemberMainActionsSection(onShareUser = onShareUser)

            Spacer(modifier = Modifier.height(26.dp))

            // TODO implement send DM
            // SendMessageSection(onSendMessage = {
            //     ...
            // })

            if (!state.isCurrentUser) {
                BlockUserSection(state)
                BlockUserDialogs(state)
            }
        }
    }
}

/*
@Composable
private fun SendMessageSection(onSendMessage: () -> Unit, modifier: Modifier = Modifier) {
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(CommonStrings.action_send_message),
            icon = Icons.Outlined.ChatBubbleOutline,
            onClick = onSendMessage,
        )
    }
}
 */

@PreviewWithLargeHeight
@Composable
internal fun RoomMemberDetailsViewLightPreview(@PreviewParameter(RoomMemberDetailsStateProvider::class) state: RoomMemberDetailsState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun RoomMemberDetailsViewDarkPreview(@PreviewParameter(RoomMemberDetailsStateProvider::class) state: RoomMemberDetailsState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomMemberDetailsState) {
    RoomMemberDetailsView(
        state = state,
        onShareUser = {},
        goBack = {},
    )
}
