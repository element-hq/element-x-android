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
import io.element.android.features.selectusers.api.SelectUsersPresenter
import io.element.android.features.selectusers.api.SelectUsersPresenterArgs
import io.element.android.features.selectusers.api.SelectionMode
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.execute
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateRoomRootPresenter @Inject constructor(
    private val presenterFactory: SelectUsersPresenter.Factory,
    private val matrixClient: MatrixClient,
) : Presenter<CreateRoomRootState> {

    private val presenter by lazy {
        presenterFactory.create(SelectUsersPresenterArgs(SelectionMode.Single))
    }

    @Composable
    override fun present(): CreateRoomRootState {
        val selectUsersState = presenter.present()

        val localCoroutineScope = rememberCoroutineScope()
        val startDmAction: MutableState<Async<RoomId>> = remember { mutableStateOf(Async.Uninitialized) }

        fun handleEvents(event: CreateRoomRootEvents) {
            when (event) {
                is CreateRoomRootEvents.StartDM -> {
                    val existingDM = matrixClient.findDM(event.matrixUser.id)
                    if (existingDM == null) {
                        localCoroutineScope.createDM(event.matrixUser, startDmAction)
                    } else {
                        startDmAction.value = Async.Success(existingDM.roomId)
                    }
                }
                CreateRoomRootEvents.InvitePeople -> Unit // Todo Handle invite people action
            }
        }

        return CreateRoomRootState(
            selectUsersState = selectUsersState,
            startDmAction = startDmAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.createDM(user: MatrixUser, startDmAction: MutableState<Async<RoomId>>) = launch {
        suspend {
            matrixClient.createDM(user.id).getOrThrow()
        }.execute(startDmAction)
    }
}
