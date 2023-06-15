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
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.features.createroom.impl.userlist.SelectionMode
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.features.createroom.impl.userlist.UserListPresenter
import io.element.android.features.createroom.impl.userlist.UserListPresenterArgs
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.usersearch.api.UserRepository
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateRoomRootPresenter @Inject constructor(
    private val presenterFactory: UserListPresenter.Factory,
    private val userRepository: UserRepository,
    private val userListDataStore: UserListDataStore,
    private val matrixClient: MatrixClient,
    private val analyticsService: AnalyticsService,
    private val buildMeta: BuildMeta,
) : Presenter<CreateRoomRootState> {

    private val presenter by lazy {
        presenterFactory.create(
            UserListPresenterArgs(
                selectionMode = SelectionMode.Single,
            ),
            userRepository,
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
            matrixClient.findDM(matrixUser.userId).use { existingDM ->
                if (existingDM == null) {
                    localCoroutineScope.createDM(matrixUser, startDmAction)
                } else {
                    startDmAction.value = Async.Success(existingDM.roomId)
                }
            }
        }

        fun handleEvents(event: CreateRoomRootEvents) {
            when (event) {
                is CreateRoomRootEvents.StartDM -> startDm(event.matrixUser)
                CreateRoomRootEvents.CancelStartDM -> startDmAction.value = Async.Uninitialized
            }
        }

        return CreateRoomRootState(
            applicationName = buildMeta.applicationName,
            userListState = userListState,
            startDmAction = startDmAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.createDM(user: MatrixUser, startDmAction: MutableState<Async<RoomId>>) = launch {
        suspend {
            matrixClient.createDM(user.userId).getOrThrow()
                .also { analyticsService.capture(CreatedRoom(isDM = true)) }
        }.runCatchingUpdatingState(startDmAction)
    }
}
