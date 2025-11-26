/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.map
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import kotlinx.collections.immutable.persistentListOf

internal class RoomMemberListStateProvider : PreviewParameterProvider<RoomMemberListState> {
    override val values: Sequence<RoomMemberListState>
        get() = sequenceOf(
            aRoomMemberListState(
                roomMembers = AsyncData.Loading(),
                selectedSection = SelectedSection.MEMBERS,
            ),
            aRoomMemberListState(
                roomMembers = AsyncData.Failure(Exception("Error details")),
                selectedSection = SelectedSection.MEMBERS,
            ),
            aRoomMemberListState(
                roomMembers = aLoadedRoomMembers(),
                selectedSection = SelectedSection.MEMBERS,
            ),
            aRoomMemberListState(
                roomMembers = aLoadedRoomMembers(),
                selectedSection = SelectedSection.BANNED,
                moderationState = aRoomMemberModerationState(canBan = true),
            ),
            aRoomMemberListState(
                roomMembers = aLoadedRoomMembers(),
                canInvite = true,
                selectedSection = SelectedSection.MEMBERS,
            ),
            aRoomMemberListState(
                roomMembers = aLoadedRoomMembers(),
                searchQuery = "alice",
                selectedSection = SelectedSection.MEMBERS,
            ),
            aRoomMemberListState(
                roomMembers = aLoadedRoomMembers(),
                searchQuery = "something-with-no-results",
                selectedSection = SelectedSection.MEMBERS,
            ),
        )
}

private fun aLoadedRoomMembers() = AsyncData.Success(
    RoomMembers(
        invited = persistentListOf(
            anInvitedVictor().withIdentity(),
            anInvitedWalter().withIdentity(),
        ),
        joined = persistentListOf(
            anAlice().withIdentity(identityState = IdentityState.Verified),
            aBob().withIdentity(identityState = IdentityState.PinViolation),
            aCarol().withIdentity(),
            aDavid().withIdentity(),
            anEve().withIdentity(identityState = IdentityState.VerificationViolation)
        ),
        banned = persistentListOf(
            aBannedMallory().withIdentity(),
            aBannedSusie().withIdentity()
        ),
    )
)

internal fun aRoomMemberListState(
    roomMembers: AsyncData<RoomMembers> = AsyncData.Loading(),
    moderationState: RoomMemberModerationState = aRoomMemberModerationState(),
    selectedSection: SelectedSection = SelectedSection.MEMBERS,
    searchQuery: String = "",
    canInvite: Boolean = false,
    eventSink: (RoomMemberListEvents) -> Unit = {},
) = RoomMemberListState(
    roomMembers = roomMembers,
    filteredRoomMembers = roomMembers.map { it.filter(searchQuery) },
    searchQuery = searchQuery,
    canInvite = canInvite,
    moderationState = moderationState,
    selectedSection = selectedSection,
    eventSink = eventSink
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
    isIgnored: Boolean = false,
    role: RoomMember.Role = RoomMember.Role.User,
    membershipChangeReason: String? = null,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    isIgnored = isIgnored,
    role = role,
    membershipChangeReason = membershipChangeReason,
)

fun aRoomMemberList() = persistentListOf(
    anAlice(),
    aBob(),
    aCarol(),
    aDavid(),
    anEve(),
    anInvitedVictor(),
    anInvitedWalter(),
    aBannedSusie(),
    aBannedMallory(),
)

fun anEve(): RoomMember = aRoomMember(UserId("@eve:server.org"), "Eve")

fun aDavid(): RoomMember = aRoomMember(UserId("@david:server.org"), "David")

fun aCarol(): RoomMember = aRoomMember(UserId("@carol:server.org"), "Carol")

fun anAlice() = aRoomMember(UserId("@alice:server.org"), "Alice", role = RoomMember.Role.Admin)
fun aBob() = aRoomMember(UserId("@bob:server.org"), "Bob", role = RoomMember.Role.Moderator)

fun anInvitedVictor() = aRoomMember(UserId("@victor:server.org"), "Victor", membership = RoomMembershipState.INVITE)

fun anInvitedWalter() = aRoomMember(UserId("@walter:server.org"), "Walter", membership = RoomMembershipState.INVITE)

fun aBannedSusie(): RoomMember = aRoomMember(UserId("@susie:server.org"), "Susie", membership = RoomMembershipState.BAN)

fun aBannedMallory(): RoomMember = aRoomMember(UserId("@mallory:server.org"), "Mallory", membership = RoomMembershipState.BAN)

private fun RoomMember.withIdentity(identityState: IdentityState? = null) = RoomMemberWithIdentityState(this, identityState)
