/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import io.element.android.libraries.matrix.api.user.MatrixUser

interface RoomMemberModerationState {
    val canKick: Boolean
    val canBan: Boolean
    val eventSink: (RoomMemberModerationEvents) -> Unit
}

sealed interface ModerationAction {
    data class DisplayProfile(val user: MatrixUser) : ModerationAction
    data class KickUser(val user: MatrixUser) : ModerationAction
    data class BanUser(val user: MatrixUser) : ModerationAction
    data class UnbanUser(val user: MatrixUser) : ModerationAction
}
