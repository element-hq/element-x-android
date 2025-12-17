/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomcall.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roomcall.impl.RoomCallStatePresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope

@ContributesTo(RoomScope::class)
@BindingContainer
interface RoomCallModule {
    @Binds
    fun bindRoomCallStatePresenter(presenter: RoomCallStatePresenter): Presenter<RoomCallState>
}
