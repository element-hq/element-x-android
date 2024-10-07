/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationPresenter
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope

@Module
@ContributesTo(RoomScope::class)
interface RoomDetailsModule {
    @Binds
    fun bindRoomMembersModerationPresenter(presenter: RoomMembersModerationPresenter): Presenter<RoomMembersModerationState>
}
