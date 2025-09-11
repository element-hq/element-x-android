/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.map

@Inject
class HomeSpacesPresenter(
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
) : Presenter<HomeSpacesState> {
    @Composable
    override fun present(): HomeSpacesState {
        val hideInvitesAvatar by client.rememberHideInvitesAvatar()
        val spaceRooms by client.spaceService.spaceRoomsFlow.collectAsState(emptyList())
        val seenSpaceInvites by remember {
            seenInvitesStore.seenRoomIds().map { it.toPersistentSet() }
        }.collectAsState(persistentSetOf())

        fun handleEvents(event: HomeSpacesEvents) {
            // when (event) { }
        }

        return HomeSpacesState(
            space = CurrentSpace.Root,
            spaceRooms = spaceRooms,
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            eventSink = ::handleEvents,
        )
    }
}
