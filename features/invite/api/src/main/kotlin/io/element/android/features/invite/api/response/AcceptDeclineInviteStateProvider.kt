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
import java.util.Optional

open class AcceptDeclineInviteStateProvider : PreviewParameterProvider<AcceptDeclineInviteState> {
    override val values: Sequence<AcceptDeclineInviteState>
        get() = sequenceOf(
            anAcceptDeclineInviteState(),
            anAcceptDeclineInviteState(
                invite = Optional.of(
                    InviteData(RoomId("!room:matrix.org"), isDm = true, roomName = "Alice"),
                ),
                declineAction = AsyncAction.Confirming,
            ),
            anAcceptDeclineInviteState(
                invite = Optional.of(
                    InviteData(RoomId("!room:matrix.org"), isDm = false, roomName = "Some room"),
                ),
                declineAction = AsyncAction.Confirming,
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
    invite: Optional<InviteData> = Optional.empty(),
    acceptAction: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    declineAction: AsyncAction<RoomId> = AsyncAction.Uninitialized,
    eventSink: (AcceptDeclineInviteEvents) -> Unit = {}
) = AcceptDeclineInviteState(
    invite = invite,
    acceptAction = acceptAction,
    declineAction = declineAction,
    eventSink = eventSink,
)
