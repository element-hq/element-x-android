/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appnav.di.RoomComponentFactory
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultRoomComponentFactory @Inject constructor(
    private val roomComponentBuilder: RoomComponent.Builder
) : RoomComponentFactory {
    override fun create(room: JoinedRoom): Any {
        return roomComponentBuilder
            .joinedRoom(room)
            .baseRoom(room)
            .build()
    }
}
