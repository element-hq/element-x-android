/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.invite

import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface RoomInviteMembersEvents {
    data class ToggleUser(val user: MatrixUser) : RoomInviteMembersEvents
    data class UpdateSearchQuery(val query: String) : RoomInviteMembersEvents
    data class OnSearchActiveChanged(val active: Boolean) : RoomInviteMembersEvents
}
