/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.invite.impl.response

import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents

sealed interface InternalAcceptDeclineInviteEvents : AcceptDeclineInviteEvents {
    data object ConfirmDeclineInvite : InternalAcceptDeclineInviteEvents
    data object CancelDeclineInvite : InternalAcceptDeclineInviteEvents
    data object DismissAcceptError : InternalAcceptDeclineInviteEvents
    data object DismissDeclineError : InternalAcceptDeclineInviteEvents
}
