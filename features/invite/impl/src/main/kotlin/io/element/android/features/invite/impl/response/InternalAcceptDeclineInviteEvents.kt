/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.invite.impl.response

import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.libraries.matrix.api.core.RoomId

sealed interface InternalAcceptDeclineInviteEvents : AcceptDeclineInviteEvents {
    data class ConfirmDeclineInvite(val roomId: RoomId) : InternalAcceptDeclineInviteEvents
    data object CancelDeclineInvite : InternalAcceptDeclineInviteEvents
    data object DismissAcceptError : InternalAcceptDeclineInviteEvents
    data object DismissDeclineError : InternalAcceptDeclineInviteEvents
}
