/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.features.space.impl.leave.ConfirmingLeavingSpace
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Inject
class SpacePresenter(
    @Assisted private val inputs: SpaceEntryPoint.Inputs,
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
) : Presenter<SpaceState> {
    @AssistedFactory
    fun interface Factory {
        fun create(inputs: SpaceEntryPoint.Inputs): SpacePresenter
    }

    private val spaceRoomList = client.spaceService.spaceRoomList(inputs.roomId)

    @Composable
    override fun present(): SpaceState {
        LaunchedEffect(Unit) {
            paginate()
        }
        val hideInvitesAvatar by client.rememberHideInvitesAvatar()
        val seenSpaceInvites by remember {
            seenInvitesStore.seenRoomIds().map { it.toPersistentSet() }
        }.collectAsState(persistentSetOf())

        val coroutineScope = rememberCoroutineScope()
        val children by spaceRoomList.spaceRoomsFlow.collectAsState(emptyList())
        val hasMoreToLoad by remember {
            spaceRoomList.paginationStatusFlow.mapState { status ->
                when (status) {
                    is SpaceRoomList.PaginationStatus.Idle -> status.hasMoreToLoad
                    SpaceRoomList.PaginationStatus.Loading -> true
                }
            }
        }.collectAsState()

        val currentSpace by remember { spaceRoomList.currentSpaceFlow() }.collectAsState(null)
        val leaveSpaceBottomSheetState = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        fun handleEvents(event: SpaceEvents) {
            when (event) {
                SpaceEvents.LoadMore -> coroutineScope.paginate()
                SpaceEvents.CancelLeaveSpace -> {
                    leaveSpaceBottomSheetState.value = AsyncAction.Uninitialized
                }
                SpaceEvents.LeaveSpace -> if (leaveSpaceBottomSheetState.value is AsyncAction.Confirming) {
                    coroutineScope.launch {
                        leaveSpaceBottomSheetState.value = AsyncAction.Loading
                        client.getRoom(inputs.roomId)?.leave()?.fold(
                            onSuccess = {
                                // Successfully left the space, nothing more to do, the screen will be closed automatically
                                leaveSpaceBottomSheetState.value = AsyncAction.Success(Unit)
                            },
                            onFailure = {
                                leaveSpaceBottomSheetState.value = AsyncAction.Failure(it)
                            }
                        )
                    }
                } else {
                    coroutineScope.startLeaveSpace(
                        spaceName = currentSpace?.name,
                        leaveSpaceBottomSheetState,
                    )
                }
            }
        }
        return SpaceState(
            currentSpace = currentSpace,
            children = children.toPersistentList(),
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            hasMoreToLoad = hasMoreToLoad,
            leaveSpaceBottomSheetState = leaveSpaceBottomSheetState.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.paginate() = launch {
        spaceRoomList.paginate()
    }

    private fun CoroutineScope.startLeaveSpace(
        spaceName: String?,
        leaveSpaceBottomSheetState: MutableState<AsyncAction<Unit>>,
    ) = launch {
        leaveSpaceBottomSheetState.value = ConfirmingLeavingSpace(
            spaceName = spaceName,
            roomsWhereUserIsTheOnlyAdmin = AsyncData.Loading(),
        )
        // TODO Fetch the actual list of rooms where the user is the only admin
        delay(1000)
        // Update state only if not cancelled by the user
        if (leaveSpaceBottomSheetState.value is ConfirmingLeavingSpace) {
            leaveSpaceBottomSheetState.value = ConfirmingLeavingSpace(
                spaceName = spaceName,
                roomsWhereUserIsTheOnlyAdmin = AsyncData.Success(persistentListOf()),
            )
        }
    }
}
