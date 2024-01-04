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
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.createroom.impl.userlist.SelectionMode
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.features.createroom.impl.userlist.UserListPresenter
import io.element.android.features.createroom.impl.userlist.UserListPresenterArgs
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.usersearch.api.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateRoomRootPresenter @Inject constructor(
    presenterFactory: UserListPresenter.Factory,
    userRepository: UserRepository,
    userListDataStore: UserListDataStore,
    private val startDMAction: StartDMAction,
    private val buildMeta: BuildMeta,
) : Presenter<CreateRoomRootState> {

    private val presenter = presenterFactory.create(
        UserListPresenterArgs(
            selectionMode = SelectionMode.Single,
        ),
        userRepository,
        userListDataStore,
    )

    @Composable
    override fun present(): CreateRoomRootState {
        val userListState = presenter.present()

        val localCoroutineScope = rememberCoroutineScope()
        val startDmActionState: MutableState<AsyncData<RoomId>> = remember { mutableStateOf(AsyncData.Uninitialized) }

        fun handleEvents(event: CreateRoomRootEvents) {
            when (event) {
                is CreateRoomRootEvents.StartDM -> localCoroutineScope.launch {
                    startDMAction.execute(event.matrixUser.userId, startDmActionState)
                }
                CreateRoomRootEvents.CancelStartDM -> startDmActionState.value = AsyncData.Uninitialized
            }
        }

        return CreateRoomRootState(
            applicationName = buildMeta.applicationName,
            userListState = userListState,
            startDmAction = startDmActionState.value,
            eventSink = ::handleEvents,
        )
    }
}
