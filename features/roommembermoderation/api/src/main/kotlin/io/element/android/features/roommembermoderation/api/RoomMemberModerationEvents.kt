/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import io.element.android.libraries.matrix.api.room.RoomMember

interface RoomMemberModerationEvents {
    data class RenderActions(val roomMember: RoomMember) : RoomMemberModerationEvents
    data class ProcessAction(val action: ModerationAction): RoomMemberModerationEvents
}
