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

package io.element.android.features.roomdetails.impl.members

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.features.userlist.api.SelectionMode
import io.element.android.features.userlist.api.UserListDataSource
import io.element.android.features.userlist.api.UserListDataStore
import io.element.android.features.userlist.api.UserListPresenter
import io.element.android.features.userlist.api.UserListPresenterArgs
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.getMemberFlow
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class RoomMemberListPresenter @Inject constructor(
    private val userListPresenterFactory: UserListPresenter.Factory,
    @Named("RoomMembers") private val userListDataSource: UserListDataSource,
    private val userListDataStore: UserListDataStore,
    private val room: MatrixRoom,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Presenter<RoomMemberListState> {

    private val userListPresenter by lazy {
        userListPresenterFactory.create(
            UserListPresenterArgs(selectionMode = SelectionMode.Single),
            userListDataSource,
            userListDataStore,
        )
    }

    @Composable
    override fun present(): RoomMemberListState {
        val userListState = userListPresenter.present()
        val allUsers = remember { mutableStateOf<Async<ImmutableList<MatrixUser>>>(Async.Loading()) }

        LaunchedEffect(Unit) {
            withContext(coroutineDispatchers.io) {
                allUsers.value = Async.Success(userListDataSource.search("").toImmutableList())
            }
        }

        return RoomMemberListState(
            allUsers = allUsers.value,
            userListState = userListState,
        )
    }

}

