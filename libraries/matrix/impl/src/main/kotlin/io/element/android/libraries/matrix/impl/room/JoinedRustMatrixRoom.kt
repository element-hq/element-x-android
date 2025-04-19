/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.JoinedMatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.message.ReplyParameters
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.room.roomNotificationSettings
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.libraries.matrix.impl.core.RustSendHandle
import io.element.android.libraries.matrix.impl.mapper.map
import io.element.android.libraries.matrix.impl.room.history.map
import io.element.android.libraries.matrix.impl.room.join.map
import io.element.android.libraries.matrix.impl.room.knock.RustKnockRequest
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher
import io.element.android.libraries.matrix.impl.room.powerlevels.RoomPowerLevelsMapper
import io.element.android.libraries.matrix.impl.roomdirectory.map
import io.element.android.libraries.matrix.impl.timeline.RustTimeline
import io.element.android.libraries.matrix.impl.util.MessageEventContent
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import io.element.android.libraries.matrix.impl.widget.RustWidgetDriver
import io.element.android.libraries.matrix.impl.widget.generateWidgetWebViewUrl
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.DateDividerMode
import org.matrix.rustcomponents.sdk.IdentityStatusChangeListener
import org.matrix.rustcomponents.sdk.KnockRequestsListener
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomInfoListener
import org.matrix.rustcomponents.sdk.RoomMessageEventMessageType
import org.matrix.rustcomponents.sdk.TimelineConfiguration
import org.matrix.rustcomponents.sdk.TimelineFilter
import org.matrix.rustcomponents.sdk.TimelineFocus
import org.matrix.rustcomponents.sdk.TypingNotificationsListener
import org.matrix.rustcomponents.sdk.UserPowerLevelUpdate
import org.matrix.rustcomponents.sdk.WidgetCapabilities
import org.matrix.rustcomponents.sdk.WidgetCapabilitiesProvider
import org.matrix.rustcomponents.sdk.getElementCallRequiredPermissions
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import uniffi.matrix_sdk.RoomPowerLevelChanges
import java.io.File
import kotlin.collections.map
import kotlin.coroutines.cancellation.CancellationException
import org.matrix.rustcomponents.sdk.IdentityStatusChange as RustIdentityStateChange
import org.matrix.rustcomponents.sdk.KnockRequest as InnerKnockRequest
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

class JoinedRustMatrixRoom(
    private val matrixRoom: RustMatrixRoom,
    private val liveInnerTimeline: InnerTimeline,
    private val notificationSettingsService: NotificationSettingsService,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val matrixRoomInfoMapper: MatrixRoomInfoMapper,
    private val systemClock: SystemClock,
    private val roomContentForwarder: RoomContentForwarder,
    private val featureFlagService: FeatureFlagService,
) : JoinedMatrixRoom, MatrixRoom by matrixRoom {
    // Create a dispatcher for all room methods...
    private val roomDispatcher = coroutineDispatchers.io.limitedParallelism(32)
    private val innerRoom = matrixRoom.innerRoom

    override val syncUpdateFlow = MutableStateFlow(0L)

    override val roomInfoFlow: StateFlow<MatrixRoomInfo> = mxCallbackFlow {
        innerRoom.subscribeToRoomInfoUpdates(object : RoomInfoListener {
            override fun call(roomInfo: RoomInfo) {
                channel.trySend(matrixRoomInfoMapper.map(roomInfo))
            }
        })
    }.stateIn(roomCoroutineScope, started = SharingStarted.Lazily, initialValue = matrixRoom.info())

    override val roomTypingMembersFlow: Flow<List<UserId>> = mxCallbackFlow {
        val initial = emptyList<UserId>()
        channel.trySend(initial)
        innerRoom.subscribeToTypingNotifications(object : TypingNotificationsListener {
            override fun call(typingUserIds: List<String>) {
                channel.trySend(
                    typingUserIds
                        .filter { it != sessionId.value }
                        .map(::UserId)
                )
            }
        })
    }

    override val identityStateChangesFlow: Flow<List<IdentityStateChange>> = mxCallbackFlow {
        val initial = emptyList<IdentityStateChange>()
        channel.trySend(initial)
        innerRoom.subscribeToIdentityStatusChanges(object : IdentityStatusChangeListener {
            override fun call(identityStatusChange: List<RustIdentityStateChange>) {
                channel.trySend(
                    identityStatusChange.map {
                        IdentityStateChange(
                            userId = UserId(it.userId),
                            identityState = it.changedTo.map(),
                        )
                    }
                )
            }
        })
    }

    override val knockRequestsFlow: Flow<List<KnockRequest>> = mxCallbackFlow {
        innerRoom.subscribeToKnockRequests(object : KnockRequestsListener {
            override fun call(joinRequests: List<InnerKnockRequest>) {
                val knockRequests = joinRequests.map { RustKnockRequest(it) }
                channel.trySend(knockRequests)
            }
        })
    }

    override val roomNotificationSettingsStateFlow = MutableStateFlow<MatrixRoomNotificationSettingsState>(MatrixRoomNotificationSettingsState.Unknown)

    override val liveTimeline = liveInnerTimeline.map(mode = Timeline.Mode.LIVE) {
        syncUpdateFlow.value = systemClock.epochMillis()
    }

    init {
        val powerLevelChanges = roomInfoFlow.map { it.userPowerLevels }.distinctUntilChanged()
        val membershipChanges = liveTimeline.membershipChangeEventReceived.onStart { emit(Unit) }
        combine(membershipChanges, powerLevelChanges) { _, _ -> }
            // Skip initial one
            .drop(1)
            // The new events should already be in the SDK cache, no need to fetch them from the server
            .onEach { matrixRoom.roomMemberListFetcher.fetchRoomMembers(source = RoomMemberListFetcher.Source.CACHE) }
            .launchIn(roomCoroutineScope)
            .invokeOnCompletion {
                Timber.d("Observing membership changes for room $roomId stopped, reason: $it")
            }
    }

    override suspend fun createTimeline(
        createTimelineParams: CreateTimelineParams,
    ): Result<Timeline> = withContext(roomDispatcher) {
        val focus = when (createTimelineParams) {
            is CreateTimelineParams.PinnedOnly -> TimelineFocus.PinnedEvents(
                maxEventsToLoad = 100u,
                maxConcurrentRequests = 10u,
            )
            is CreateTimelineParams.MediaOnly -> TimelineFocus.Live
            is CreateTimelineParams.Focused -> TimelineFocus.Event(
                eventId = createTimelineParams.focusedEventId.value,
                numContextEvents = 50u,
            )
            is CreateTimelineParams.MediaOnlyFocused -> TimelineFocus.Event(
                eventId = createTimelineParams.focusedEventId.value,
                numContextEvents = 50u,
            )
        }

        val filter = when (createTimelineParams) {
            is CreateTimelineParams.MediaOnly,
            is CreateTimelineParams.MediaOnlyFocused -> TimelineFilter.OnlyMessage(
                types = listOf(
                    RoomMessageEventMessageType.FILE,
                    RoomMessageEventMessageType.IMAGE,
                    RoomMessageEventMessageType.VIDEO,
                    RoomMessageEventMessageType.AUDIO,
                )
            )
            is CreateTimelineParams.Focused,
            CreateTimelineParams.PinnedOnly -> TimelineFilter.All
        }

        val internalIdPrefix = when (createTimelineParams) {
            is CreateTimelineParams.PinnedOnly -> "pinned_events"
            is CreateTimelineParams.Focused -> "focus_${createTimelineParams.focusedEventId}"
            is CreateTimelineParams.MediaOnly -> "MediaGallery_"
            is CreateTimelineParams.MediaOnlyFocused -> "MediaGallery_${createTimelineParams.focusedEventId}"
        }

        // Note that for TimelineFilter.MediaOnlyFocused, the date separator will be filtered out,
        // but there is no way to exclude data separator at the moment.
        val dateDividerMode = when (createTimelineParams) {
            is CreateTimelineParams.MediaOnly,
            is CreateTimelineParams.MediaOnlyFocused -> DateDividerMode.MONTHLY
            is CreateTimelineParams.Focused,
            CreateTimelineParams.PinnedOnly -> DateDividerMode.DAILY
        }

        // Track read receipts only for focused timeline for performance optimization
        val trackReadReceipts = createTimelineParams is CreateTimelineParams.Focused

        runCatching {
            innerRoom.timelineWithConfiguration(
                configuration = TimelineConfiguration(
                    focus = focus,
                    filter = filter,
                    internalIdPrefix = internalIdPrefix,
                    dateDividerMode = dateDividerMode,
                    trackReadReceipts = trackReadReceipts,
                )
            ).let { innerTimeline ->
                val mode = when (createTimelineParams) {
                    is CreateTimelineParams.Focused -> Timeline.Mode.FOCUSED_ON_EVENT
                    is CreateTimelineParams.MediaOnly -> Timeline.Mode.MEDIA
                    is CreateTimelineParams.MediaOnlyFocused -> Timeline.Mode.FOCUSED_ON_EVENT
                    CreateTimelineParams.PinnedOnly -> Timeline.Mode.PINNED_EVENTS
                }
                innerTimeline.map(mode = mode)
            }
        }.mapFailure {
            when (createTimelineParams) {
                is CreateTimelineParams.Focused,
                is CreateTimelineParams.MediaOnlyFocused -> it.toFocusEventException()
                CreateTimelineParams.MediaOnly,
                CreateTimelineParams.PinnedOnly -> it
            }
        }.onFailure {
            if (it is CancellationException) {
                throw it
            }
        }
    }

    override suspend fun sendMessage(body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>): Result<Unit> {
        return liveTimeline.sendMessage(body, htmlBody, intentionalMentions)
    }

    override suspend fun editMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>
    ): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            MessageEventContent.from(body, htmlBody, intentionalMentions).use { newContent ->
                innerRoom.edit(eventId.value, newContent)
            }
        }
    }

    override suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendImage(
            file = file,
            thumbnailFile = thumbnailFile,
            imageInfo = imageInfo,
            caption = caption,
            formattedCaption = formattedCaption,
            progressCallback = progressCallback,
            replyParameters = replyParameters
        )
    }

    override suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendVideo(
            file = file,
            thumbnailFile = thumbnailFile,
            videoInfo = videoInfo,
            caption = caption,
            formattedCaption = formattedCaption,
            progressCallback = progressCallback,
            replyParameters = replyParameters
        )
    }

    override suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendAudio(
            file = file,
            audioInfo = audioInfo,
            caption = caption,
            formattedCaption = formattedCaption,
            progressCallback = progressCallback,
            replyParameters = replyParameters,
        )
    }

    override suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendFile(
            file = file,
            fileInfo = fileInfo,
            caption = caption,
            formattedCaption = formattedCaption,
            progressCallback = progressCallback,
            replyParameters = replyParameters,
        )
    }

    override suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler> {
        return liveTimeline.sendVoiceMessage(
            file = file,
            audioInfo = audioInfo,
            waveform = waveform,
            progressCallback = progressCallback,
            replyParameters = replyParameters,
        )
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

    override suspend fun typingNotice(isTyping: Boolean) = withContext(roomDispatcher) {
        runCatching {
            innerRoom.typingNotice(isTyping)
        }
    }

    override suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit> {
        return liveTimeline.toggleReaction(emoji, eventOrTransactionId)
    }

    override suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit> {
        return liveTimeline.forwardEvent(eventId, roomIds)
    }

    override suspend fun cancelSend(transactionId: TransactionId): Result<Unit> {
        return liveTimeline.cancelSend(transactionId)
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.inviteUserById(id.value)
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

    override suspend fun updateRoomNotificationSettings(): Result<Unit> = withContext(roomDispatcher) {
        val currentState = roomNotificationSettingsStateFlow.value
        val currentRoomNotificationSettings = currentState.roomNotificationSettings()
        roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Pending(prevRoomNotificationSettings = currentRoomNotificationSettings)
        runCatching {
            val isEncrypted = roomInfoFlow.value.isEncrypted ?: getUpdatedIsEncrypted().getOrThrow()
            notificationSettingsService.getRoomNotificationSettings(roomId, isEncrypted, isOneToOne).getOrThrow()
        }.map {
            roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Ready(it)
        }.onFailure {
            roomNotificationSettingsStateFlow.value = MatrixRoomNotificationSettingsState.Error(
                prevRoomNotificationSettings = currentRoomNotificationSettings,
                failure = it
            )
        }
    }

    override suspend fun updateCanonicalAlias(canonicalAlias: RoomAlias?, alternativeAliases: List<RoomAlias>): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.updateCanonicalAlias(canonicalAlias?.value, alternativeAliases.map { it.value })
        }
    }

    override suspend fun publishRoomAliasInRoomDirectory(roomAlias: RoomAlias): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.publishRoomAliasInRoomDirectory(roomAlias.value)
        }
    }

    override suspend fun removeRoomAliasFromRoomDirectory(roomAlias: RoomAlias): Result<Boolean> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.removeRoomAliasFromRoomDirectory(roomAlias.value)
        }
    }

    override suspend fun updateRoomVisibility(roomVisibility: RoomVisibility): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.updateRoomVisibility(roomVisibility.map())
        }
    }

    override suspend fun updateHistoryVisibility(historyVisibility: RoomHistoryVisibility): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.updateHistoryVisibility(historyVisibility.map())
        }
    }

    override suspend fun enableEncryption(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.enableEncryption()
        }
    }

    override suspend fun updateJoinRule(joinRule: JoinRule): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.updateJoinRules(joinRule.map())
        }
    }

    override suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit> {
        return runCatching {
            val powerLevelChanges = changes.map { UserPowerLevelUpdate(it.userId.value, it.powerLevel) }
            innerRoom.updatePowerLevelsForUsers(powerLevelChanges)
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

    override suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ) = withContext(roomDispatcher) {
        runCatching {
            widgetSettings.generateWidgetWebViewUrl(innerRoom, clientId, languageTag, theme)
        }
    }

    override fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver> {
        return runCatching {
            RustWidgetDriver(
                widgetSettings = widgetSettings,
                room = innerRoom,
                widgetCapabilitiesProvider = object : WidgetCapabilitiesProvider {
                    override fun acquireCapabilities(capabilities: WidgetCapabilities): WidgetCapabilities {
                        return getElementCallRequiredPermissions(sessionId.value, matrixRoom.deviceId.value)
                    }
                },
            )
        }
    }

    override suspend fun sendCallNotificationIfNeeded(): Result<Unit> = withContext(roomDispatcher) {
        runCatching {
            innerRoom.sendCallNotificationIfNeeded()
        }
    }

    override suspend fun setSendQueueEnabled(enabled: Boolean) {
        withContext(roomDispatcher) {
            Timber.d("setSendQueuesEnabled: $enabled")
            runCatching {
                innerRoom.enableSendQueue(enabled)
            }
        }
    }

    override suspend fun ignoreDeviceTrustAndResend(devices: Map<UserId, List<DeviceId>>, sendHandle: SendHandle) = withContext(roomDispatcher) {
        runCatching {
            innerRoom.ignoreDeviceTrustAndResend(
                devices = devices.entries.associate { entry ->
                    entry.key.value to entry.value.map { it.value }
                },
                sendHandle = (sendHandle as RustSendHandle).inner,
            )
        }
    }

    override suspend fun withdrawVerificationAndResend(userIds: List<UserId>, sendHandle: SendHandle) = withContext(roomDispatcher) {
        runCatching {
            innerRoom.withdrawVerificationAndResend(
                userIds = userIds.map { it.value },
                sendHandle = (sendHandle as RustSendHandle).inner,
            )
        }
    }

    override fun destroy() {
        matrixRoom.destroy()
        liveInnerTimeline.close()
        roomCoroutineScope.cancel()
    }

    private fun InnerTimeline.map(
        mode: Timeline.Mode,
        onNewSyncedEvent: () -> Unit = {},
    ): Timeline {
        val timelineCoroutineScope = roomCoroutineScope.childScope(coroutineDispatchers.main, "TimelineScope-$roomId-$this")
        return RustTimeline(
            mode = mode,
            matrixRoom = this@JoinedRustMatrixRoom,
            inner = this@map,
            systemClock = systemClock,
            coroutineScope = timelineCoroutineScope,
            dispatcher = roomDispatcher,
            roomContentForwarder = roomContentForwarder,
            onNewSyncedEvent = onNewSyncedEvent,
            featureFlagsService = featureFlagService,
        )
    }
}
