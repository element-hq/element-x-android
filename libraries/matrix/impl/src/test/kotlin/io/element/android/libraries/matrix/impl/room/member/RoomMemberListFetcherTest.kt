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

package io.element.android.libraries.matrix.impl.room.member

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.MembershipState
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomMembersIterator
import uniffi.matrix_sdk.RoomMemberRole

class RoomMemberListFetcherTest {
    @Test
    fun `fetchCachedRoomMembers - emits cached members, if any`() = runTest {
        val room = FakeRustRoom(getMembersNoSync = {
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

            fetcher.fetchCachedRoomMembers()

            val cachedItemsState = awaitItem()
            assertThat(cachedItemsState).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            assertThat((cachedItemsState as? MatrixRoomMembersState.Pending)?.prevRoomMembers).hasSize(3)
        }
    }

    @Test
    fun `fetchCachedRoomMembers - emits empty list, if no members exist`() = runTest {
        val room = FakeRustRoom(getMembersNoSync = {
            FakeRoomMembersIterator(emptyList())
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchCachedRoomMembers()
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem().roomMembers()).isEmpty()
        }
    }

    @Test
    fun `fetchCachedRoomMembers - emits Error on error found`() = runTest {
        val room = FakeRustRoom(getMembersNoSync = {
            error("Some unexpected issue")
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchCachedRoomMembers()
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Error::class.java)
        }
    }

    @Test
    fun `fetchCachedRoomMembers - emits all items at once`() = runTest {
        val room = FakeRustRoom(getMembersNoSync = {
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
            fetcher.fetchCachedRoomMembers()

            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat((awaitItem() as? MatrixRoomMembersState.Pending)?.prevRoomMembers).hasSize(3)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `fetchRoomMembers - with 'withCache' set to false emits only new members, if any`() = runTest {
        val room = FakeRustRoom(getMembers = {
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
            fetcher.fetchRoomMembers(withCache = false)

            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            assertThat((awaitItem() as? MatrixRoomMembersState.Ready)?.roomMembers?.size).isEqualTo(3)
        }
    }

    @Test
    fun `fetchRoomMembers - on error it emits an Error item`() = runTest {
        val room = FakeRustRoom(getMembers = { error("An unexpected error") })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(withCache = false)

            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
            assertThat(awaitItem()).isInstanceOf(MatrixRoomMembersState.Error::class.java)
        }
    }

    @Test
    fun `fetchRoomMembers - with 'withCache' returns cached items first, then new ones`() = runTest {
        val room = FakeRustRoom(
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
            fetcher.fetchRoomMembers(withCache = true)
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
        }
    }
}

class FakeRustRoom(
    private val getMembers: () -> RoomMembersIterator = { FakeRoomMembersIterator() },
    private val getMembersNoSync: () -> RoomMembersIterator = { FakeRoomMembersIterator() },
) : Room(NoPointer) {
    override fun id(): String {
        return A_ROOM_ID.value
    }

    override suspend fun members(): RoomMembersIterator {
        return getMembers()
    }

    override suspend fun membersNoSync(): RoomMembersIterator {
        return getMembersNoSync()
    }

    override fun close() {
        // No-op
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
