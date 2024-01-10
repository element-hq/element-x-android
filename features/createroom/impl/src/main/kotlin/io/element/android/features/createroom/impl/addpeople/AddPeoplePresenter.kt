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
