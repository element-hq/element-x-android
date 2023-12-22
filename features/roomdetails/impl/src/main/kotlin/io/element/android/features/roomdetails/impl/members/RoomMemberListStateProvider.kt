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

package io.element.android.features.roomdetails.impl.members

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import kotlinx.collections.immutable.persistentListOf

internal class RoomMemberListStateProvider : PreviewParameterProvider<RoomMemberListState> {
    override val values: Sequence<RoomMemberListState>
        get() = sequenceOf(
            aRoomMemberListState(
                roomMembers = Async.Success(
                    RoomMembers(
                        invited = persistentListOf(aVictor(), aWalter()),
                        joined = persistentListOf(anAlice(), aBob()),
                    )
                )
            ),
            aRoomMemberListState(roomMembers = Async.Loading()),
            aRoomMemberListState().copy(canInvite = true),
            aRoomMemberListState().copy(isSearchActive = false),
            aRoomMemberListState().copy(isSearchActive = true),
            aRoomMemberListState().copy(isSearchActive = true, searchQuery = "someone"),
            aRoomMemberListState().copy(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                searchResults = SearchBarResultState.Results(
                    RoomMembers(
                        invited = persistentListOf(aVictor()),
                        joined = persistentListOf(anAlice()),
                    )
                ),
            ),
            aRoomMemberListState().copy(
                isSearchActive = true,
                searchQuery = "something-with-no-results",
                searchResults = SearchBarResultState.NoResults()
            ),
        )
}

internal fun aRoomMemberListState(
    roomMembers: Async<RoomMembers> = Async.Uninitialized,
    searchResults: SearchBarResultState<RoomMembers> = SearchBarResultState.NotSearching(),
) = RoomMemberListState(
    roomMembers = roomMembers,
    searchQuery = "",
    searchResults = searchResults,
    isSearchActive = false,
    canInvite = false,
    eventSink = {}
)

fun aRoomMember(
    userId: UserId = UserId("@alice:server.org"),
    displayName: String? = null,
    avatarUrl: String? = null,
    membership: RoomMembershipState = RoomMembershipState.JOIN,
    isNameAmbiguous: Boolean = false,
    powerLevel: Long = 0L,
    normalizedPowerLevel: Long = 0L,
    isIgnored: Boolean = false,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    normalizedPowerLevel = normalizedPowerLevel,
    isIgnored = isIgnored,
)

fun aRoomMemberList() = persistentListOf(
    anAlice(),
    aBob(),
    aRoomMember(UserId("@carol:server.org"), "Carol"),
    aRoomMember(UserId("@david:server.org"), "David"),
    aRoomMember(UserId("@eve:server.org"), "Eve"),
    aRoomMember(UserId("@justin:server.org"), "Justin"),
    aRoomMember(UserId("@mallory:server.org"), "Mallory"),
    aRoomMember(UserId("@susie:server.org"), "Susie"),
    aVictor(),
    aWalter(),
)

fun anAlice() = aRoomMember(UserId("@alice:server.org"), "Alice")
fun aBob() = aRoomMember(UserId("@bob:server.org"), "Bob")

fun aVictor() = aRoomMember(UserId("@victor:server.org"), "Victor", membership = RoomMembershipState.INVITE)

fun aWalter() = aRoomMember(UserId("@walter:server.org"), "Walter", membership = RoomMembershipState.INVITE)
