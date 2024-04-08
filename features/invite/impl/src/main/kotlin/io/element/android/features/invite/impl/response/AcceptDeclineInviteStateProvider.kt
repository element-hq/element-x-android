/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.invite.impl.response

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
                    InviteData(RoomId(""), isDirect = true, roomName = "Alice"),
                ),
                declineAction = AsyncAction.Confirming,
            ),
            anAcceptDeclineInviteState(
                invite = Optional.of(
                    InviteData(RoomId(""), isDirect = false, roomName = "Some room"),
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
