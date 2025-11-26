/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.map
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceHandle
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class LeaveSpacePresenter(
    @Assisted private val leaveSpaceHandle: LeaveSpaceHandle,
) : Presenter<LeaveSpaceState> {
    @AssistedFactory
    fun interface Factory {
        fun create(leaveSpaceHandle: LeaveSpaceHandle): LeaveSpacePresenter
    }

    data class LeaveSpaceRooms(
        val current: LeaveSpaceRoom?,
        val others: List<LeaveSpaceRoom>,
    )

    @Composable
    override fun present(): LeaveSpaceState {
        val coroutineScope = rememberCoroutineScope()
        var retryCount by remember { mutableIntStateOf(0) }
        val leaveSpaceAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        var selectedRoomIds by remember {
            mutableStateOf<Collection<RoomId>>(setOf())
        }
        var leaveSpaceRooms by remember {
            mutableStateOf<AsyncData<LeaveSpaceRooms>>(AsyncData.Loading())
        }
        LaunchedEffect(retryCount) {
            val rooms = leaveSpaceHandle.rooms()
            val (currentRoom, otherRooms) = rooms.getOrNull()
                .orEmpty()
                .partition { it.spaceRoom.roomId == leaveSpaceHandle.id }
            // By default select all rooms that can be left
            val otherRoomsExcludingDm = otherRooms.filter { it.spaceRoom.isDirect != true }
            selectedRoomIds = otherRoomsExcludingDm
                .filter { it.isLastAdmin.not() }
                .map { it.spaceRoom.roomId }
            leaveSpaceRooms = rooms.fold(
                onSuccess = {
                    AsyncData.Success(
                        LeaveSpaceRooms(
                            current = currentRoom.firstOrNull(),
                            others = otherRoomsExcludingDm.toImmutableList(),
                        )
                    )
                },
                onFailure = { AsyncData.Failure(it) }
            )
        }
        var selectableSpaceRooms by remember {
            mutableStateOf<AsyncData<ImmutableList<SelectableSpaceRoom>>>(AsyncData.Loading())
        }
        LaunchedEffect(selectedRoomIds, leaveSpaceRooms) {
            selectableSpaceRooms = leaveSpaceRooms.map {
                it.others.map { room ->
                    SelectableSpaceRoom(
                        spaceRoom = room.spaceRoom,
                        isLastAdmin = room.isLastAdmin,
                        isSelected = selectedRoomIds.contains(room.spaceRoom.roomId),
                    )
                }.toImmutableList()
            }
        }

        fun handleEvent(event: LeaveSpaceEvents) {
            when (event) {
                LeaveSpaceEvents.Retry -> {
                    leaveSpaceRooms = AsyncData.Loading()
                    retryCount += 1
                }
                LeaveSpaceEvents.DeselectAllRooms -> {
                    selectedRoomIds = persistentSetOf()
                }
                LeaveSpaceEvents.SelectAllRooms -> {
                    selectedRoomIds = selectableSpaceRooms.dataOrNull()
                        .orEmpty()
                        .filter { it.isLastAdmin.not() }
                        .map { it.spaceRoom.roomId }
                }
                is LeaveSpaceEvents.ToggleRoomSelection -> {
                    selectedRoomIds = if (selectedRoomIds.contains(event.roomId)) {
                        selectedRoomIds - event.roomId
                    } else {
                        selectedRoomIds + event.roomId
                    }
                }
                LeaveSpaceEvents.LeaveSpace -> coroutineScope.leaveSpace(
                    leaveSpaceAction = leaveSpaceAction,
                    selectedRoomIds = selectedRoomIds,
                )
                LeaveSpaceEvents.CloseError -> {
                    leaveSpaceAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return LeaveSpaceState(
            spaceName = leaveSpaceRooms.dataOrNull()?.current?.spaceRoom?.displayName,
            isLastAdmin = leaveSpaceRooms.dataOrNull()?.current?.isLastAdmin == true,
            selectableSpaceRooms = selectableSpaceRooms,
            leaveSpaceAction = leaveSpaceAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.leaveSpace(
        leaveSpaceAction: MutableState<AsyncAction<Unit>>,
        selectedRoomIds: Collection<RoomId>,
    ) = launch {
        runUpdatingState(leaveSpaceAction) {
            leaveSpaceHandle.leave(selectedRoomIds.toList())
        }
    }
}
