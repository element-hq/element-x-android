/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.invite.impl.model

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId

open class InviteListInviteSummaryProvider : PreviewParameterProvider<InviteListInviteSummary> {
    override val values: Sequence<InviteListInviteSummary>
        get() = sequenceOf(
            aInviteListInviteSummary(),
            aInviteListInviteSummary().copy(roomAlias = "#someroom-with-a-long-alias:example.com"),
            aInviteListInviteSummary().copy(roomAlias = "#someroom-with-a-long-alias:example.com", isNew = true),
            aInviteListInviteSummary().copy(roomName = "Alice", sender = null),
            aInviteListInviteSummary().copy(isNew = true)
        )
}

fun aInviteListInviteSummary() = InviteListInviteSummary(
    roomId = RoomId("!room1:example.com"),
    roomName = "Some room with a long name that will truncate",
    sender = InviteSender(
        userId = UserId("@alice-with-a-long-mxid:example.org"),
        displayName = "Alice with a long name"
    ),
)
