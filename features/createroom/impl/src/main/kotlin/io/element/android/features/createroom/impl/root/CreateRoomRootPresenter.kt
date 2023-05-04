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

package io.element.android.features.createroom.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.userlist.api.SelectionMode
import io.element.android.features.userlist.api.UserListDataSource
import io.element.android.features.userlist.api.UserListDataStore
import io.element.android.features.userlist.api.UserListPresenter
import io.element.android.features.userlist.api.UserListPresenterArgs
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.execute
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class CreateRoomRootPresenter @Inject constructor(
    private val presenterFactory: UserListPresenter.Factory,
    @Named("AllUsers") private val userListDataSource: UserListDataSource,
    private val userListDataStore: UserListDataStore,
    private val matrixClient: MatrixClient,
) : Presenter<CreateRoomRootState> {

    private val presenter by lazy {
        presenterFactory.create(
            UserListPresenterArgs(
                selectionMode = SelectionMode.Single,
                minimumSearchLength = 3,
                searchDebouncePeriodMillis = 500,
            ),
            userListDataSource,
            userListDataStore,
        )
    }

    @Composable
    override fun present(): CreateRoomRootState {
        val userListState = presenter.present()

        val localCoroutineScope = rememberCoroutineScope()
        val startDmAction: MutableState<Async<RoomId>> = remember { mutableStateOf(Async.Uninitialized) }

        fun startDm(matrixUser: MatrixUser) {
            startDmAction.value = Async.Uninitialized
            val existingDM = matrixClient.findDM(matrixUser.userId)
            if (existingDM == null) {
                localCoroutineScope.createDM(matrixUser, startDmAction)
            } else {
                startDmAction.value = Async.Success(existingDM.roomId)
            }
        }

        fun handleEvents(event: CreateRoomRootEvents) {
            when (event) {
                is CreateRoomRootEvents.StartDM -> startDm(event.matrixUser)
                CreateRoomRootEvents.CancelStartDM -> startDmAction.value = Async.Uninitialized
                CreateRoomRootEvents.InvitePeople -> Unit // Todo Handle invite people action
            }
        }

        return CreateRoomRootState(
            userListState = userListState,
            startDmAction = startDmAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.createDM(user: MatrixUser, startDmAction: MutableState<Async<RoomId>>) = launch {
        suspend {
            matrixClient.createDM(user.userId).getOrThrow()
        }.execute(startDmAction)
    }
}
