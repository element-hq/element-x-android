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
    /**
     * Creates a presenter for one invite flow.
     */
    interface Factory {
        /**
         * @param joinedRoom the room to invite into, or `null` when the room does not exist yet, as when inviting from a direct message.
         * @param roomId the id of the room to invite into.
         */
        fun create(
            joinedRoom: JoinedRoom?,
            roomId: RoomId,
        ): InvitePeoplePresenter
    }
}
