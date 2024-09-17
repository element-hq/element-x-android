/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.member

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher.Source.CACHE
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher.Source.CACHE_AND_SERVER
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher.Source.SERVER
import io.element.android.libraries.matrix.impl.sdk.FakeRoomInterface
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.MembershipState
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomMembersIterator
import uniffi.matrix_sdk.RoomMemberRole

class RoomMemberListFetcherTest {
    @Test
    fun `fetchRoomMembers with CACHE source - emits cached members, if any`() = runTest {
        val room = FakeRoomInterface(getMembersNoSync = {
            FakeRoomMembersIterator(
                listOf(
                    fakeRustRoomMember(A_USER_ID),
                    fakeRustRoomMember(A_USER_ID_2),
                    fakeRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)

            fetcher.fetchRoomMembers(source = CACHE)

            // Loading state
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)

            val cachedItemsState = awaitItem()
            assertThat(cachedItemsState).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
            assertThat((cachedItemsState as? MatrixRoomMembersState.Ready)?.roomMembers).hasSize(3)

            // Assert only the 'no sync' method was called, so no new member sync happened
            assertThat(room.membersNoSyncCallCount).isEqualTo(1)
            assertThat(room.membersCallCount).isEqualTo(0)
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE source - emits empty list, if no members exist`() = runTest {
        val room = FakeRoomInterface(getMembersNoSync = {
            FakeRoomMembersIterator(emptyList())
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            assertThat((awaitItem() as? MatrixRoomMembersState.Ready)?.roomMembers).isEmpty()
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE source - emits Error on error found`() = runTest {
        val room = FakeRoomInterface(getMembersNoSync = {
            error("Some unexpected issue")
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Error::class.java)
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE source - emits all items at once`() = runTest {
        val room = FakeRoomInterface(getMembersNoSync = {
            FakeRoomMembersIterator(
                listOf(
                    fakeRustRoomMember(A_USER_ID),
                    fakeRustRoomMember(A_USER_ID_2),
                    fakeRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default, pageSize = 2)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE)

            // Initial state
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            // Started loading cached members
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            // Finished loading cached members
            assertThat((awaitItem() as? MatrixRoomMembersState.Ready)?.roomMembers).hasSize(3)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `fetchRoomMembers with SERVER source - emits only new members, if any`() = runTest {
        val room = FakeRoomInterface(getMembers = {
            FakeRoomMembersIterator(
                listOf(
                    fakeRustRoomMember(A_USER_ID),
                    fakeRustRoomMember(A_USER_ID_2),
                    fakeRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = SERVER)

            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            assertThat((awaitItem() as? MatrixRoomMembersState.Ready)?.roomMembers?.size).isEqualTo(3)

            // Assert only the 'sync' method was called, so a new member sync happened
            assertThat(room.membersNoSyncCallCount).isEqualTo(0)
            assertThat(room.membersCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `fetchRoomMembers with SERVER source - on error it emits an Error item`() = runTest {
        val room = FakeRoomInterface(getMembers = { error("An unexpected error") })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = SERVER)

            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Error::class.java)
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE_AND_SERVER source - returns cached items first, then new ones`() = runTest {
        val room = FakeRoomInterface(
            getMembersNoSync = {
                FakeRoomMembersIterator(listOf(fakeRustRoomMember(A_USER_ID_4)))
            },
            getMembers = {
                FakeRoomMembersIterator(
                    listOf(
                        fakeRustRoomMember(A_USER_ID),
                        fakeRustRoomMember(A_USER_ID_2),
                        fakeRustRoomMember(A_USER_ID_3),
                    )
                )
            }
        )

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE_AND_SERVER)
            // Initial
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            // Loading cached
            awaitItem().let { pending ->
                assertThat(pending).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
                assertThat(pending.roomMembers()).isEmpty()
            }
            // Loaded cached
            awaitItem().let { cached ->
                assertThat(cached).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
                assertThat(cached.roomMembers()).hasSize(1)
            }
            // Start loading new
            awaitItem().let { ready ->
                assertThat(ready).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
                assertThat(ready.roomMembers()).hasSize(3)
            }

            // Assert both member methods were called, so both the cache was hit and a new member sync happened
            assertThat(room.membersNoSyncCallCount).isEqualTo(1)
            assertThat(room.membersCallCount).isEqualTo(1)
        }
    }
}

class FakeRoomMembersIterator(
    private var members: List<RoomMember>? = null
) : RoomMembersIterator(NoPointer) {
    override fun len(): UInt {
        return members?.size?.toUInt() ?: 0u
    }

    override fun nextChunk(chunkSize: UInt): List<RoomMember>? {
        if (members?.isEmpty() == true) {
            return null
        }
        return members?.let {
            val result = it.take(chunkSize.toInt())
            members = it.subList(result.size, it.size)
            result
        }
    }
}

private fun fakeRustRoomMember(
    userId: UserId,
    displayName: String? = null,
    avatarUrl: String? = null,
    membership: MembershipState = MembershipState.JOIN,
    isNameAmbiguous: Boolean = false,
    powerLevel: Long = 0L,
    isIgnored: Boolean = false,
    role: RoomMemberRole = RoomMemberRole.USER,
) = RoomMember(
    userId = userId.value,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    normalizedPowerLevel = powerLevel,
    isIgnored = isIgnored,
    suggestedRoleForPowerLevel = role,
)
