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
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.room.location.toInner
import io.element.android.libraries.matrix.impl.timeline.RustMatrixTimeline
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RequiredState
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.RoomSubscription
import org.matrix.rustcomponents.sdk.SendAttachmentJoinHandle
import org.matrix.rustcomponents.sdk.genTransactionId
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdown
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class RustMatrixRoom(
    override val sessionId: SessionId,
    private val roomListItem: RoomListItem,
    private val innerRoom: Room,
    sessionCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val systemClock: SystemClock,
    private val roomContentForwarder: RoomContentForwarder,
    private val sessionData: SessionData,
) : MatrixRoom {

    override val roomId = RoomId(innerRoom.id())

    // Create a dispatcher for all room methods...
    private val roomDispatcher = coroutineDispatchers.io.limitedParallelism(32)

    //...except getMember methods as it could quickly fill the roomDispatcher...
    private val roomMembersDispatcher = coroutineDispatchers.io.limitedParallelism(8)

    private val roomCoroutineScope = sessionCoroutineScope.childScope(coroutineDispatchers.main, "RoomScope-$roomId")
    private val _membersStateFlow = MutableStateFlow<MatrixRoomMembersState>(MatrixRoomMembersState.Unknown)
    private val _syncUpdateFlow = MutableStateFlow(0L)
    private val _timeline by lazy {
        RustMatrixTimeline(
            matrixRoom = this,
            innerRoom = innerRoom,
            roomCoroutineScope = roomCoroutineScope,
            dispatcher = roomDispatcher,
            lastLoginTimestamp = sessionData.loginTimestamp,
            onNewSyncedEvent = { _syncUpdateFlow.value = systemClock.epochMillis() }
        )
    }

    override val membersStateFlow: StateFlow<MatrixRoomMembersState> = _membersStateFlow.asStateFlow()

    override val syncUpdateFlow: StateFlow<Long> = _syncUpdateFlow.asStateFlow()

    override val timeline: MatrixTimeline = _timeline

    override fun subscribeToSync() {
        val settings = RoomSubscription(
            requiredState = listOf(
                RequiredState(key = EventType.STATE_ROOM_CANONICAL_ALIAS, value = ""),
                RequiredState(key = EventType.STATE_ROOM_TOPIC, value = ""),
                RequiredState(key = EventType.STATE_ROOM_JOIN_RULES, value = ""),
                RequiredState(key = EventType.STATE_ROOM_POWER_LEVELS, value = ""),
            ),
            timelineLimit = null
        )
        roomListItem.subscribe(settings)
    }

    override fun unsubscribeFromSync() {
        roomListItem.unsubscribe()
    }

    override fun destroy() {
        roomCoroutineScope.cancel()
        innerRoom.destroy()
        roomListItem.destroy()
    }

    override val name: String?
        get() {
            return roomListItem.name()
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
            return roomListItem.avatarUrl() ?: innerRoom.avatarUrl()
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

    override val activeMemberCount: Long
        get() = innerRoom.activeMembersCount().toLong()

    override suspend fun updateMembers(): Result<Unit> = withContext(roomMembersDispatcher) {
        val currentState = _membersStateFlow.value
        val currentMembers = currentState.roomMembers()
        _membersStateFlow.value = MatrixRoomMembersState.Pending(prevRoomMembers = currentMembers)
        runCatching {
            innerRoom.members().parallelMap(RoomMemberMapper::map)
        }.map {
            _membersStateFlow.value = MatrixRoomMembersState.Ready(it)
        }.onFailure {
            _membersStateFlow.value = MatrixRoomMembersState.Error(prevRoomMembers = currentMembers, failure = it)
        }
    }

    override suspend fun userDisplayName(userId: UserId): Result<String?> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.memberDisplayName(userId.value)
        }
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.memberAvatarUrl(userId.value)
        }
    }

    override suspend fun sendMessage(message: String): Result<Unit> = withContext(roomDispatcher) {
        val transactionId = genTransactionId()
        messageEventContentFromMarkdown(message).use { content ->
            runCatching {
                innerRoom.send(content, transactionId)
            }
        }
    }

    override suspend fun editMessage(originalEventId: EventId?, transactionId: TransactionId?, message: String): Result<Unit> = withContext(roomDispatcher) {
        if (originalEventId != null) {
            runCatching {
                innerRoom.edit(/* TODO use content */ message, originalEventId.value, transactionId?.value)
            }
        } else {
            runCatching {
                transactionId?.let { cancelSend(it) }
                innerRoom.send(messageEventContentFromMarkdown(message), genTransactionId())
            }
        }
    }

    override suspend fun replyMessage(eventId: EventId, message: String): Result<Unit> = withContext(roomDispatcher) {
        val transactionId = genTransactionId()
        // val content = messageEventContentFromMarkdown(message)
        runCatching {
            innerRoom.sendReply(/* TODO use content */ message, eventId.value, transactionId)
        }
    }

    override suspend fun redactEvent(eventId: EventId, reason: String?) = withContext(roomDispatcher) {
        val transactionId = genTransactionId()
        runCatching {
            innerRoom.redact(eventId.value, reason, transactionId)
        }
    }

    override suspend fun leave(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.leave()
        }
    }

    override suspend fun join(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.join()
        }
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.inviteUserById(id.value)
        }
    }

    override suspend fun canUserInvite(userId: UserId): Result<Boolean> {
        return runCatching {
            innerRoom.canUserInvite(userId.value)
        }
    }

    override suspend fun canUserRedact(userId: UserId): Result<Boolean> {
        return runCatching {
            innerRoom.canUserRedact(userId.value)
        }
    }

    override suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean> {
        return runCatching {
            innerRoom.canUserSendState(userId.value, type.map())
        }
    }

    override suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean> {
        return runCatching {
            innerRoom.canUserSendMessage(userId.value, type.map())
        }
    }

    override suspend fun sendImage(file: File, thumbnailFile: File, imageInfo: ImageInfo, progressCallback: ProgressCallback?): Result<Unit> {
        return sendAttachment {
            innerRoom.sendImage(file.path, thumbnailFile.path, imageInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun sendVideo(file: File, thumbnailFile: File, videoInfo: VideoInfo, progressCallback: ProgressCallback?): Result<Unit> {
        return sendAttachment {
            innerRoom.sendVideo(file.path, thumbnailFile.path, videoInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun sendAudio(file: File, audioInfo: AudioInfo, progressCallback: ProgressCallback?): Result<Unit> {
        return sendAttachment {
            innerRoom.sendAudio(file.path, audioInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<Unit> {
        return sendAttachment {
            innerRoom.sendFile(file.path, fileInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun toggleReaction(emoji: String, eventId: EventId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.toggleReaction(key = emoji, eventId = eventId.value)
        }
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            roomContentForwarder.forward(fromRoom = innerRoom, eventId = eventId, toRoomIds = roomIds)
        }.onFailure {
            Timber.e(it)
        }
    }

    override suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.retrySend(transactionId.value)
        }
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.cancelSend(transactionId.value)
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.uploadAvatar(mimeType, data.toUByteArray().toList())
        }
    }

    override suspend fun removeAvatar(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.removeAvatar()
        }
    }

    override suspend fun setName(name: String): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.setName(name)
        }
    }

    override suspend fun setTopic(topic: String): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.setTopic(topic)
        }
    }

    override suspend fun reportContent(eventId: EventId, reason: String, blockUserId: UserId?): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.reportContent(eventId = eventId.value, score = null, reason = reason)
            if (blockUserId != null) {
                innerRoom.ignoreUser(blockUserId.value)
            }
        }
    }

    override suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
    ): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.sendLocation(
                body = body,
                geoUri = geoUri,
                description = description,
                zoomLevel = zoomLevel?.toUByte(),
                assetType = assetType?.toInner(),
                txnId = genTransactionId()
            )
        }
    }

    //TODO handle cancellation, need refactoring of how we are catching errors
    private suspend fun sendAttachment(handle: () -> SendAttachmentJoinHandle): Result<Unit> {
        return runCatching {
            handle().use {
                it.join()
            }
        }
    }
}


