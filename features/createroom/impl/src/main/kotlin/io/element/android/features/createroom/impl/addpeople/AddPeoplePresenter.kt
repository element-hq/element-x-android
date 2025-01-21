/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.addpeople

import androidx.compose.runtime.Composable
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.features.createroom.impl.userlist.SelectionMode
import io.element.android.features.createroom.impl.userlist.UserListPresenter
import io.element.android.features.createroom.impl.userlist.UserListPresenterArgs
import io.element.android.features.createroom.impl.userlist.UserListState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.usersearch.api.UserRepository
import javax.inject.Inject

class AddPeoplePresenter @Inject constructor(
    userListPresenterFactory: UserListPresenter.Factory,
    userRepository: UserRepository,
    dataStore: CreateRoomDataStore,
) : Presenter<UserListState> {
    private val userListPresenter = userListPresenterFactory.create(
        UserListPresenterArgs(
            selectionMode = SelectionMode.Multiple,
        ),
        userRepository,
        dataStore.selectedUserListDataStore,
    )

    @Composable
    override fun present(): UserListState {
        return userListPresenter.present()
    }
}
