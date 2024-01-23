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
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.room.roomNotificationSettings
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.media.MediaUploadHandlerImpl
import io.element.android.libraries.matrix.impl.media.map
import io.element.android.libraries.matrix.impl.media.toMSC3246range
import io.element.android.libraries.matrix.impl.notificationsettings.RustNotificationSettingsService
import io.element.android.libraries.matrix.impl.poll.toInner
import io.element.android.libraries.matrix.impl.room.location.toInner
import io.element.android.libraries.matrix.impl.timeline.AsyncMatrixTimeline
import io.element.android.libraries.matrix.impl.timeline.RustMatrixTimeline
import io.element.android.libraries.matrix.impl.util.destroyAll
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import io.element.android.libraries.matrix.impl.widget.RustWidgetDriver
import io.element.android.libraries.matrix.impl.widget.generateWidgetWebViewUrl
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomInfoListener
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.RoomMember
import org.matrix.rustcomponents.sdk.RoomMessageEventContentWithoutRelation
import org.matrix.rustcomponents.sdk.SendAttachmentJoinHandle
import org.matrix.rustcomponents.sdk.WidgetCapabilities
import org.matrix.rustcomponents.sdk.WidgetCapabilitiesProvider
import org.matrix.rustcomponents.sdk.messageEventContentFromHtml
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdown
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import org.matrix.rustcomponents.sdk.Room as InnerRoom
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

@OptIn(ExperimentalCoroutinesApi::class)
class RustMatrixRoom(
    override val sessionId: SessionId,
    private val isKeyBackupEnabled: Boolean,
    private val roomListItem: RoomListItem,
    private val innerRoom: InnerRoom,
    private val innerTimeline: InnerTimeline,
    private val roomNotificationSettingsService: RustNotificationSettingsService,
    sessionCoroutineScope: CoroutineScope,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val systemClock: SystemClock,
    private val roomContentForwarder: RoomContentForwarder,
    private val sessionData: SessionData,
    private val roomSyncSubscriber: RoomSyncSubscriber,
    private val matrixRoomInfoMapper: MatrixRoomInfoMapper,
) : MatrixRoom {
    override val roomId = RoomId(innerRoom.id())

    override val roomInfoFlow: Flow<MatrixRoomInfo> = mxCallbackFlow {
        launch {
            val initial = innerRoom.roomInfo().use(matrixRoomInfoMapper::map)
            channel.trySend(initial)
        }
        innerRoom.subscribeToRoomInfoUpdates(object : RoomInfoListener {
            override fun call(roomInfo: RoomInfo) {
                channel.trySend(matrixRoomInfoMapper.map(roomInfo))
            }
        })
    }

    // Create a dispatcher for all room methods...
    private val roomDispatcher = coroutineDispatchers.io.limitedParallelism(32)

    // ...except getMember methods as it could quickly fill the roomDispatcher...
    private val roomMembersDispatcher = coroutineDispatchers.io.limitedParallelism(8)

    private val roomCoroutineScope = sessionCoroutineScope.childScope(coroutineDispatchers.main, "RoomScope-$roomId")
    private val _membersStateFlow = MutableStateFlow<MatrixRoomMembersState>(MatrixRoomMembersState.Unknown)
    private val _syncUpdateFlow = MutableStateFlow(0L)

    private val _roomNotificationSettingsStateFlow = MutableStateFlow<MatrixRoomNotificationSettingsState>(MatrixRoomNotificationSettingsState.Unknown)
    override val roomNotificationSettingsStateFlow: StateFlow<MatrixRoomNotificationSettingsState> = _roomNotificationSettingsStateFlow

    override val timeline = createMatrixTimeline(innerTimeline) {
        _syncUpdateFlow.value = systemClock.epochMillis()
    }

    override val membersStateFlow: StateFlow<MatrixRoomMembersState> = _membersStateFlow.asStateFlow()

    override val syncUpdateFlow: StateFlow<Long> = _syncUpdateFlow.asStateFlow()

    override suspend fun subscribeToSync() = roomSyncSubscriber.subscribe(roomId)

    override suspend fun unsubscribeFromSync() = roomSyncSubscriber.unsubscribe(roomId)

    override fun destroy() {
        roomCoroutineScope.cancel()
        timeline.close()
        innerRoom.destroy()
        roomListItem.destroy()
        specialModeEventTimelineItem?.destroy()
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
        val currentMembers = currentState.roomMembers()?.toImmutableList()
        _membersStateFlow.value = MatrixRoomMembersState.Pending(prevRoomMembers = currentMembers)
        var rustMembers: List<RoomMember>? = null
        try {
            rustMembers = innerRoom.members().use { membersIterator ->
                buildList {
                    while (true) {
                        // Loading the whole membersIterator as a stop-gap measure.
                        // We should probably implement some sort of paging in the future.
                        ensureActive()
                        addAll(membersIterator.nextChunk(1000u) ?: break)
                    }
                }
            }
            val mappedMembers = rustMembers.parallelMap(RoomMemberMapper::map)
            _membersStateFlow.value = MatrixRoomMembersState.Ready(mappedMembers.toImmutableList())
            Result.success(Unit)
        } catch (exception: CancellationException) {
            _membersStateFlow.value = MatrixRoomMembersState.Error(prevRoomMembers = currentMembers, failure = exception)
            throw exception
        } catch (exception: Exception) {
            _membersStateFlow.value = MatrixRoomMembersState.Error(prevRoomMembers = currentMembers, failure = exception)
            Result.failure(exception)
        } finally {
            rustMembers?.destroyAll()
        }
    }

    override suspend fun userDisplayName(userId: UserId): Result<String?> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.memberDisplayName(userId.value)
        }
    }

    override suspend fun updateRoomNotificationSettings(): Result<Unit> = withContext(coroutineDispatchers.io) {
        val currentState = _roomNotificationSettingsStateFlow.value
        val currentRoomNotificationSettings = currentState.roomNotificationSettings()
        _roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Pending(prevRoomNotificationSettings = currentRoomNotificationSettings)
        runCatching {
            roomNotificationSettingsService.getRoomNotificationSettings(roomId, isEncrypted, isOneToOne).getOrThrow()
        }.map {
            _roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Ready(it)
        }.onFailure {
            _roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Error(
                prevRoomNotificationSettings = currentRoomNotificationSettings,
                failure = it
            )
        }
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.memberAvatarUrl(userId.value)
        }
    }

    override suspend fun sendMessage(body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit> = withContext(roomDispatcher) {
        messageEventContentFromParts(body, htmlBody).withMentions(mentions.map()).use { content ->
            runCatching {
                innerTimeline.send(content)
            }
        }
    }

    override suspend fun editMessage(
        originalEventId: EventId?,
        transactionId: TransactionId?,
        body: String,
        htmlBody: String?,
        mentions: List<Mention>,
    ): Result<Unit> =
        withContext(roomDispatcher) {
            if (originalEventId != null) {
                runCatching {
                    val editedEvent = specialModeEventTimelineItem ?: innerTimeline.getEventTimelineItemByEventId(originalEventId.value)
                    editedEvent.use {
                        innerTimeline.edit(
                            newContent = messageEventContentFromParts(body, htmlBody).withMentions(mentions.map()),
                            editItem = it,
                        )
                    }
                    specialModeEventTimelineItem = null
                }
            } else {
                runCatching {
                    transactionId?.let { cancelSend(it) }
                    innerTimeline.send(messageEventContentFromParts(body, htmlBody))
                }
            }
        }

    private var specialModeEventTimelineItem: EventTimelineItem? = null

    override suspend fun enterSpecialMode(eventId: EventId?): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            specialModeEventTimelineItem?.destroy()
            specialModeEventTimelineItem = null
            specialModeEventTimelineItem = eventId?.let { innerTimeline.getEventTimelineItemByEventId(it.value) }
        }
    }

    override suspend fun replyMessage(eventId: EventId, body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            val inReplyTo = specialModeEventTimelineItem ?: innerTimeline.getEventTimelineItemByEventId(eventId.value)
            inReplyTo.use { eventTimelineItem ->
                innerTimeline.sendReply(messageEventContentFromParts(body, htmlBody).withMentions(mentions.map()), eventTimelineItem)
            }
            specialModeEventTimelineItem = null
        }
    }

    override suspend fun redactEvent(eventId: EventId, reason: String?) = withContext(roomDispatcher) {
        runCatching {
            innerRoom.redact(eventId.value, reason)
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

    override suspend fun canUserRedactOwn(userId: UserId): Result<Boolean> {
        return runCatching {
            innerRoom.canUserRedactOwn(userId.value)
        }
    }

    override suspend fun canUserRedactOther(userId: UserId): Result<Boolean> {
        return runCatching {
            innerRoom.canUserRedactOther(userId.value)
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

    override suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean> {
        return runCatching {
            innerRoom.canUserTriggerRoomNotification(userId.value)
        }
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> {
        return sendAttachment(listOfNotNull(file, thumbnailFile)) {
            innerTimeline.sendImage(file.path, thumbnailFile?.path, imageInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> {
        return sendAttachment(listOfNotNull(file, thumbnailFile)) {
            innerTimeline.sendVideo(file.path, thumbnailFile?.path, videoInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun sendAudio(file: File, audioInfo: AudioInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler> {
        return sendAttachment(listOf(file)) {
            innerTimeline.sendAudio(file.path, audioInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler> {
        return sendAttachment(listOf(file)) {
            innerTimeline.sendFile(file.path, fileInfo.map(), progressCallback?.toProgressWatcher())
        }
    }

    override suspend fun toggleReaction(emoji: String, eventId: EventId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerTimeline.toggleReaction(key = emoji, eventId = eventId.value)
        }
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            roomContentForwarder.forward(fromTimeline = innerTimeline, eventId = eventId, toRoomIds = roomIds)
        }.onFailure {
            Timber.e(it)
        }
    }

    override suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerTimeline.retrySend(transactionId.value)
        }
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerTimeline.cancelSend(transactionId.value)
        }
    }

    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.uploadAvatar(mimeType, data, null)
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
            innerTimeline.sendLocation(
                body = body,
                geoUri = geoUri,
                description = description,
                zoomLevel = zoomLevel?.toUByte(),
                assetType = assetType?.toInner(),
            )
        }
    }

    override suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerTimeline.createPoll(
                question = question,
                answers = answers,
                maxSelections = maxSelections.toUByte(),
                pollKind = pollKind.toInner(),
            )
        }
    }

    override suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            val pollStartEvent =
                innerTimeline.getEventTimelineItemByEventId(
                    eventId = pollStartId.value
                )
            pollStartEvent.use {
                innerTimeline.editPoll(
                    question = question,
                    answers = answers,
                    maxSelections = maxSelections.toUByte(),
                    pollKind = pollKind.toInner(),
                    editItem = pollStartEvent,
                )
            }
        }
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>
    ): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerTimeline.sendPollResponse(
                pollStartId = pollStartId.value,
                answers = answers,
            )
        }
    }

    override suspend fun endPoll(
        pollStartId: EventId,
        text: String
    ): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerTimeline.endPoll(
                pollStartId = pollStartId.value,
                text = text,
            )
        }
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> = sendAttachment(listOf(file)) {
        innerTimeline.sendVoiceMessage(
            url = file.path,
            audioInfo = audioInfo.map(),
            waveform = waveform.toMSC3246range(),
            progressWatcher = progressCallback?.toProgressWatcher(),
        )
    }

    override suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ) = runCatching {
        widgetSettings.generateWidgetWebViewUrl(innerRoom, clientId, languageTag, theme)
    }

    override fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver> = runCatching {
        RustWidgetDriver(
            widgetSettings = widgetSettings,
            room = innerRoom,
            widgetCapabilitiesProvider = object : WidgetCapabilitiesProvider {
                override fun acquireCapabilities(capabilities: WidgetCapabilities): WidgetCapabilities {
                    return capabilities
                }
            },
        )
    }

    override fun pollHistory() = AsyncMatrixTimeline(
        coroutineScope = roomCoroutineScope,
        dispatcher = roomDispatcher
    ) {
        val innerTimeline = innerRoom.pollHistory()
        createMatrixTimeline(innerTimeline)
    }

    private fun sendAttachment(files: List<File>, handle: () -> SendAttachmentJoinHandle): Result<MediaUploadHandler> {
        return runCatching {
            MediaUploadHandlerImpl(files, handle())
        }
    }

    private fun createMatrixTimeline(
        timeline: InnerTimeline,
        onNewSyncedEvent: () -> Unit = {},
    ): MatrixTimeline {
        return RustMatrixTimeline(
            isKeyBackupEnabled = isKeyBackupEnabled,
            matrixRoom = this,
            roomCoroutineScope = roomCoroutineScope,
            dispatcher = roomDispatcher,
            lastLoginTimestamp = sessionData.loginTimestamp,
            onNewSyncedEvent = onNewSyncedEvent,
            innerTimeline = timeline,
        )
    }

    private fun messageEventContentFromParts(body: String, htmlBody: String?): RoomMessageEventContentWithoutRelation =
        if (htmlBody != null) {
            messageEventContentFromHtml(body, htmlBody)
        } else {
            messageEventContentFromMarkdown(body)
        }
}
