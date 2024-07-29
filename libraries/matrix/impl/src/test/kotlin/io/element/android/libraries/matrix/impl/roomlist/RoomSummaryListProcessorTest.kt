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

package io.element.android.libraries.matrix.impl.roomlist

import com.google.common.truth.Truth.assertThat
import com.sun.jna.Pointer
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.room.aRoomSummaryFilled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.Membership
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RoomHero
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomList
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.RoomListServiceInterface
import org.matrix.rustcomponents.sdk.RoomListServiceStateListener
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicatorListener
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomNotificationMode
import org.matrix.rustcomponents.sdk.RoomSubscription
import org.matrix.rustcomponents.sdk.TaskHandle

// NOTE: this class is using a fake implementation of a Rust SDK interface which returns actual Rust objects with pointers.
// Since we don't access the data in those objects, this is fine for our tests, but that's as far as we can test this class.
class RoomSummaryListProcessorTest {
    private val summaries = MutableStateFlow<List<RoomSummary>>(emptyList())

    @Test
    fun `Append adds new entries at the end of the list`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()

        val newEntry = FakeRoomListItem(A_ROOM_ID_2)
        processor.postUpdate(listOf(RoomListEntriesUpdate.Append(listOf(newEntry, newEntry, newEntry))))

        assertThat(summaries.value.count()).isEqualTo(4)
        assertThat(summaries.value.subList(1, 4).all { it.roomId == A_ROOM_ID_2 }).isTrue()
    }

    @Test
    fun `PushBack adds a new entry at the end of the list`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled())
        val processor = createProcessor()
        processor.postUpdate(listOf(RoomListEntriesUpdate.PushBack(FakeRoomListItem(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value.last().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `PushFront inserts a new entry at the start of the list`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled())
        val processor = createProcessor()
        processor.postUpdate(listOf(RoomListEntriesUpdate.PushFront(FakeRoomListItem(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value.first().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Set replaces an entry at some index`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled())
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Set(index.toUInt(), FakeRoomListItem(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Insert inserts a new entry at the provided index`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled())
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Insert(index.toUInt(), FakeRoomListItem(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Remove removes an entry at some index`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled(roomId = A_ROOM_ID), aRoomSummaryFilled(A_ROOM_ID_2))
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Remove(index.toUInt())))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `PopBack removes an entry at the end of the list`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled(roomId = A_ROOM_ID), aRoomSummaryFilled(A_ROOM_ID_2))
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.PopBack))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    @Test
    fun `PopFront removes an entry at the start of the list`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled(roomId = A_ROOM_ID), aRoomSummaryFilled(A_ROOM_ID_2))
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.PopFront))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Clear removes all the entries`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled(roomId = A_ROOM_ID), aRoomSummaryFilled(A_ROOM_ID_2))
        val processor = createProcessor()

        processor.postUpdate(listOf(RoomListEntriesUpdate.Clear))

        assertThat(summaries.value).isEmpty()
    }

    @Test
    fun `Truncate removes all entries after the provided length`() = runTest {
        summaries.value = listOf(aRoomSummaryFilled(roomId = A_ROOM_ID), aRoomSummaryFilled(A_ROOM_ID_2))
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Truncate(1u)))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    private fun TestScope.createProcessor() = RoomSummaryListProcessor(
        summaries,
        fakeRoomListService,
        coroutineContext = StandardTestDispatcher(testScheduler),
        roomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
    )

    // Fake room list service that returns Rust objects with null pointers. Luckily for us, they don't crash for our test cases
    private val fakeRoomListService = object : RoomListServiceInterface {
        override suspend fun allRooms(): RoomList {
            return RoomList(Pointer.NULL)
        }

        override fun room(roomId: String): RoomListItem {
            return RoomListItem(Pointer.NULL)
        }

        override fun state(listener: RoomListServiceStateListener): TaskHandle {
            return TaskHandle(Pointer.NULL)
        }

        override fun syncIndicator(delayBeforeShowingInMs: UInt, delayBeforeHidingInMs: UInt, listener: RoomListServiceSyncIndicatorListener): TaskHandle {
            return TaskHandle(Pointer.NULL)
        }
    }
}

private fun aRustRoomInfo(
    id: String = A_ROOM_ID.value,
    displayName: String = A_ROOM_NAME,
    rawName: String = A_ROOM_NAME,
    topic: String? = null,
    avatarUrl: String? = null,
    isDirect: Boolean = false,
    isPublic: Boolean = false,
    isSpace: Boolean = false,
    isTombstoned: Boolean = false,
    isFavourite: Boolean = false,
    canonicalAlias: String? = null,
    alternativeAliases: List<String> = listOf(),
    membership: Membership = Membership.JOINED,
    inviter: RoomMember? = null,
    heroes: List<RoomHero> = listOf(),
    activeMembersCount: ULong = 0uL,
    invitedMembersCount: ULong = 0uL,
    joinedMembersCount: ULong = 0uL,
    userPowerLevels: Map<String, Long> = mapOf(),
    highlightCount: ULong = 0uL,
    notificationCount: ULong = 0uL,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    activeRoomCallParticipants: List<String> = listOf(),
    isMarkedUnread: Boolean = false,
    numUnreadMessages: ULong = 0uL,
    numUnreadNotifications: ULong = 0uL,
    numUnreadMentions: ULong = 0uL,
    pinnedEventIds: List<String> = listOf(),
) = RoomInfo(
    id = id,
    displayName = displayName,
    rawName = rawName,
    topic = topic,
    avatarUrl = avatarUrl,
    isDirect = isDirect,
    isPublic = isPublic,
    isSpace = isSpace,
    isTombstoned = isTombstoned,
    isFavourite = isFavourite,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases,
    membership = membership,
    inviter = inviter,
    heroes = heroes,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    userPowerLevels = userPowerLevels,
    highlightCount = highlightCount,
    notificationCount = notificationCount,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = hasRoomCall,
    activeRoomCallParticipants = activeRoomCallParticipants,
    isMarkedUnread = isMarkedUnread,
    numUnreadMessages = numUnreadMessages,
    numUnreadNotifications = numUnreadNotifications,
    numUnreadMentions = numUnreadMentions,
    pinnedEventIds = pinnedEventIds,
)

class FakeRoomListItem(
    private val roomId: RoomId,
    private val roomInfo: RoomInfo = aRustRoomInfo(id = roomId.value),
    private val latestEvent: EventTimelineItem? = null,
) : RoomListItem(NoPointer) {
    override fun id(): String {
        return roomId.value
    }

    override suspend fun roomInfo(): RoomInfo {
        return roomInfo
    }

    override suspend fun latestEvent(): EventTimelineItem? {
        return latestEvent
    }

    override fun subscribe(settings: RoomSubscription?) = Unit
}
