/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.acceptdecline

import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents

sealed interface InternalAcceptDeclineInviteEvents : AcceptDeclineInviteEvents {
    data object ClearAcceptActionState : InternalAcceptDeclineInviteEvents
    data object ClearDeclineActionState : InternalAcceptDeclineInviteEvents
}
