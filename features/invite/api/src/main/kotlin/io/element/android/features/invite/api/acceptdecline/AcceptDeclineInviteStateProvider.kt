/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.api.acceptdecline

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

fun anAcceptDeclineInviteState(
    acceptAction: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    declineAction: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    eventSink: (AcceptDeclineInviteEvents) -> Unit = {},
) = AcceptDeclineInviteState(
    acceptAction = acceptAction,
    declineAction = declineAction,
    eventSink = eventSink,
)
