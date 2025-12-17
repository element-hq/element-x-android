/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

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
import io.element.android.features.space.impl.settings.SpaceSettingsPermissions
import io.element.android.features.space.impl.settings.spaceSettingsPermissions
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.jvm.optionals.getOrNull

@Inject
class SpacePresenter(
    private val spaceRoomList: SpaceRoomList,
    private val room: BaseRoom,
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
    private val joinRoom: JoinRoom,
    private val acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val featureFlagService: FeatureFlagService,
) : Presenter<SpaceState> {
    private var children by mutableStateOf<ImmutableList<SpaceRoom>>(persistentListOf())

    @Composable
    override fun present(): SpaceState {
        LaunchedEffect(Unit) {
            paginate()
            spaceRoomList.spaceRoomsFlow.collect { children = it.toImmutableList() }
        }

        val hideInvitesAvatar by client.rememberHideInvitesAvatar()
        val seenSpaceInvites by remember {
            seenInvitesStore.seenRoomIds().map { it.toImmutableSet() }
        }.collectAsState(persistentSetOf())

        val localCoroutineScope = rememberCoroutineScope()

        val hasMoreToLoad by remember {
            spaceRoomList.paginationStatusFlow.mapState { status ->
                when (status) {
                    is SpaceRoomList.PaginationStatus.Idle -> status.hasMoreToLoad
                    SpaceRoomList.PaginationStatus.Loading -> true
                }
            }
        }.collectAsState()

        val permissions by room.permissionsAsState(SpaceSettingsPermissions.DEFAULT) { perms ->
            perms.spaceSettingsPermissions()
        }
        val isSpaceSettingsEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.SpaceSettings)
        }.collectAsState(false)

        val roomInfo by room.roomInfoFlow.collectAsState()
        val canAccessSpaceSettings by remember {
            derivedStateOf { isSpaceSettingsEnabled && permissions.hasAny(roomInfo.joinRule) }
        }
        val currentSpace by spaceRoomList.currentSpaceFlow.collectAsState()
        val (joinActions, setJoinActions) = remember { mutableStateOf(emptyMap<RoomId, AsyncAction<Unit>>()) }

        var topicViewerState: TopicViewerState by remember { mutableStateOf(TopicViewerState.Hidden) }

        LaunchedEffect(children) {
            // Remove joined children from the join actions
            val joinedChildren = children
                .filter { it.state == CurrentUserMembership.JOINED }
                .map { it.roomId }
            setJoinActions(joinActions - joinedChildren)
        }

        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        fun handleEvent(event: SpaceEvents) {
            when (event) {
                SpaceEvents.LoadMore -> localCoroutineScope.paginate()
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
            }
        }
        return SpaceState(
            currentSpace = currentSpace.getOrNull(),
            children = children,
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            hasMoreToLoad = hasMoreToLoad,
            joinActions = joinActions.toImmutableMap(),
            acceptDeclineInviteState = acceptDeclineInviteState,
            topicViewerState = topicViewerState,
            canAccessSpaceSettings = canAccessSpaceSettings,
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

    private fun CoroutineScope.paginate() = launch {
        spaceRoomList.paginate()
    }
}
