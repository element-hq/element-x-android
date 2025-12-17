/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import io.element.android.features.invite.api.InviteData

sealed interface JoinRoomEvents {
    data object RetryFetchingContent : JoinRoomEvents
    data object DismissErrorAndHideContent : JoinRoomEvents
    data object JoinRoom : JoinRoomEvents
    data object KnockRoom : JoinRoomEvents
    data object ForgetRoom : JoinRoomEvents
    data class CancelKnock(val requiresConfirmation: Boolean) : JoinRoomEvents
    data class UpdateKnockMessage(val message: String) : JoinRoomEvents
    data object ClearActionStates : JoinRoomEvents
    data class AcceptInvite(val inviteData: InviteData) : JoinRoomEvents
    data class DeclineInvite(val inviteData: InviteData, val blockUser: Boolean) : JoinRoomEvents
}
