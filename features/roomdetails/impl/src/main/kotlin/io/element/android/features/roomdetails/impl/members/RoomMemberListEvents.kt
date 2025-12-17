/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import io.element.android.libraries.matrix.api.room.RoomMember

sealed interface RoomMemberListEvents {
    data class ChangeSelectedSection(val section: SelectedSection) : RoomMemberListEvents
    data class UpdateSearchQuery(val query: String) : RoomMemberListEvents
    data class RoomMemberSelected(val roomMember: RoomMember) : RoomMemberListEvents
}
