/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.api

import androidx.compose.runtime.Immutable

/**
 * State of the member moderation flow, which is the action sheet and the confirmation dialogs shown for a room member.
 */
@Immutable
interface RoomMemberModerationState {
    /** Which moderation actions the current user is allowed to perform, so the UI can hide or disable the rest. */
    val permissions: RoomMemberModerationPermissions

    /** Where the host screen sends its events to open the actions of a member or run one of them. */
    val eventSink: (RoomMemberModerationEvent) -> Unit
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
