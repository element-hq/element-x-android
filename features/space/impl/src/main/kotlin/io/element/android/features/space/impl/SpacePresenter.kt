/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Inject
class SpacePresenter(
    @Assisted private val inputs: SpaceEntryPoint.Inputs,
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
) : Presenter<SpaceState> {
    @AssistedFactory
    interface Factory {
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

        fun handleEvents(event: SpaceEvents) {
            when (event) {
                SpaceEvents.LoadMore -> coroutineScope.paginate()
            }
        }
        return SpaceState(
            currentSpace = currentSpace,
            children = children.toPersistentList(),
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            hasMoreToLoad = hasMoreToLoad,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.paginate() = launch {
        spaceRoomList.paginate()
    }
}
