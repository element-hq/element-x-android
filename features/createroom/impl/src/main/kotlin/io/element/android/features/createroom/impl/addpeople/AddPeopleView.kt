/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AddPeopleViewTopBar(
                hasSelectedUsers = state.selectedUsers.isNotEmpty(),
                onBackClick = {
                    if (state.isSearchActive) {
                        state.eventSink(UserListEvents.OnSearchActiveChanged(false))
                    } else {
                        onBackClick()
                    }
                },
                onNextClick = onNextClick,
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
            onSelectUser = {},
            onDeselectUser = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPeopleViewTopBar(
    hasSelectedUsers: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.screen_create_room_add_people_title),
                style = ElementTheme.typography.aliasScreenTitle
            )
        },
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            val textActionResId = if (hasSelectedUsers) CommonStrings.action_next else CommonStrings.action_skip
            TextButton(
                text = stringResource(id = textActionResId),
                onClick = onNextClick,
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun AddPeopleViewPreview(@PreviewParameter(AddPeopleUserListStateProvider::class) state: UserListState) = ElementPreview {
    AddPeopleView(
        state = state,
        onBackClick = {},
        onNextClick = {},
    )
}
