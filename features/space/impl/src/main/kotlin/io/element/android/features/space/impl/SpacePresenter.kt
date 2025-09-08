/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.map

@Inject
class SpacePresenter(
    @Assisted private val params: SpaceEntryPoint.Params,
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
) : Presenter<SpaceState> {

    @AssistedFactory
    interface Factory {
        fun create(params: SpaceEntryPoint.Params): SpacePresenter
    }

    private val spaceRoomList = client.spaceService.spaceRoomList(params.roomId())

    @Composable
    override fun present(): SpaceState {
        val hideInvitesAvatar by remember {
            client
                .mediaPreviewService()
                .mediaPreviewConfigFlow
                .mapState { config -> config.hideInviteAvatar }
        }.collectAsState()
        val seenSpaceInvites by remember {
            seenInvitesStore.seenRoomIds().map { it.toPersistentSet() }
        }.collectAsState(persistentSetOf())

        val children by spaceRoomList.spaceRoomsFlow.collectAsState(emptyList())

        fun handleEvents(event: SpaceEvents) {
            //when (event) { }
        }

        return SpaceState(
            parentSpace = null,
            children = children.toPersistentList(),
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            eventSink = ::handleEvents,
        )
    }
}
