/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(FlowPreview::class)

package io.element.android.features.space.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.JoinedRoom.Trigger
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.toInviteData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.api.spaces.SpaceService
import io.element.android.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Inject
class SpacePresenter(
    private val spaceRoomList: SpaceRoomList,
    private val room: BaseRoom,
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
    private val joinRoom: JoinRoom,
    private val acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val spaceService: SpaceService,
) : Presenter<SpaceState> {
    private var children by mutableStateOf<ImmutableList<SpaceRoom>>(persistentListOf())

    @Composable
    override fun present(): SpaceState {
        LaunchedEffect(Unit) {
            spaceRoomList.spaceRoomsFlow.collect { children = it.toImmutableList() }
        }

        val hideInvitesAvatar by client.rememberHideInvitesAvatar()
        val seenSpaceInvites by remember {
            seenInvitesStore.seenRoomIds().map { it.toImmutableSet() }
        }.collectAsState(persistentSetOf())

        val localCoroutineScope = rememberCoroutineScope()

        val hasMoreToLoad by remember {
            spaceRoomList.paginationStatusFlow
                .mapState { status ->
                    when (status) {
                        is SpaceRoomList.PaginationStatus.Idle -> status.hasMoreToLoad
                        SpaceRoomList.PaginationStatus.Loading -> true
                    }
                }
                // Debounce to give more time for spaceRoomList to updates
                .debounce(100.milliseconds)
        }.collectAsState(true)

        val permissions by room.permissionsAsState(SpacePermissions.DEFAULT) { perms ->
            perms.spacePermissions()
        }

        val roomInfo by room.roomInfoFlow.collectAsState()
        val canAccessSpaceSettings by remember {
            derivedStateOf { permissions.settingsPermissions.hasAny(roomInfo.joinRule) }
        }
        val canEditSpaceGraph by remember {
            derivedStateOf { permissions.canEditSpaceGraph }
        }
        val (joinActions, setJoinActions) = remember { mutableStateOf(emptyMap<RoomId, AsyncAction<Unit>>()) }

        var topicViewerState: TopicViewerState by remember { mutableStateOf(TopicViewerState.Hidden) }

        // Manage mode state
        var isManageMode by remember { mutableStateOf(false) }
        var selectedRoomIds by remember { mutableStateOf<Set<RoomId>>(emptySet()) }
        var removeRoomsAction by remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        // Track locally removed rooms for partial failure cases
        var removedRoomIds by remember { mutableStateOf<Set<RoomId>>(emptySet()) }

        val filteredChildren by remember {
            derivedStateOf {
                val notRemoved = children.filterNot { it.roomId in removedRoomIds }
                if (isManageMode) {
                    // In manage mode, only show rooms (not spaces)
                    notRemoved.filter { !it.isSpace }.toImmutableList()
                } else {
                    notRemoved.toImmutableList()
                }
            }
        }

        LaunchedEffect(children) {
            // Remove joined children from the join actions
            val joinedChildren = children
                .filter { it.state == CurrentUserMembership.JOINED }
                .map { it.roomId }
            setJoinActions(joinActions - joinedChildren)
        }

        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        suspend fun exitManageMode(shouldReset: Boolean) {
            isManageMode = false
            selectedRoomIds = emptySet()
            removedRoomIds = emptySet()
            if (shouldReset) {
                // Reset the space room list to see the updates.
                spaceRoomList.reset()
            }
        }

        fun handleEvent(event: SpaceEvents) {
            when (event) {
                // SpaceRoomList is loaded automatically as backend is really slow. Event is kept for future.
                SpaceEvents.LoadMore -> Unit
                is SpaceEvents.Join -> {
                    sessionCoroutineScope.joinRoom(event.spaceRoom, joinActions, setJoinActions)
                }
                SpaceEvents.ClearFailures -> {
                    val failedActions = joinActions
                        .filterValues { it is AsyncAction.Failure }
                        .mapValues { AsyncAction.Uninitialized }
                    setJoinActions(joinActions + failedActions)
                }
                is SpaceEvents.AcceptInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.AcceptInvite(event.spaceRoom.toInviteData())
                    )
                }
                is SpaceEvents.DeclineInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.DeclineInvite(invite = event.spaceRoom.toInviteData(), shouldConfirm = true, blockUser = false)
                    )
                }
                SpaceEvents.HideTopicViewer -> topicViewerState = TopicViewerState.Hidden
                is SpaceEvents.ShowTopicViewer -> topicViewerState = TopicViewerState.Shown(event.topic)

                // Manage mode events
                SpaceEvents.EnterManageMode -> {
                    isManageMode = true
                    selectedRoomIds = emptySet()
                }
                SpaceEvents.ExitManageMode -> {
                    localCoroutineScope.launch { exitManageMode(shouldReset = removedRoomIds.isNotEmpty()) }
                }
                is SpaceEvents.ToggleRoomSelection -> {
                    selectedRoomIds = if (event.roomId in selectedRoomIds) {
                        selectedRoomIds - event.roomId
                    } else {
                        selectedRoomIds + event.roomId
                    }
                }
                SpaceEvents.RemoveSelectedRooms -> {
                    removeRoomsAction = AsyncAction.ConfirmingNoParams
                }
                SpaceEvents.ConfirmRoomRemoval -> {
                    localCoroutineScope.launch {
                        removeRoomsAction = AsyncAction.Loading
                        val spaceId = spaceRoomList.spaceId
                        val roomsToRemove = selectedRoomIds.toSet()
                        val successfullyRemoved = mutableSetOf<RoomId>()
                        val results = roomsToRemove.map { roomId ->
                            async {
                                spaceService.removeChildFromSpace(spaceId, roomId)
                                    .onSuccess { successfullyRemoved.add(roomId) }
                            }
                        }
                        results.awaitAll()
                        val hasError = successfullyRemoved.size < roomsToRemove.size
                        if (hasError) {
                            // On partial success, update selection to only keep failed rooms
                            selectedRoomIds = selectedRoomIds - successfullyRemoved
                            removedRoomIds = removedRoomIds + successfullyRemoved
                            removeRoomsAction = AsyncAction.Failure(Exception("Failed to remove some rooms"))
                        } else {
                            removeRoomsAction = AsyncAction.Success(Unit)
                            exitManageMode(shouldReset = true)
                        }
                    }
                }
                SpaceEvents.ClearRemoveAction -> {
                    removeRoomsAction = AsyncAction.Uninitialized
                }
            }
        }
        return SpaceState(
            spaceInfo = roomInfo,
            children = filteredChildren,
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            hasMoreToLoad = hasMoreToLoad,
            joinActions = joinActions.toImmutableMap(),
            acceptDeclineInviteState = acceptDeclineInviteState,
            topicViewerState = topicViewerState,
            canAccessSpaceSettings = canAccessSpaceSettings,
            isManageMode = isManageMode,
            selectedRoomIds = selectedRoomIds.toImmutableSet(),
            canEditSpaceGraph = canEditSpaceGraph,
            removeRoomsAction = removeRoomsAction,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.joinRoom(
        spaceRoom: SpaceRoom,
        joinActions: Map<RoomId, AsyncAction<Unit>>,
        setJoinActions: (Map<RoomId, AsyncAction<Unit>>) -> Unit
    ) = launch {
        setJoinActions(joinActions + mapOf(spaceRoom.roomId to AsyncAction.Loading))
        joinRoom.invoke(
            roomIdOrAlias = spaceRoom.roomId.toRoomIdOrAlias(),
            serverNames = spaceRoom.via,
            trigger = Trigger.SpaceHierarchy,
        ).onFailure {
            setJoinActions(joinActions + mapOf(spaceRoom.roomId to AsyncAction.Failure(it)))
        }
    }
}
