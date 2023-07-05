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

package io.element.android.features.createroom.impl.addpeople

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.createroom.impl.R
import io.element.android.features.createroom.impl.components.UserListView
import io.element.android.features.createroom.impl.userlist.UserListEvents
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddPeopleView(
    state: UserListState,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNextPressed: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AddPeopleViewTopBar(
                hasSelectedUsers = state.selectedUsers.isNotEmpty(),
                onBackPressed = {
                    if (state.isSearchActive) {
                        state.eventSink(UserListEvents.OnSearchActiveChanged(false))
                    } else {
                        onBackPressed()
                    }
                },
                onNextPressed = onNextPressed,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            UserListView(
                modifier = Modifier
                    .fillMaxWidth(),
                state = state,
                showBackButton = false,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPeopleViewTopBar(
    hasSelectedUsers: Boolean,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onNextPressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(id = R.string.screen_create_room_add_people_title),
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
                val textActionResId = if (hasSelectedUsers) CommonStrings.action_next else CommonStrings.action_skip
                Text(
                    text = stringResource(id = textActionResId),
                    fontSize = 16.sp,
                )
            }
        }
    )
}

@Preview
@Composable
internal fun AddPeopleViewLightPreview(@PreviewParameter(AddPeopleUserListStateProvider::class) state: UserListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun AddPeopleViewDarkPreview(@PreviewParameter(AddPeopleUserListStateProvider::class) state: UserListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: UserListState) {
    AddPeopleView(state = state)
}
