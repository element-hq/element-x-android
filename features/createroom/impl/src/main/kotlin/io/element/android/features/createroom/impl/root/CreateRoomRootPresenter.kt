/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.architecture.AsyncAction
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
        val startDmActionState: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvents(event: CreateRoomRootEvents) {
            when (event) {
                is CreateRoomRootEvents.StartDM -> localCoroutineScope.launch {
                    startDMAction.execute(event.matrixUser.userId, startDmActionState)
                }
                CreateRoomRootEvents.CancelStartDM -> startDmActionState.value = AsyncAction.Uninitialized
            }
        }

        return CreateRoomRootState(
            isDebugBuild = buildMeta.isDebuggable,
            applicationName = buildMeta.applicationName,
            userListState = userListState,
            startDmAction = startDmActionState.value,
            eventSink = ::handleEvents,
        )
    }
}
