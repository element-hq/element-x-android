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
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.room.roomNotificationSettings
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher
import io.element.android.libraries.matrix.impl.room.member.RoomMemberMapper
import io.element.android.libraries.matrix.impl.room.powerlevels.RoomPowerLevelsMapper
import io.element.android.libraries.matrix.impl.timeline.RustTimeline
import io.element.android.libraries.matrix.impl.timeline.toRustReceiptType
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import io.element.android.libraries.matrix.impl.widget.RustWidgetDriver
import io.element.android.libraries.matrix.impl.widget.generateWidgetWebViewUrl
import io.element.android.libraries.sessionstorage.api.SessionData
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomInfoListener
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.TypingNotificationsListener
import org.matrix.rustcomponents.sdk.UserPowerLevelUpdate
import org.matrix.rustcomponents.sdk.WidgetCapabilities
import org.matrix.rustcomponents.sdk.WidgetCapabilitiesProvider
import org.matrix.rustcomponents.sdk.getElementCallRequiredPermissions
import org.matrix.rustcomponents.sdk.use
import uniffi.matrix_sdk.RoomPowerLevelChanges
import java.io.File
import org.matrix.rustcomponents.sdk.Room as InnerRoom
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

@OptIn(ExperimentalCoroutinesApi::class)
class RustMatrixRoom(
    override val sessionId: SessionId,
    private val isKeyBackupEnabled: Boolean,
    private val roomListItem: RoomListItem,
    private val innerRoom: InnerRoom,
    innerTimeline: InnerTimeline,
    private val notificationSettingsService: NotificationSettingsService,
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

    override val roomTypingMembersFlow: Flow<List<UserId>> = mxCallbackFlow {
        launch {
            val initial = emptyList<UserId>()
            channel.trySend(initial)
        }
        innerRoom.subscribeToTypingNotifications(object : TypingNotificationsListener {
            override fun call(typingUserIds: List<String>) {
                channel.trySend(
                    typingUserIds
                        .filter { it != sessionData.userId }
                        .map(::UserId)
                )
            }
        })
    }

    // Create a dispatcher for all room methods...
    private val roomDispatcher = coroutineDispatchers.io.limitedParallelism(32)

    // ...except getMember methods as it could quickly fill the roomDispatcher...
    private val roomMembersDispatcher = coroutineDispatchers.io.limitedParallelism(8)

    private val roomCoroutineScope = sessionCoroutineScope.childScope(coroutineDispatchers.main, "RoomScope-$roomId")
    private val _syncUpdateFlow = MutableStateFlow(0L)
    private val roomMemberListFetcher = RoomMemberListFetcher(innerRoom, roomMembersDispatcher)

    private val _roomNotificationSettingsStateFlow = MutableStateFlow<MatrixRoomNotificationSettingsState>(MatrixRoomNotificationSettingsState.Unknown)
    override val roomNotificationSettingsStateFlow: StateFlow<MatrixRoomNotificationSettingsState> = _roomNotificationSettingsStateFlow

    override val liveTimeline = createTimeline(innerTimeline, isLive = true) {
        _syncUpdateFlow.value = systemClock.epochMillis()
    }

    override val membersStateFlow: StateFlow<MatrixRoomMembersState> = roomMemberListFetcher.membersFlow

    override val syncUpdateFlow: StateFlow<Long> = _syncUpdateFlow.asStateFlow()

    init {
        val powerLevelChanges = roomInfoFlow.map { it.userPowerLevels }.distinctUntilChanged()
        val membershipChanges = liveTimeline.membershipChangeEventReceived.onStart { emit(Unit) }
        combine(membershipChanges, powerLevelChanges) { _, _ -> }
            // Skip initial one
            .drop(1)
            // The new events should already be in the SDK cache, no need to fetch them from the server
            .onEach { roomMemberListFetcher.fetchRoomMembers(source = RoomMemberListFetcher.Source.CACHE) }
            .launchIn(roomCoroutineScope)
    }

    override suspend fun subscribeToSync() = roomSyncSubscriber.subscribe(roomId)

    override suspend fun unsubscribeFromSync() = roomSyncSubscriber.unsubscribe(roomId)

    override suspend fun timelineFocusedOnEvent(eventId: EventId): Result<Timeline> {
        return runCatching {
            innerRoom.timelineFocusedOnEvent(
                eventId = eventId.value,
                numContextEvents = 50u,
                internalIdPrefix = "focus_$eventId",
            ).let { inner ->
                createTimeline(inner, isLive = false)
            }
        }.mapFailure {
            it.toFocusEventException()
        }
    }

    override fun destroy() {
        roomCoroutineScope.cancel()
        liveTimeline.close()
        innerRoom.destroy()
        roomListItem.destroy()
    }

    override val displayName: String
        get() = runCatching { innerRoom.displayName() }.getOrDefault("")

    override val topic: String?
        get() = runCatching { innerRoom.topic() }.getOrDefault(null)

    override val avatarUrl: String?
        get() = runCatching { roomListItem.avatarUrl() ?: innerRoom.avatarUrl() }.getOrDefault(null)

    override val isEncrypted: Boolean
        get() = runCatching { innerRoom.isEncrypted() }.getOrDefault(false)

    override val alias: RoomAlias?
        get() = runCatching { innerRoom.canonicalAlias()?.let(::RoomAlias) }.getOrDefault(null)

    override val alternativeAliases: List<RoomAlias>
        get() = runCatching { innerRoom.alternativeAliases().map { RoomAlias(it) } }.getOrDefault(emptyList())

    override val isPublic: Boolean
        get() = runCatching { innerRoom.isPublic() }.getOrDefault(false)

    override val isSpace: Boolean
        get() = runCatching { innerRoom.isSpace() }.getOrDefault(false)

    override val isDirect: Boolean
        get() = runCatching { innerRoom.isDirect() }.getOrDefault(false)

    override val joinedMemberCount: Long
        get() = runCatching { innerRoom.joinedMembersCount().toLong() }.getOrDefault(0)

    override val activeMemberCount: Long
        get() = runCatching { innerRoom.activeMembersCount().toLong() }.getOrDefault(0)

    override suspend fun updateMembers() {
        val useCache = membersStateFlow.value is MatrixRoomMembersState.Unknown
        val source = if (useCache) {
            RoomMemberListFetcher.Source.CACHE_AND_SERVER
        } else {
            RoomMemberListFetcher.Source.SERVER
        }
        roomMemberListFetcher.fetchRoomMembers(source = source)
    }

    override suspend fun getMembers(limit: Int) = withContext(roomDispatcher) {
        runCatching {
            innerRoom.members().use {
                it.nextChunk(limit.toUInt()).orEmpty().map { roomMember ->
                    RoomMemberMapper.map(roomMember)
                }
            }
        }
    }

    override suspend fun getUpdatedMember(userId: UserId): Result<RoomMember> = withContext(roomDispatcher) {
        runCatching {
            RoomMemberMapper.map(innerRoom.member(userId.value))
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
            notificationSettingsService.getRoomNotificationSettings(roomId, isEncrypted, isOneToOne).getOrThrow()
        }.map {
            _roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Ready(it)
        }.onFailure {
            _roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Error(
                prevRoomNotificationSettings = currentRoomNotificationSettings,
                failure = it
            )
        }
    }

    override suspend fun userRole(userId: UserId): Result<RoomMember.Role> = withContext(coroutineDispatchers.io) {
        runCatching {
            RoomMemberMapper.mapRole(innerRoom.suggestedRoleForUser(userId.value))
        }
    }

    override suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit> {
        return runCatching {
            val powerLevelChanges = changes.map { UserPowerLevelUpdate(it.userId.value, it.powerLevel) }
            innerRoom.updatePowerLevelsForUsers(powerLevelChanges)
        }
    }

    override suspend fun powerLevels(): Result<MatrixRoomPowerLevels> = withContext(roomDispatcher) {
        runCatching {
            RoomPowerLevelsMapper.map(innerRoom.getPowerLevels())
        }
    }

    override suspend fun updatePowerLevels(matrixRoomPowerLevels: MatrixRoomPowerLevels): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            val changes = RoomPowerLevelChanges(
                ban = matrixRoomPowerLevels.ban,
                invite = matrixRoomPowerLevels.invite,
                kick = matrixRoomPowerLevels.kick,
                redact = matrixRoomPowerLevels.redactEvents,
                eventsDefault = matrixRoomPowerLevels.sendEvents,
                roomName = matrixRoomPowerLevels.roomName,
                roomAvatar = matrixRoomPowerLevels.roomAvatar,
                roomTopic = matrixRoomPowerLevels.roomTopic,
            )
            innerRoom.applyPowerLevelChanges(changes)
        }
    }

    override suspend fun resetPowerLevels(): Result<MatrixRoomPowerLevels> = withContext(roomDispatcher) {
        runCatching {
            RoomPowerLevelsMapper.map(innerRoom.resetPowerLevels())
        }
    }

    override suspend fun userAvatarUrl(userId: UserId): Result<String?> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.memberAvatarUrl(userId.value)
        }
    }

    override suspend fun sendMessage(body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit> {
        return liveTimeline.sendMessage(body, htmlBody, mentions)
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

    override suspend fun canUserKick(userId: UserId): Result<Boolean> {
        return runCatching {
            innerRoom.canUserKick(userId.value)
        }
    }

    override suspend fun canUserBan(userId: UserId): Result<Boolean> {
        return runCatching {
            innerRoom.canUserBan(userId.value)
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
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendImage(file, thumbnailFile, imageInfo, body, formattedBody, progressCallback)
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        body: String?,
        formattedBody: String?,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendVideo(file, thumbnailFile, videoInfo, body, formattedBody, progressCallback)
    }

    override suspend fun sendAudio(file: File, audioInfo: AudioInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler> {
        return liveTimeline.sendAudio(file, audioInfo, progressCallback)
    }

    override suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler> {
        return liveTimeline.sendFile(file, fileInfo, progressCallback)
    }

    override suspend fun toggleReaction(emoji: String, eventId: EventId): Result<Unit> {
        return liveTimeline.toggleReaction(emoji, eventId)
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> {
        return liveTimeline.forwardEvent(eventId, roomIds)
    }

    override suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit> {
        return liveTimeline.retrySendMessage(transactionId)
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> {
        return liveTimeline.cancelSend(transactionId)
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

    override suspend fun kickUser(userId: UserId, reason: String?): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.kickUser(userId.value, reason)
        }
    }

    override suspend fun banUser(userId: UserId, reason: String?): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.banUser(userId.value, reason)
        }
    }

    override suspend fun unbanUser(userId: UserId, reason: String?): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.unbanUser(userId.value, reason)
        }
    }

    override suspend fun setIsFavorite(isFavorite: Boolean): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.setIsFavourite(isFavorite, null)
        }
    }

    override suspend fun markAsRead(receiptType: ReceiptType): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.markAsRead(receiptType.toRustReceiptType())
        }
    }

    override suspend fun setUnreadFlag(isUnread: Boolean): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.setUnreadFlag(isUnread)
        }
    }

    override suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String?,
        zoomLevel: Int?,
        assetType: AssetType?,
    ): Result<Unit> {
        return liveTimeline.sendLocation(body, geoUri, description, zoomLevel, assetType)
    }

    override suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> {
        return liveTimeline.createPoll(question, answers, maxSelections, pollKind)
    }

    override suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit> {
        return liveTimeline.editPoll(pollStartId, question, answers, maxSelections, pollKind)
    }

    override suspend fun sendPollResponse(
        pollStartId: EventId,
        answers: List<String>
    ): Result<Unit> {
        return liveTimeline.sendPollResponse(pollStartId, answers)
    }

    override suspend fun endPoll(
        pollStartId: EventId,
        text: String
    ): Result<Unit> {
        return liveTimeline.endPoll(pollStartId, text)
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendVoiceMessage(file, audioInfo, waveform, progressCallback)
    }

    override suspend fun typingNotice(isTyping: Boolean) = runCatching {
        innerRoom.typingNotice(isTyping)
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
                    return getElementCallRequiredPermissions(sessionId.value)
                }
            },
        )
    }

    override suspend fun getPermalink(): Result<String> = runCatching {
        innerRoom.matrixToPermalink()
    }

    override suspend fun getPermalinkFor(eventId: EventId): Result<String> = runCatching {
        innerRoom.matrixToEventPermalink(eventId.value)
    }

    override suspend fun sendCallNotificationIfNeeded(): Result<Unit> = runCatching {
        innerRoom.sendCallNotificationIfNeeded()
    }

    private fun createTimeline(
        timeline: InnerTimeline,
        isLive: Boolean,
        onNewSyncedEvent: () -> Unit = {},
    ): Timeline {
        return RustTimeline(
            isKeyBackupEnabled = isKeyBackupEnabled,
            isLive = isLive,
            matrixRoom = this,
            systemClock = systemClock,
            roomCoroutineScope = roomCoroutineScope,
            dispatcher = roomDispatcher,
            lastLoginTimestamp = sessionData.loginTimestamp,
            onNewSyncedEvent = onNewSyncedEvent,
            roomContentForwarder = roomContentForwarder,
            inner = timeline,
        )
    }
}
