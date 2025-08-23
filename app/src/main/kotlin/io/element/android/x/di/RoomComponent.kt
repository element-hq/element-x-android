/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.element.android.libraries.architecture.NodeFactoriesBindings
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.JoinedRoom

@GraphExtension(RoomScope::class)
interface RoomComponent : NodeFactoriesBindings {
    @ContributesTo(SessionScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(
            @Provides joinedRoom: JoinedRoom,
            @Provides baseRoom: BaseRoom
        ): RoomComponent
    }
}
