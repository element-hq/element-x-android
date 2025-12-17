/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.acceptdecline

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.ConfirmingDeclineInvite
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.features.invite.impl.AcceptInvite
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

open class AcceptDeclineInviteStateProvider : PreviewParameterProvider<AcceptDeclineInviteState> {
    override val values: Sequence<AcceptDeclineInviteState>
        get() = sequenceOf(
            anAcceptDeclineInviteState(),
            anAcceptDeclineInviteState(
                declineAction = ConfirmingDeclineInvite(
                    InviteData(
                        roomId = RoomId("!room:matrix.org"),
                        isDm = true,
                        roomName = "Alice"
                    ),
                    blockUser = false,
                ),
            ),
            anAcceptDeclineInviteState(
                declineAction = ConfirmingDeclineInvite(
                    InviteData(
                        roomId = RoomId("!room:matrix.org"),
                        isDm = true,
                        roomName = "Alice"
                    ),
                    blockUser = true,
                ),
            ),
            anAcceptDeclineInviteState(
                acceptAction = AsyncAction.Failure(RuntimeException("Error while accepting invite")),
            ),
            anAcceptDeclineInviteState(
                acceptAction = AsyncAction.Failure(AcceptInvite.Failures.InvalidInvite),
            ),
            anAcceptDeclineInviteState(
                declineAction = AsyncAction.Failure(RuntimeException("Error while declining invite")),
            ),
        )
}
