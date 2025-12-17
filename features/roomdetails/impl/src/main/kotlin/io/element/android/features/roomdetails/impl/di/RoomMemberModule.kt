/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.room.JoinedRoom

@BindingContainer
@ContributesTo(RoomScope::class)
object RoomMemberModule {
    @Provides
    fun provideRoomMemberDetailsPresenterFactory(
        room: JoinedRoom,
        userProfilePresenterFactory: UserProfilePresenterFactory,
        encryptionService: EncryptionService,
        clipboardHelper: ClipboardHelper,
    ): RoomMemberDetailsPresenter.Factory {
        return object : RoomMemberDetailsPresenter.Factory {
            override fun create(roomMemberId: UserId): RoomMemberDetailsPresenter {
                return RoomMemberDetailsPresenter(
                    roomMemberId = roomMemberId,
                    room = room,
                    userProfilePresenterFactory = userProfilePresenterFactory,
                    encryptionService = encryptionService,
                    clipboardHelper = clipboardHelper,
                )
            }
        }
    }
}
