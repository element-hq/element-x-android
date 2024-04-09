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

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.createroom.impl.R
import io.element.android.features.createroom.impl.components.UserListView
import io.element.android.features.createroom.impl.userlist.UserListEvents
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AddPeopleView(
    state: UserListState,
    onBackPressed: () -> Unit,
    onNextPressed: () -> Unit,
    modifier: Modifier = Modifier,
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
        UserListView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            state = state,
            showBackButton = false,
            onUserSelected = {},
            onUserDeselected = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPeopleViewTopBar(
    hasSelectedUsers: Boolean,
    onBackPressed: () -> Unit,
    onNextPressed: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.screen_create_room_add_people_title),
                style = ElementTheme.typography.aliasScreenTitle
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
        actions = {
            val textActionResId = if (hasSelectedUsers) CommonStrings.action_next else CommonStrings.action_skip
            TextButton(
                text = stringResource(id = textActionResId),
                onClick = onNextPressed,
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun AddPeopleViewPreview(@PreviewParameter(AddPeopleUserListStateProvider::class) state: UserListState) = ElementPreview {
    AddPeopleView(
        state = state,
        onBackPressed = {},
        onNextPressed = {},
    )
}
