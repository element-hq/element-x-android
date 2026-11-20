/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvent

sealed interface InternalRoomMemberModerationEvent : RoomMemberModerationEvent {
    data class DoKickUser(val reason: String) : InternalRoomMemberModerationEvent
    data class DoBanUser(val reason: String) : InternalRoomMemberModerationEvent
    data class DoUnbanUser(val reason: String) : InternalRoomMemberModerationEvent
    data object Reset : InternalRoomMemberModerationEvent
}
