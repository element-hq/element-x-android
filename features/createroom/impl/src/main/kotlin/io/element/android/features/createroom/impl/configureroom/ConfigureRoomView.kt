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

package io.element.android.features.createroom.impl.configureroom

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.element.android.features.selectusers.api.SelectedUsersList
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureRoomView(
    state: ConfigureRoomState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onCreatePressed: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ConfigureRoomToolbar(
                isNextActionEnabled = false,
                onBackPressed = onBackPressed,
                onNextPressed = onCreatePressed,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            RoomNameWithAvatar(
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            RoomTopic(
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            SelectedUsersList(
                listState = LazyListState(), // FIXME
                contentPadding = PaddingValues(horizontal = 24.dp),
                selectedUsers = state.selectedUsers,
                onUserRemoved = {
                    // TODO
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureRoomToolbar(
    isNextActionEnabled: Boolean,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNextPressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "Create a room",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
        actions = {
            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                enabled = isNextActionEnabled,
                onClick = onNextPressed,
            ) {
                Text(
                    text = "Create",
                    fontSize = 16.sp,
                )
            }
        }
    )
}

@Composable
fun RoomNameWithAvatar(
    modifier: Modifier = Modifier,
    avatarUri: Uri? = null,
    roomName: String = "",
    onAvatarClick: () -> Unit = {},
    onRoomNameChanged: (String) -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarUri = avatarUri,
            onClick = onAvatarClick,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "Room name"
            )

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = roomName,
                placeholder = { Text("e.g. Product Sprint") },
                onValueChange = onRoomNameChanged,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    avatarUri: Uri? = null,
    onClick: () -> Unit = {},
) {
    val commonModifier = modifier
        .size(70.dp)
        .clip(CircleShape)
        .clickable(onClick = onClick)

    if (avatarUri != null) {
        val context = LocalContext.current
        val model = ImageRequest.Builder(context)
            .data(avatarUri)
            .build()
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = commonModifier,
        )
    } else {
        Box(
            modifier = commonModifier
                .background(LocalColors.current.quinary)
        ) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = "",
                modifier = modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
fun RoomTopic(
    modifier: Modifier = Modifier,
    topic: String = "",
    onTopicChanged: (String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Topic (optional)",
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = topic,
            placeholder = { Text("What is this room about?") },
            onValueChange = onTopicChanged,
            maxLines = 3,
        )
    }
}

@Preview
@Composable
fun ConfigureRoomViewLightPreview(@PreviewParameter(ConfigureRoomStateProvider::class) state: ConfigureRoomState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun ConfigureRoomViewDarkPreview(@PreviewParameter(ConfigureRoomStateProvider::class) state: ConfigureRoomState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ConfigureRoomState) {
    ConfigureRoomView(
        state = state,
    )
}
