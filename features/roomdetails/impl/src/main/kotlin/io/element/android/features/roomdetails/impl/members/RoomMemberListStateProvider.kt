/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import kotlinx.collections.immutable.persistentListOf

internal class RoomMemberListStateProvider : PreviewParameterProvider<RoomMemberListState> {
    override val values: Sequence<RoomMemberListState>
        get() = sequenceOf(
            aRoomMemberListState(
                roomMembers = AsyncData.Success(
                    RoomMembers(
                        invited = persistentListOf(aVictor().withIdentity(), aWalter().withIdentity()),
                        joined = persistentListOf(anAlice().withIdentity(), aBob().withIdentity(), aWalter().withIdentity()),
                        banned = persistentListOf(),
                    )
                )
            ),
            aRoomMemberListState(
                roomMembers = AsyncData.Success(
                    RoomMembers(
                        invited = persistentListOf(aVictor().withIdentity(), aWalter().withIdentity()),
                        joined = persistentListOf(
                            anAlice().withIdentity(identityState = IdentityState.Verified),
                            aBob().withIdentity(identityState = IdentityState.PinViolation),
                            aWalter().withIdentity(identityState = IdentityState.VerificationViolation)
                        ),
                        banned = persistentListOf(),
                    )
                )
            ),
            aRoomMemberListState(roomMembers = AsyncData.Loading()),
            aRoomMemberListState().copy(canInvite = true),
            aRoomMemberListState().copy(isSearchActive = false),
            aRoomMemberListState().copy(isSearchActive = true),
            aRoomMemberListState().copy(isSearchActive = true, searchQuery = "someone"),
            aRoomMemberListState().copy(
                isSearchActive = true,
                searchQuery = "@someone:matrix.org",
                searchResults = SearchBarResultState.Results(
                    AsyncData.Success(
                        RoomMembers(
                            invited = persistentListOf(aVictor().withIdentity()),
                            joined = persistentListOf(anAlice().withIdentity()),
                            banned = persistentListOf(),
                        )
                    )
                ),
            ),
            aRoomMemberListState().copy(
                isSearchActive = true,
                searchQuery = "something-with-no-results",
                searchResults = SearchBarResultState.NoResultsFound()
            ),
            aRoomMemberListState(
                roomMembers = AsyncData.Failure(Exception("Error details")),
            ),
        )
}

internal class RoomMemberListStateBannedProvider : PreviewParameterProvider<RoomMemberListState> {
    override val values: Sequence<RoomMemberListState>
        get() = sequenceOf(
            aRoomMemberListState(
                roomMembers = AsyncData.Success(
                    RoomMembers(
                        invited = persistentListOf(),
                        joined = persistentListOf(),
                        banned = persistentListOf(
                            aRoomMember(userId = UserId("@alice:example.com"), displayName = "Alice").withIdentity(),
                            aRoomMember(userId = UserId("@bob:example.com"), displayName = "Bob").withIdentity(),
                            aRoomMember(userId = UserId("@charlie:example.com"), displayName = "Charlie").withIdentity(),
                        ),
                    )
                ),
                moderationState = aRoomMemberModerationState(),
            ),
            aRoomMemberListState(
                roomMembers = AsyncData.Loading(
                    RoomMembers(
                        invited = persistentListOf(),
                        joined = persistentListOf(),
                        banned = persistentListOf(
                            aRoomMember(userId = UserId("@alice:example.com"), displayName = "Alice").withIdentity(),
                            aRoomMember(userId = UserId("@bob:example.com"), displayName = "Bob").withIdentity(),
                            aRoomMember(userId = UserId("@charlie:example.com"), displayName = "Charlie").withIdentity(),
                        ),
                    )
                ),
                moderationState = aRoomMemberModerationState(),
            ),
            aRoomMemberListState(
                roomMembers = AsyncData.Success(
                    RoomMembers(
                        invited = persistentListOf(),
                        joined = persistentListOf(),
                        banned = persistentListOf(),
                    )
                ),
                moderationState = aRoomMemberModerationState(),
            )
        )
}

internal fun aRoomMemberListState(
    roomMembers: AsyncData<RoomMembers> = AsyncData.Loading(),
    searchResults: SearchBarResultState<AsyncData<RoomMembers>> = SearchBarResultState.Initial(),
    moderationState: RoomMemberModerationState = aRoomMemberModerationState(),
) = RoomMemberListState(
    roomMembers = roomMembers,
    searchQuery = "",
    searchResults = searchResults,
    isSearchActive = false,
    canInvite = false,
    moderationState = moderationState,
    eventSink = {}
)

fun aRoomMemberModerationState(
    canBan: Boolean = false,
    canKick: Boolean = false,
): RoomMemberModerationState {
    return object : RoomMemberModerationState {
        override val canKick: Boolean = canKick
        override val canBan: Boolean = canBan
        override val eventSink: (RoomMemberModerationEvents) -> Unit = {}
    }
}

fun aRoomMember(
    userId: UserId = UserId("@alice:server.org"),
    displayName: String? = null,
    avatarUrl: String? = null,
    membership: RoomMembershipState = RoomMembershipState.JOIN,
    isNameAmbiguous: Boolean = false,
    powerLevel: Long = 0L,
    normalizedPowerLevel: Long = 0L,
    isIgnored: Boolean = false,
    role: RoomMember.Role = RoomMember.Role.USER,
    membershipChangeReason: String? = null,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    normalizedPowerLevel = normalizedPowerLevel,
    isIgnored = isIgnored,
    role = role,
    membershipChangeReason = membershipChangeReason,
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

fun anAlice() = aRoomMember(UserId("@alice:server.org"), "Alice", role = RoomMember.Role.ADMIN)
fun aBob() = aRoomMember(UserId("@bob:server.org"), "Bob", role = RoomMember.Role.MODERATOR)

fun aVictor() = aRoomMember(UserId("@victor:server.org"), "Victor", membership = RoomMembershipState.INVITE)

fun aWalter() = aRoomMember(UserId("@walter:server.org"), "Walter", membership = RoomMembershipState.INVITE)

private fun RoomMember.withIdentity(identityState: IdentityState? = null) = RoomMemberWithIdentityState(this, identityState)
