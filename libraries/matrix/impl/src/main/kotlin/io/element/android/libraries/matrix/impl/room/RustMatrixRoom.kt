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

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.timeline.RustMatrixTimeline
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.SlidingSyncRoom
import org.matrix.rustcomponents.sdk.UpdateSummary
import org.matrix.rustcomponents.sdk.genTransactionId
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdown
import java.io.File

class RustMatrixRoom(
    override val sessionId: SessionId,
    private val slidingSyncUpdateFlow: Flow<UpdateSummary>,
    private val slidingSyncRoom: SlidingSyncRoom,
    private val innerRoom: Room,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val clock: SystemClock,
) : MatrixRoom {

    override val membersStateFlow: StateFlow<MatrixRoomMembersState>
        get() = _membersStateFlow

    private var _membersStateFlow = MutableStateFlow<MatrixRoomMembersState>(MatrixRoomMembersState.Unknown)

    override val roomNotificationSettingsStateFlow: StateFlow<MatrixRoomNotificationSettingsState>
        get() = _roomNotificationSettingsStateFlow

    private var _roomNotificationSettingsStateFlow = MutableStateFlow<MatrixRoomNotificationSettingsState>(MatrixRoomNotificationSettingsState.Unknown)

    private val timeline by lazy {
        RustMatrixTimeline(
            matrixRoom = this,
            innerRoom = innerRoom,
            slidingSyncRoom = slidingSyncRoom,
            coroutineScope = coroutineScope,
            coroutineDispatchers = coroutineDispatchers
        )
    }

    override fun syncUpdateFlow(): Flow<Long> {
        return slidingSyncUpdateFlow
            .filter {
                it.rooms.contains(roomId.value)
            }
            .map {
                clock.epochMillis()
            }
            .onStart { emit(clock.epochMillis()) }
    }

    override fun timeline(): MatrixTimeline {
        return timeline
    }

    override fun close() {
        innerRoom.destroy()
        slidingSyncRoom.destroy()
    }

    override val roomId = RoomId(innerRoom.id())

    override val name: String?
        get() {
            return slidingSyncRoom.name()
        }

    override val bestName: String
        get() {
            return name?.takeIf { it.isNotEmpty() } ?: innerRoom.id()
        }

    override val displayName: String
        get() {
            return innerRoom.displayName()
        }

    override val topic: String?
        get() {
            return innerRoom.topic()
        }

    override val avatarUrl: String?
        get() {
            return innerRoom.avatarUrl()
        }

    override val isEncrypted: Boolean
        get() = runCatching { innerRoom.isEncrypted() }.getOrDefault(false)

    override val alias: String?
        get() = innerRoom.canonicalAlias()

    override val alternativeAliases: List<String>
        get() = innerRoom.alternativeAliases()

    override val isPublic: Boolean
        get() = innerRoom.isPublic()

    override val isDirect: Boolean
        get() = innerRoom.isDirect()

    override val joinedMemberCount: Long
        get() = innerRoom.joinedMembersCount().toLong()

    override suspend fun updateMembers(): Result<Unit> = withContext(coroutineDispatchers.io) {
        val currentState = _membersStateFlow.value
        val currentMembers = currentState.roomMembers()
        _membersStateFlow.value = MatrixRoomMembersState.Pending(prevRoomMembers = currentMembers)
        runCatching {
            innerRoom.members().map(RoomMemberMapper::map)
        }.map {
            _membersStateFlow.value = MatrixRoomMembersState.Ready(it)
        }.onFailure {
            _membersStateFlow.value = MatrixRoomMembersState.Error(prevRoomMembers = currentMembers, failure = it)
        }
    }

    override suspend fun userDisplayName(userId: UserId): Result<String?> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.memberDisplayName(userId.value)
            }
        }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.memberAvatarUrl(userId.value)
            }
        }

    override suspend fun sendMessage(message: String): Result<Unit> = withContext(coroutineDispatchers.io) {
        val transactionId = genTransactionId()
        messageEventContentFromMarkdown(message).use { content ->
            runCatching {
                innerRoom.send(content, transactionId)
            }
        }
    }

    override suspend fun editMessage(originalEventId: EventId, message: String): Result<Unit> = withContext(coroutineDispatchers.io) {
        val transactionId = genTransactionId()
        // val content = messageEventContentFromMarkdown(message)
        runCatching {
            innerRoom.edit(/* TODO use content */ message, originalEventId.value, transactionId)
        }
    }

    override suspend fun replyMessage(eventId: EventId, message: String): Result<Unit> = withContext(coroutineDispatchers.io) {
        val transactionId = genTransactionId()
        // val content = messageEventContentFromMarkdown(message)
        runCatching {
            innerRoom.sendReply(/* TODO use content */ message, eventId.value, transactionId)
        }
    }

    override suspend fun redactEvent(eventId: EventId, reason: String?) = withContext(coroutineDispatchers.io) {
        val transactionId = genTransactionId()
        runCatching {
            innerRoom.redact(eventId.value, reason, transactionId)
        }
    }

    override suspend fun leave(): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.leave()
        }
    }

    override suspend fun acceptInvitation(): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.acceptInvitation()
        }
    }

    override suspend fun rejectInvitation(): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.rejectInvitation()
        }
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.inviteUserById(id.value)
        }
    }

    override suspend fun canInvite(): Result<Boolean> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.member(sessionId.value).use(RoomMember::canInvite)
        }
    }

    override suspend fun canSendStateEvent(type: StateEventType): Result<Boolean> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.member(sessionId.value).use { it.canSendState(type.map()) }
        }
    }

    override suspend fun sendImage(file: File, thumbnailFile: File, imageInfo: ImageInfo): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.sendImage(file.path, thumbnailFile.path, imageInfo.map())
        }
    }

    override suspend fun sendVideo(file: File, thumbnailFile: File, videoInfo: VideoInfo): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.sendVideo(file.path, thumbnailFile.path, videoInfo.map())
        }
    }

    override suspend fun sendAudio(file: File, audioInfo: AudioInfo): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.sendAudio(file.path, audioInfo.map())
        }
    }

    override suspend fun sendFile(file: File, fileInfo: FileInfo): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.sendFile(file.path, fileInfo.map())
        }
    }

    override suspend fun sendReaction(emoji: String, eventId: EventId): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            innerRoom.sendReaction(key = emoji, eventId = eventId.value)
        }
    }

    override suspend fun retrySendMessage(transactionId: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.retrySend(transactionId)
            }
        }

    override suspend fun cancelSend(transactionId: String): Result<Unit>  =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.cancelSend(transactionId)
            }
        }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.uploadAvatar(mimeType, data.toUByteArray().toList())
            }
        }

    override suspend fun removeAvatar(): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.removeAvatar()
            }
        }

    override suspend fun setName(name: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.setName(name)
            }
        }

    override suspend fun setTopic(topic: String): Result<Unit> =
        withContext(coroutineDispatchers.io) {
            runCatching {
                innerRoom.setTopic(topic)
            }
        }
}
