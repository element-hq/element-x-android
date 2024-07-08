import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.runner.AndroidJUnitRunner
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.impl.room.elementHeroes
import io.element.android.libraries.matrix.impl.room.map
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.TracingConfiguration
import org.matrix.rustcomponents.sdk.mockedEntries
import org.matrix.rustcomponents.sdk.setupTracing
import java.time.Instant
import kotlin.time.measureTime

/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@RunWith(AndroidJUnit4::class)
class RoomListFFIBenchmark {
    @Test
    fun benchmark() {
        setupTracing(
            TracingConfiguration(
                filter = "matrix_sdk_ffi=warn",
                writeToStdoutOrSystem = true,
                writeToFiles = null,
            )
        )
        val started = Instant.now()
        Log.d("Test", "Benchmarking RoomListService at $started...")
        val result = runBlocking { mockedEntries() }
        val end = Instant.now()
        Log.d("Test", "Benchmarking finished at $end")
        Log.d("Test", "Benchmarking RoomListService result: ${(result.first() as RoomListEntriesUpdate.Reset).values.size} rooms")
        val resetValues = (result.first() as RoomListEntriesUpdate.Reset).values
        val mappingTime = measureTime {
            resetValues.map {
                val roomInfo = it.roomInfo()
//                RoomSummary(
//                    roomId = RoomId(roomInfo.id),
//                    name = roomInfo.displayName,
//                    canonicalAlias = roomInfo.canonicalAlias?.let(::RoomAlias),
//                    isDirect = roomInfo.isDirect,
//                    avatarUrl = roomInfo.avatarUrl,
//                    numUnreadMentions = roomInfo.numUnreadMentions.toInt(),
//                    numUnreadMessages = roomInfo.numUnreadMessages.toInt(),
//                    numUnreadNotifications = roomInfo.numUnreadNotifications.toInt(),
//                    isMarkedUnread = roomInfo.isMarkedUnread,
//                    lastMessage = null,
//                    inviter = roomInfo.inviter?.let(RoomMemberMapper::map),
//                    userDefinedNotificationMode = roomInfo.userDefinedNotificationMode?.map(),
//                    hasRoomCall = roomInfo.hasRoomCall,
//                    isDm = roomInfo.isDirect,
//                    isFavorite = roomInfo.isFavourite,
//                    currentUserMembership = roomInfo.membership.map(),
//                    heroes = roomInfo.elementHeroes(),
//                )
            }
        }
        Log.d("Test", "Benchmarking RoomListService took ${end.toEpochMilli() - started.toEpochMilli()} ms")
        Log.d("Test", "Benchmark: mapping took $mappingTime")
    }
}
