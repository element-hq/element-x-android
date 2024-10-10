/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.invite.api.response

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

data class AcceptDeclineInviteState(
    val acceptAction: AsyncAction<Unit, RoomId>,
    val declineAction: AsyncAction<InviteData, RoomId>,
    val eventSink: (AcceptDeclineInviteEvents) -> Unit,
)
