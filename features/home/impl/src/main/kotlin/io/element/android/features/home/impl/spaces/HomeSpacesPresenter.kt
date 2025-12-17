/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.map

@Inject
class HomeSpacesPresenter(
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
) : Presenter<HomeSpacesState> {
    @Composable
    override fun present(): HomeSpacesState {
        val hideInvitesAvatar by client.rememberHideInvitesAvatar()
        val spaceRooms by remember {
            client.spaceService.spaceRoomsFlow.map { it.toImmutableList() }
        }.collectAsState(persistentListOf())

        val seenSpaceInvites by remember {
            seenInvitesStore.seenRoomIds().map { it.toImmutableSet() }
        }.collectAsState(persistentSetOf())

        fun handleEvent(event: HomeSpacesEvents) {
            // when (event) { }
        }

        return HomeSpacesState(
            space = CurrentSpace.Root,
            spaceRooms = spaceRooms,
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            eventSink = ::handleEvent,
        )
    }
}
