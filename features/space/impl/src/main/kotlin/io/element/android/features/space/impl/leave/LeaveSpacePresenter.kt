/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.optionals.getOrNull

@Inject
class LeaveSpacePresenter(
    @Assisted private val inputs: SpaceEntryPoint.Inputs,
    private val matrixClient: MatrixClient,
) : Presenter<LeaveSpaceState> {
    @AssistedFactory
    fun interface Factory {
        fun create(inputs: SpaceEntryPoint.Inputs): LeaveSpacePresenter
    }

    private val spaceRoomList = matrixClient.spaceService.spaceRoomList(inputs.roomId)

    @Composable
    override fun present(): LeaveSpaceState {
        val coroutineScope = rememberCoroutineScope()
        val currentSpace by spaceRoomList.currentSpaceFlow.collectAsState()
        val leaveSpaceAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val selectedRoomIds = remember {
            mutableStateOf<Set<RoomId>>(emptySet())
        }
        val joinedSpaceRooms by produceState(emptyList()) {
            // TODO Get the joined room from the SDK, should also have the
            val rooms = listOf(
                aSpaceRoom(
                    roomId = RoomId("!roomId1:example.com"),
                ),
                aSpaceRoom(
                    roomId = RoomId("!roomId2:example.com"),
                ),
            )
            value = rooms
        }
        val selectableSpaceRooms by produceState<AsyncData<ImmutableList<SelectableSpaceRoom>>>(
            initialValue = AsyncData.Uninitialized,
            key1 = joinedSpaceRooms,
            key2 = selectedRoomIds.value,
        ) {
            value = AsyncData.Success(
                joinedSpaceRooms.map {
                    SelectableSpaceRoom(
                        it,
                        // TODO Get this value from the SDK
                        isLastAdmin = false,
                        selectedRoomIds.value.contains(it.roomId),
                    )
                }.toPersistentList()
            )
        }

        fun handleEvents(event: LeaveSpaceEvents) {
            when (event) {
                LeaveSpaceEvents.DeselectAllRooms -> selectedRoomIds.value = emptySet()
                LeaveSpaceEvents.SelectAllRooms -> {
                    selectedRoomIds.value = selectableSpaceRooms.dataOrNull()
                        .orEmpty()
                        .filter { it.isLastAdmin.not() }
                        .map { it.spaceRoom.roomId }
                        .toSet()
                }
                is LeaveSpaceEvents.ToggleRoomSelection -> {
                    val currentSet = selectedRoomIds.value
                    selectedRoomIds.value = if (currentSet.contains(event.roomId)) {
                        currentSet - event.roomId
                    } else {
                        currentSet + event.roomId
                    }
                }
                LeaveSpaceEvents.LeaveSpace -> coroutineScope.leaveSpace(
                    leaveSpaceAction = leaveSpaceAction,
                    selectedRoomIds = selectedRoomIds.value,
                )
                LeaveSpaceEvents.CloseError -> {
                    leaveSpaceAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return LeaveSpaceState(
            spaceName = currentSpace.getOrNull()?.name,
            selectableSpaceRooms = selectableSpaceRooms,
            leaveSpaceAction = leaveSpaceAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.leaveSpace(
        leaveSpaceAction: MutableState<AsyncAction<Unit>>,
        @Suppress("unused") selectedRoomIds: Set<RoomId>,
    ) = launch {
        runUpdatingState(leaveSpaceAction) {
            // TODO SDK API call to leave all the rooms and space
            delay(1000)
            val room = matrixClient.getRoom(inputs.roomId)
                ?: return@runUpdatingState Result.failure(Exception("Room not found"))
            room.leave()
        }
    }
}
