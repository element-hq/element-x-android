/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.member

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustRoomMember
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoom
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomMembersIterator
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher.Source.CACHE
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher.Source.CACHE_AND_SERVER
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher.Source.SERVER
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID_3
import io.element.android.libraries.matrix.test.A_USER_ID_4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomMemberListFetcherTest {
    @Test
    fun `fetchRoomMembers with CACHE source - emits cached members, if any`() = runTest {
        val room = FakeFfiRoom(getMembersNoSync = {
            FakeFfiRoomMembersIterator(
                listOf(
                    aRustRoomMember(A_USER_ID),
                    aRustRoomMember(A_USER_ID_2),
                    aRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Unknown::class.java)

            fetcher.fetchRoomMembers(source = CACHE)

            // Loading state
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Pending::class.java)

            val cachedItemsState = awaitItem()
            assertThat(cachedItemsState).isInstanceOf(RoomMembersState.Ready::class.java)
            assertThat((cachedItemsState as? RoomMembersState.Ready)?.roomMembers).hasSize(3)
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE source - emits empty list, if no members exist`() = runTest {
        val room = FakeFfiRoom(getMembersNoSync = {
            FakeFfiRoomMembersIterator(emptyList())
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Pending::class.java)
            assertThat((awaitItem() as? RoomMembersState.Ready)?.roomMembers).isEmpty()
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE source - emits Error on error found`() = runTest {
        val room = FakeFfiRoom(getMembersNoSync = {
            error("Some unexpected issue")
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Pending::class.java)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Error::class.java)
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE source - emits all items at once`() = runTest {
        val room = FakeFfiRoom(getMembersNoSync = {
            FakeFfiRoomMembersIterator(
                listOf(
                    aRustRoomMember(A_USER_ID),
                    aRustRoomMember(A_USER_ID_2),
                    aRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default, pageSize = 2)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE)

            // Initial state
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Unknown::class.java)
            // Started loading cached members
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Pending::class.java)
            // Finished loading cached members
            assertThat((awaitItem() as? RoomMembersState.Ready)?.roomMembers).hasSize(3)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `fetchRoomMembers with SERVER source - emits only new members, if any`() = runTest {
        val room = FakeFfiRoom(getMembers = {
            FakeFfiRoomMembersIterator(
                listOf(
                    aRustRoomMember(A_USER_ID),
                    aRustRoomMember(A_USER_ID_2),
                    aRustRoomMember(A_USER_ID_3),
                )
            )
        })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = SERVER)

            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Pending::class.java)
            assertThat((awaitItem() as? RoomMembersState.Ready)?.roomMembers?.size).isEqualTo(3)
        }
    }

    @Test
    fun `fetchRoomMembers with SERVER source - on error it emits an Error item`() = runTest {
        val room = FakeFfiRoom(getMembers = { error("An unexpected error") })

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = SERVER)

            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Unknown::class.java)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Pending::class.java)
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Error::class.java)
        }
    }

    @Test
    fun `fetchRoomMembers with CACHE_AND_SERVER source - returns cached items first, then new ones`() = runTest {
        val room = FakeFfiRoom(
            getMembersNoSync = {
                FakeFfiRoomMembersIterator(listOf(aRustRoomMember(A_USER_ID_4)))
            },
            getMembers = {
                FakeFfiRoomMembersIterator(
                    listOf(
                        aRustRoomMember(A_USER_ID),
                        aRustRoomMember(A_USER_ID_2),
                        aRustRoomMember(A_USER_ID_3),
                    )
                )
            }
        )

        val fetcher = RoomMemberListFetcher(room, Dispatchers.Default)
        fetcher.membersFlow.test {
            fetcher.fetchRoomMembers(source = CACHE_AND_SERVER)
            // Initial
            assertThat(awaitItem()).isInstanceOf(RoomMembersState.Unknown::class.java)
            // Loading cached
            awaitItem().let { pending ->
                assertThat(pending).isInstanceOf(RoomMembersState.Pending::class.java)
                assertThat(pending.roomMembers()).isEmpty()
            }
            // Loaded cached
            awaitItem().let { cached ->
                assertThat(cached).isInstanceOf(RoomMembersState.Pending::class.java)
                assertThat(cached.roomMembers()).hasSize(1)
            }
            // Start loading new
            awaitItem().let { ready ->
                assertThat(ready).isInstanceOf(RoomMembersState.Ready::class.java)
                assertThat(ready.roomMembers()).hasSize(3)
            }
        }
    }
}
