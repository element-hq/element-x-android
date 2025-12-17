/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import io.element.android.libraries.matrix.api.user.MatrixUser

interface RoomMemberModerationEvents {
    data class ShowActionsForUser(val user: MatrixUser) : RoomMemberModerationEvents
    data class ProcessAction(val action: ModerationAction, val targetUser: MatrixUser) : RoomMemberModerationEvents
}
