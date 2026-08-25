/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.api.acceptdecline

import io.element.android.features.invite.api.InviteData

/**
 * Events sent to the accept/decline invite presenter, from whichever screen is showing the invitation.
 */
interface AcceptDeclineInviteEvent {
    data class AcceptInvite(val invite: InviteData) : AcceptDeclineInviteEvent
    data class DeclineInvite(val invite: InviteData, val blockUser: Boolean, val shouldConfirm: Boolean) : AcceptDeclineInviteEvent
}
