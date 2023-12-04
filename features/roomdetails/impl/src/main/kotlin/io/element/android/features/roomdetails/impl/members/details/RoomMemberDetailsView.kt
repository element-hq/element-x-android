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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.blockuser.BlockUserDialogs
import io.element.android.features.roomdetails.impl.blockuser.BlockUserSection
import io.element.android.libraries.designsystem.components.async.AsyncView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomMemberDetailsView(
    state: RoomMemberDetailsState,
    onShareUser: () -> Unit,
    onDMStarted: (RoomId) -> Unit,
    goBack: () -> Unit,
    openAvatarPreview: (username: String, url: String) -> Unit,
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
                openAvatarPreview = { avatarUrl ->
                    openAvatarPreview(state.userName ?: state.userId, avatarUrl)
                },
            )

            RoomMemberMainActionsSection(onShareUser = onShareUser)

            Spacer(modifier = Modifier.height(26.dp))

            if (!state.isCurrentUser) {
                StartDMSection(onStartDMClicked = { state.eventSink(RoomMemberDetailsEvents.StartDM) })
                BlockUserSection(state)
                BlockUserDialogs(state)
            }
            AsyncView(
                async = state.startDmActionState,
                progressText = stringResource(CommonStrings.common_starting_chat),
                onSuccess = onDMStarted,
                errorMessage = { stringResource(R.string.screen_start_chat_error_starting_chat) },
                onRetry = { state.eventSink(RoomMemberDetailsEvents.StartDM) },
                onErrorDismiss = { state.eventSink(RoomMemberDetailsEvents.ClearStartDMState) },
            )
        }
    }
}

@Composable
private fun StartDMSection(
    onStartDMClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(stringResource(CommonStrings.common_direct_chat)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Chat)),
        style = ListItemStyle.Primary,
        onClick = onStartDMClicked,
        modifier = modifier,
    )
}

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
        onDMStarted = {},
        openAvatarPreview = { _, _ -> }
    )
}
