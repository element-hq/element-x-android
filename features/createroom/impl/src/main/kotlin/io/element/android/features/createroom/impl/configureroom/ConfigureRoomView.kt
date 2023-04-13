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
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.element.android.features.createroom.impl.R
import io.element.android.features.userlist.api.components.SelectedUsersList
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureRoomView(
    state: ConfigureRoomState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier,
        topBar = {
            ConfigureRoomToolbar(
                isNextActionEnabled = state.isCreateButtonEnabled,
                onBackPressed = onBackPressed,
                onNextPressed = { state.eventSink(ConfigureRoomEvents.CreateRoom) },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            RoomNameWithAvatar(
                modifier = Modifier.padding(horizontal = 16.dp),
                avatarUri = state.config.avatarUrl?.toUri(),
                roomName = state.config.roomName.orEmpty(),
                onAvatarClick = { Toast.makeText(context, "not implemented yet", Toast.LENGTH_SHORT).show() },
                onRoomNameChanged = { state.eventSink(ConfigureRoomEvents.RoomNameChanged(it)) },
            )
            RoomTopic(
                modifier = Modifier.padding(horizontal = 16.dp),
                topic = state.config.topic.orEmpty(),
                onTopicChanged = { state.eventSink(ConfigureRoomEvents.TopicChanged(it)) },
            )
            SelectedUsersList(
                contentPadding = PaddingValues(horizontal = 24.dp),
                selectedUsers = state.config.invites,
                onUserRemoved = { state.eventSink(ConfigureRoomEvents.RemoveFromSelection(it)) },
            )
            Spacer(Modifier.weight(1f))
            RoomPrivacyOptions(
                modifier = Modifier.padding(bottom = 40.dp),
                selected = state.config.privacy,
                onOptionSelected = { state.eventSink(ConfigureRoomEvents.RoomPrivacyChanged(it)) },
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
                text = stringResource(R.string.screen_create_room_title),
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
                    text = stringResource(StringR.string.action_create),
                    fontSize = 16.sp,
                )
            }
        }
    )
}

@Composable
fun RoomNameWithAvatar(
    avatarUri: Uri?,
    roomName: String,
    modifier: Modifier = Modifier,
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

        LabelledTextField(
            label = stringResource(R.string.screen_create_room_room_name_label),
            value = roomName,
            placeholder = stringResource(R.string.screen_create_room_room_name_placeholder),
            onValueChange = onRoomNameChanged
        )
    }
}

@Composable
fun Avatar(
    avatarUri: Uri?,
    modifier: Modifier = Modifier,
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
            modifier = commonModifier,
            model = model,
            contentDescription = null,
        )
    } else {
        Box(modifier = commonModifier.background(LocalColors.current.quinary)) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = "",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
fun RoomTopic(
    topic: String,
    modifier: Modifier = Modifier,
    onTopicChanged: (String) -> Unit = {},
) {
    LabelledTextField(
        modifier = modifier,
        label = stringResource(R.string.screen_create_room_topic_label),
        value = topic,
        placeholder = stringResource(R.string.screen_create_room_topic_placeholder),
        onValueChange = onTopicChanged,
        maxLines = 3,
    )
}

@Composable
fun RoomPrivacyOptions(
    selected: RoomPrivacy?,
    modifier: Modifier = Modifier,
    onOptionSelected: (RoomPrivacy) -> Unit = {},
) {

    data class RoomPrivacyItem(
        val privacy: RoomPrivacy,
        val icon: ImageVector,
        val title: String,
        val description: String,
    )

    val items = RoomPrivacy.values().map {
        when (it) {
            RoomPrivacy.Public -> RoomPrivacyItem(
                privacy = it,
                icon = Icons.Outlined.Lock,
                title = stringResource(R.string.screen_create_room_private_option_title),
                description = stringResource(R.string.screen_create_room_private_option_description),
            )
            RoomPrivacy.Private -> RoomPrivacyItem(
                privacy = it,
                icon = Icons.Outlined.Public,
                title = stringResource(R.string.screen_create_room_public_option_title),
                description = stringResource(R.string.screen_create_room_public_option_description),
            )
        }
    }
    Column(modifier = modifier.selectableGroup()) {
        items.forEach { item ->
            RoomPrivacyOption(
                privacy = RoomPrivacy.Private,
                icon = item.icon,
                title = item.title,
                description = item.description,
                isSelected = selected == item.privacy,
                onOptionSelected = { onOptionSelected(item.privacy) }
            )
        }
    }
}

@Composable
fun RoomPrivacyOption(
    privacy: RoomPrivacy,
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onOptionSelected: (RoomPrivacy) -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onOptionSelected(privacy) },
                role = Role.RadioButton,
            )
            .padding(8.dp),
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 8.dp),
            imageVector = icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.secondary,
        )

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        RadioButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(48.dp),
            selected = isSelected,
            onClick = null // null recommended for accessibility with screenreaders
        )
    }
}

// Move this composable to design module if we want to reuse it in other screens
@Composable
fun LabelledTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    maxLines: Int = 1,
    onValueChange: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = label
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            placeholder = { Text(placeholder) },
            onValueChange = onValueChange,
            maxLines = maxLines,
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
