/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdetails.impl.members.moderation.aRoomMembersModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.persistentListOf

internal class RoomMemberListStateBannedProvider : PreviewParameterProvider<RoomMemberListState> {
    override val values: Sequence<RoomMemberListState>
        get() = sequenceOf(
            aRoomMemberListState(
                roomMembers = AsyncData.Success(
                    RoomMembers(
                        invited = persistentListOf(),
                        joined = persistentListOf(),
                        banned = persistentListOf(
                            aRoomMember(
                                userId = UserId("@alice:example.com"),
                                displayName = "Alice"
                            ).withIdentity(),
                            aRoomMember(
                                userId = UserId("@bob:example.com"),
                                displayName = "Bob"
                            ).withIdentity(),
                            aRoomMember(
                                userId = UserId("@charlie:example.com"),
                                displayName = "Charlie"
                            ).withIdentity(),
                        ),
                    )
                ),
                moderationState = aRoomMembersModerationState(canDisplayBannedUsers = true),
            ),
            aRoomMemberListState(
                roomMembers = AsyncData.Loading(
                    RoomMembers(
                        invited = persistentListOf(),
                        joined = persistentListOf(),
                        banned = persistentListOf(
                            aRoomMember(
                                userId = UserId("@alice:example.com"),
                                displayName = "Alice"
                            ).withIdentity(),
                            aRoomMember(
                                userId = UserId("@bob:example.com"),
                                displayName = "Bob"
                            ).withIdentity(),
                            aRoomMember(
                                userId = UserId("@charlie:example.com"),
                                displayName = "Charlie"
                            ).withIdentity(),
                        ),
                    )
                ),
                moderationState = aRoomMembersModerationState(canDisplayBannedUsers = true),
            ),
            aRoomMemberListState(
                roomMembers = AsyncData.Success(
                    RoomMembers(
                        invited = persistentListOf(),
                        joined = persistentListOf(),
                        banned = persistentListOf(),
                    )
                ),
                moderationState = aRoomMembersModerationState(canDisplayBannedUsers = true),
            )
        )
}
