/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import io.element.android.features.invite.api.InviteData

sealed interface JoinRoomEvent {
    data object RetryFetchingContent : JoinRoomEvent
    data object DismissErrorAndHideContent : JoinRoomEvent
    data object JoinRoom : JoinRoomEvent
    data object KnockRoom : JoinRoomEvent
    data object ForgetRoom : JoinRoomEvent
    data class CancelKnock(val requiresConfirmation: Boolean) : JoinRoomEvent
    data class UpdateKnockMessage(val message: String) : JoinRoomEvent
    data object ClearActionStates : JoinRoomEvent
    data class AcceptInvite(val inviteData: InviteData) : JoinRoomEvent
    data class DeclineInvite(val inviteData: InviteData, val blockUser: Boolean) : JoinRoomEvent
}
