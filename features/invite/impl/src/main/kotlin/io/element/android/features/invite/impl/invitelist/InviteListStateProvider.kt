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

package io.element.android.features.invite.impl.invitelist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.invite.impl.model.InviteListInviteSummary
import io.element.android.features.invite.impl.model.InviteSender
import io.element.android.features.invite.impl.response.AcceptDeclineInviteState
import io.element.android.features.invite.impl.response.AcceptDeclineInviteStateProvider
import io.element.android.features.invite.impl.response.anAcceptDeclineInviteState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class InviteListStateProvider : PreviewParameterProvider<InviteListState> {

    private val acceptDeclineInviteStateProvider = AcceptDeclineInviteStateProvider()

    override val values: Sequence<InviteListState>
        get() = sequenceOf(
            anInviteListState(),
            anInviteListState(inviteList = persistentListOf()),
        ) + acceptDeclineInviteStateProvider.values.map { acceptDeclineInviteState ->
            anInviteListState(acceptDeclineInviteState = acceptDeclineInviteState)
        }
}

internal fun anInviteListState(
    inviteList: ImmutableList<InviteListInviteSummary> = aInviteListInviteSummaryList(),
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    eventSink: (InviteListEvents) -> Unit = {}
) = InviteListState(
    inviteList = inviteList,
    acceptDeclineInviteState = acceptDeclineInviteState,
    eventSink = eventSink,
)

internal fun aInviteListInviteSummaryList(): ImmutableList<InviteListInviteSummary> {
    return persistentListOf(
        InviteListInviteSummary(
            roomId = RoomId("!id1:example.com"),
            roomName = "Room 1",
            roomAlias = "#room:example.org",
            sender = InviteSender(
                userId = UserId("@alice:example.org"),
                displayName = "Alice"
            ),
        ),
        InviteListInviteSummary(
            roomId = RoomId("!id2:example.com"),
            roomName = "Room 2",
            sender = InviteSender(
                userId = UserId("@bob:example.org"),
                displayName = "Bob"
            ),
        ),
        InviteListInviteSummary(
            roomId = RoomId("!id3:example.com"),
            roomName = "Alice",
            roomAlias = "@alice:example.com"
        ),
    )
}
