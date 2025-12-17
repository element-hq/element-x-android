/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import androidx.compose.runtime.Immutable

@Immutable
interface RoomMemberModerationState {
    val permissions: RoomMemberModerationPermissions
    val eventSink: (RoomMemberModerationEvents) -> Unit
}

data class ModerationActionState(
    val action: ModerationAction,
    val isEnabled: Boolean,
)

sealed interface ModerationAction {
    data object DisplayProfile : ModerationAction
    data object KickUser : ModerationAction
    data object BanUser : ModerationAction
    data object UnbanUser : ModerationAction
}
