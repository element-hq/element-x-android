/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.RoomCoroutineScope
import io.element.android.libraries.matrix.api.room.BaseRoom
import kotlinx.coroutines.CoroutineScope

@BindingContainer
@ContributesTo(RoomScope::class)
object RoomModule {
    @RoomCoroutineScope
    @Provides
    fun providesSessionCoroutineScope(room: BaseRoom): CoroutineScope {
        return room.roomCoroutineScope
    }
}
