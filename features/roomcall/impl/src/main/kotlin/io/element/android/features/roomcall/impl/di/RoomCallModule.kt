/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomcall.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roomcall.impl.RoomCallStatePresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope

@ContributesTo(RoomScope::class)
@Module
interface RoomCallModule {
    @Binds
    fun bindRoomCallStatePresenter(presenter: RoomCallStatePresenter): Presenter<RoomCallState>
}
