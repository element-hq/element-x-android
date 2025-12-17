/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.api

import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom

interface InvitePeoplePresenter : Presenter<InvitePeopleState> {
    interface Factory {
        fun create(
            joinedRoom: JoinedRoom?,
            roomId: RoomId,
        ): InvitePeoplePresenter
    }
}
