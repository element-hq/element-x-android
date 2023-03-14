/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.createroom.impl.selectmembers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.ui.model.MatrixUser
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectMembersView(
    state: SelectMembersState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNextPressed: () -> Unit = {},
) {
    val eventSink = state.eventSink

    Scaffold(
        topBar = {
            SelectMembersViewTopBar(
                hasSelectedUsers = state.selectedUsers.isNotEmpty(),
                onBackPressed = onBackPressed,
                onNextPressed = onNextPressed,
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // TODO create a SearchUserView with multi selection option + callbacks
            SelectedMembersList(
                modifier = Modifier.padding(horizontal = 16.dp),
                selectedUsers = state.selectedUsers,
                onUserRemoved = { eventSink(SelectMembersEvents.RemoveFromSelection(it)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectMembersViewTopBar(
    hasSelectedUsers: Boolean,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNextPressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(id = StringR.string.add_people),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
        actions = {
            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = onNextPressed,
            ) {
                val textActionResId = if (hasSelectedUsers) StringR.string.action_next else StringR.string.action_skip
                Text(
                    text = stringResource(id = textActionResId),
                    fontSize = 16.sp,
                )
            }
        }
    )
}

@Composable
fun SelectedMembersList(
    selectedUsers: List<MatrixUser>,
    modifier: Modifier = Modifier,
    onUserRemoved: (MatrixUser) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(selectedUsers) { matrixUser ->
            SelectedMember(
                matrixUser = matrixUser,
                onUserRemoved = onUserRemoved,
            )
        }
    }
}

@Composable
fun SelectedMember(
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    onUserRemoved: (MatrixUser) -> Unit,
) {
    Box(modifier = modifier.width(56.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // TODO set the size using custom Avatar size set to 56.dp
            Avatar(matrixUser.avatarData)
            Text(
                text = matrixUser.username.orEmpty(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .size(20.dp)
                .align(Alignment.TopEnd),
            onClick = { onUserRemoved(matrixUser) }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(id = StringR.string.action_remove),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Preview
@Composable
internal fun ChangeServerViewLightPreview(@PreviewParameter(SelectMembersStateProvider::class) state: SelectMembersState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun ChangeServerViewDarkPreview(@PreviewParameter(SelectMembersStateProvider::class) state: SelectMembersState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: SelectMembersState) {
    SelectMembersView(state = state)
}
