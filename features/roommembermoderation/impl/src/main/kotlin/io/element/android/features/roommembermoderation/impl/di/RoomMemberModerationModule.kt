/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.features.roommembermoderation.impl.RoomMemberModerationPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope

@ContributesTo(RoomScope::class)
@Module
interface RoomMemberModerationModule {
    @Binds
    fun bindRoomMemberModerationPresenter(presenter: RoomMemberModerationPresenter): Presenter<RoomMemberModerationState>
}
