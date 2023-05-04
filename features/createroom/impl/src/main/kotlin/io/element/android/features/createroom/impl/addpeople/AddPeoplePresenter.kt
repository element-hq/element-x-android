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
import io.element.android.features.userlist.api.SelectionMode
import io.element.android.features.userlist.api.UserListDataSource
import io.element.android.features.userlist.api.UserListPresenter
import io.element.android.features.userlist.api.UserListPresenterArgs
import io.element.android.features.userlist.api.UserListState
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject
import javax.inject.Named

class AddPeoplePresenter @Inject constructor(
    private val userListPresenterFactory: UserListPresenter.Factory,
    @Named("AllUsers") private val userListDataSource: UserListDataSource,
    private val dataStore: CreateRoomDataStore,
) : Presenter<UserListState> {

    private val userListPresenter by lazy {
        userListPresenterFactory.create(
            UserListPresenterArgs(
                selectionMode = SelectionMode.Multiple,
                minimumSearchLength = 3,
                searchDebouncePeriodMillis = UserListPresenterArgs.DEFAULT_DEBOUNCE
            ),
            userListDataSource,
            dataStore.selectedUserListDataStore,
        )
    }

    @Composable
    override fun present(): UserListState {
        return userListPresenter.present()
    }
}

