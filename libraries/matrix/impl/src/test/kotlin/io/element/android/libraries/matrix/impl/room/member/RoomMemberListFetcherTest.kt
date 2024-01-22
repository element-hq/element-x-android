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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.MembershipState
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomMembersIterator

@OptIn(ExperimentalCoroutinesApi::class)
class RoomMemberListFetcherTest {
    @Test
    fun `getCachedRoomMembers - emits cached members, if any`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(getMembersNoSync = {
            FakeRoomMembersIterator(
                listOf(
                    FakeRustRoomMember(A_USER_ID),
                    FakeRustRoomMember(A_USER_ID_2),
                    FakeRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        val asyncMembers = async { fetcher.membersFlow.take(2).toList() }
        runCurrent()
        fetcher.getCachedRoomMembers()
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(2)
        assertThat(memberStates[0]).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
        assertThat(memberStates[1]).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
        assertThat((memberStates[1] as? MatrixRoomMembersState.Ready)?.roomMembers?.size).isEqualTo(3)
    }

    @Test
    fun `getCachedRoomMembers - emits empty list, if no members exist`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(getMembersNoSync = {
            FakeRoomMembersIterator(emptyList())
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        val asyncMembers = async { fetcher.membersFlow.take(2).toList() }
        runCurrent()
        fetcher.getCachedRoomMembers()
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(2)
        assertThat(memberStates[0]).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
        assertThat(memberStates[1].roomMembers()).isEmpty()
    }

    @Test
    fun `getCachedRoomMembers - emits Error on error found`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(getMembersNoSync = {
            error("Some unexpected issue")
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        val asyncMembers = async { fetcher.membersFlow.take(2).toList() }
        runCurrent()
        fetcher.getCachedRoomMembers()
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(2)
        assertThat(memberStates.last()).isInstanceOf(MatrixRoomMembersState.Error::class.java)
    }

    @Test
    fun `getCachedRoomMembers - emits items using page size`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(getMembersNoSync = {
            FakeRoomMembersIterator(
                listOf(
                    FakeRustRoomMember(A_USER_ID),
                    FakeRustRoomMember(A_USER_ID_2),
                    FakeRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default, pageSize = 2)
        val asyncMembers = async { fetcher.membersFlow.take(3).toList() }
        runCurrent()
        fetcher.getCachedRoomMembers()
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(3)
        assertThat(memberStates[0]).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
        assertThat(memberStates.drop(1).all { it is MatrixRoomMembersState.Ready }).isTrue()
        assertThat((memberStates[1] as? MatrixRoomMembersState.Ready)?.roomMembers?.size).isEqualTo(2)
        assertThat((memberStates[2] as? MatrixRoomMembersState.Ready)?.roomMembers?.size).isEqualTo(3)
    }

    @Test
    fun `getUpdatedRoomMembers - with 'withCache' set to false emits only new members, if any`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(getMembers = {
            FakeRoomMembersIterator(
                listOf(
                    FakeRustRoomMember(A_USER_ID),
                    FakeRustRoomMember(A_USER_ID_2),
                    FakeRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        val asyncMembers = async { fetcher.membersFlow.take(3).toList() }
        runCurrent()
        fetcher.getUpdatedRoomMembers(withCache = false)
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(3)
        assertThat(memberStates[0]).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
        assertThat(memberStates[1]).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
        assertThat(memberStates[2]).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
        assertThat((memberStates[2] as? MatrixRoomMembersState.Ready)?.roomMembers?.size).isEqualTo(3)
    }

    @Test
    fun `getUpdatedRoomMembers - on error it emits an Error item`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(getMembers = { error("An unexpected error") })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        val asyncMembers = async { fetcher.membersFlow.take(3).toList() }
        runCurrent()
        fetcher.getUpdatedRoomMembers(withCache = false)
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(3)
        assertThat(memberStates[0]).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
        assertThat(memberStates[1]).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
        assertThat(memberStates[2]).isInstanceOf(MatrixRoomMembersState.Error::class.java)
    }

    @Test
    fun `getUpdatedRoomMembers - with 'withCache' returns cached items first, then new ones`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(
            getMembersNoSync = {
                FakeRoomMembersIterator(listOf(FakeRustRoomMember(A_USER_ID_4)))
            },
            getMembers = {
                FakeRoomMembersIterator(
                    listOf(
                        FakeRustRoomMember(A_USER_ID),
                        FakeRustRoomMember(A_USER_ID_2),
                        FakeRustRoomMember(A_USER_ID_3),
                    )
                )
            }
        )

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        val asyncMembers = async { fetcher.membersFlow.take(4).toList() }
        runCurrent()
        fetcher.getUpdatedRoomMembers(withCache = true)
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(4)
        // Initial
        assertThat(memberStates[0]).isInstanceOf(MatrixRoomMembersState.Unknown::class.java)
        // Loaded cached
        assertThat(memberStates[1]).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
        assertThat(memberStates[1].roomMembers()).hasSize(1)
        // Start loading new
        assertThat(memberStates[2]).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
        assertThat(memberStates[3]).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
        assertThat(memberStates[3].roomMembers()).hasSize(3)
    }

    @Test
    fun `getUpdatedRoomMembers - with 'withCache' skips cache if there is already a ready state`() = runTest(StandardTestDispatcher()) {
        val room = FakeRustRoom(
            getMembersNoSync = {
                FakeRoomMembersIterator(listOf(FakeRustRoomMember(A_USER_ID_4)))
            },
            getMembers = {
                FakeRoomMembersIterator(
                    listOf(
                        FakeRustRoomMember(A_USER_ID),
                        FakeRustRoomMember(A_USER_ID_2),
                        FakeRustRoomMember(A_USER_ID_3),
                    )
                )
            }
        )

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        // Set a ready state
        fetcher.getUpdatedRoomMembers(withCache = false)

        val asyncMembers = async { fetcher.membersFlow.take(3).toList() }
        runCurrent()
        // Start loading new members
        fetcher.getUpdatedRoomMembers(withCache = true)
        val memberStates = asyncMembers.await()

        assertThat(memberStates).hasSize(3)
        // Previous ready state
        assertThat(memberStates[0]).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
        // New pending state
        assertThat(memberStates[1]).isInstanceOf(MatrixRoomMembersState.Pending::class.java)
        // New ready state
        assertThat(memberStates[2]).isInstanceOf(MatrixRoomMembersState.Ready::class.java)
        assertThat(memberStates[2].roomMembers()).hasSize(3)
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

class FakeRustRoomMember(
    private val userId: UserId,
    private val displayName: String? = null,
    private val avatarUrl: String? = null,
    private val membership: MembershipState = MembershipState.JOIN,
    private val isNameAmbiguous: Boolean = false,
    private val powerLevel: Long = 0L,
) : RoomMember(NoPointer) {
    override fun userId(): String {
        return userId.value
    }

    override fun displayName(): String? {
        return displayName
    }

    override fun avatarUrl(): String? {
        return avatarUrl
    }

    override fun membership(): MembershipState {
        return membership
    }

    override fun isNameAmbiguous(): Boolean {
        return isNameAmbiguous
    }

    override fun powerLevel(): Long {
        return powerLevel
    }

    override fun normalizedPowerLevel(): Long {
        return powerLevel
    }

    override fun isIgnored(): Boolean {
        return false
    }
}
