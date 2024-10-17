/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.di

import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom

@Module
@ContributesTo(RoomScope::class)
object RoomMemberModule {
    @Provides
    fun provideRoomMemberDetailsPresenterFactory(
        room: MatrixRoom,
        userProfilePresenterFactory: UserProfilePresenterFactory,
    ): RoomMemberDetailsPresenter.Factory {
        return object : RoomMemberDetailsPresenter.Factory {
            override fun create(roomMemberId: UserId): RoomMemberDetailsPresenter {
                return RoomMemberDetailsPresenter(
                    roomMemberId = roomMemberId,
                    room = room,
                    userProfilePresenterFactory = userProfilePresenterFactory,
                )
            }
        }
    }
}
