/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.invite.api.response

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId

open class AcceptDeclineInviteStateProvider : PreviewParameterProvider<AcceptDeclineInviteState> {
    override val values: Sequence<AcceptDeclineInviteState>
        get() = sequenceOf(
            anAcceptDeclineInviteState(),
            anAcceptDeclineInviteState(
                declineAction = AsyncAction.Confirming(
                    InviteData(RoomId("!room:matrix.org"), isDm = true, roomName = "Alice")
                ),
            ),
            anAcceptDeclineInviteState(
                declineAction = AsyncAction.Confirming(
                    InviteData(RoomId("!room:matrix.org"), isDm = false, roomName = "Some room")
                ),
            ),
            anAcceptDeclineInviteState(
                acceptAction = AsyncAction.Failure(Throwable("Whoops")),
            ),
            anAcceptDeclineInviteState(
                declineAction = AsyncAction.Failure(Throwable("Whoops")),
            ),
        )
}

fun anAcceptDeclineInviteState(
    acceptAction: AsyncAction<Unit, RoomId> = AsyncAction.Uninitialized,
    declineAction: AsyncAction<InviteData, RoomId> = AsyncAction.Uninitialized,
    eventSink: (AcceptDeclineInviteEvents) -> Unit = {}
) = AcceptDeclineInviteState(
    acceptAction = acceptAction,
    declineAction = declineAction,
    eventSink = eventSink,
)
