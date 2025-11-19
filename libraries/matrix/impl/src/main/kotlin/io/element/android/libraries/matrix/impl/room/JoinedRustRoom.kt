/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.room.roomNotificationSettings
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import io.element.android.libraries.matrix.impl.core.RustSendHandle
import io.element.android.libraries.matrix.impl.mapper.map
import io.element.android.libraries.matrix.impl.room.history.map
import io.element.android.libraries.matrix.impl.room.join.map
import io.element.android.libraries.matrix.impl.room.knock.RustKnockRequest
import io.element.android.libraries.matrix.impl.room.member.RoomMemberListFetcher
import io.element.android.libraries.matrix.impl.roomdirectory.map
import io.element.android.libraries.matrix.impl.timeline.RustTimeline
import io.element.android.libraries.matrix.impl.util.MessageEventContent
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import io.element.android.libraries.matrix.impl.widget.RustWidgetDriver
import io.element.android.libraries.matrix.impl.widget.generateWidgetWebViewUrl
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.DateDividerMode
import org.matrix.rustcomponents.sdk.IdentityStatusChangeListener
import org.matrix.rustcomponents.sdk.KnockRequestsListener
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
import kotlin.coroutines.cancellation.CancellationException
import org.matrix.rustcomponents.sdk.IdentityStatusChange as RustIdentityStateChange
import org.matrix.rustcomponents.sdk.KnockRequest as InnerKnockRequest
import org.matrix.rustcomponents.sdk.Timeline as InnerTimeline

class JoinedRustRoom(
    private val baseRoom: RustBaseRoom,
    private val liveInnerTimeline: InnerTimeline,
    private val notificationSettingsService: NotificationSettingsService,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val systemClock: SystemClock,
    private val roomContentForwarder: RoomContentForwarder,
    private val featureFlagService: FeatureFlagService,
) : JoinedRoom, BaseRoom by baseRoom {
    // Create a dispatcher for all room methods...
    private val roomDispatcher = coroutineDispatchers.io.limitedParallelism(32)
    private val innerRoom = baseRoom.innerRoom

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

    override val roomNotificationSettingsStateFlow = MutableStateFlow<RoomNotificationSettingsState>(RoomNotificationSettingsState.Unknown)

    override val liveTimeline = liveInnerTimeline.map(mode = Timeline.Mode.Live)

    override val syncUpdateFlow = flow {
        var counter = 0L
        liveTimeline.onSyncedEventReceived.collect {
            emit(++counter)
        }
    }.stateIn(
        scope = roomCoroutineScope,
        started = WhileSubscribed(),
        initialValue = 0L,
    )

    init {
        subscribeToRoomMembersChange()
    }

    private fun subscribeToRoomMembersChange() {
        val powerLevelChanges = roomInfoFlow.map { it.roomPowerLevels }.distinctUntilChanged()
        val membershipChanges = liveTimeline.membershipChangeEventReceived.onStart { emit(Unit) }
        combine(membershipChanges, powerLevelChanges) { _, _ -> }
            // Skip initial one
            .drop(1)
            // The new events should already be in the SDK cache, no need to fetch them from the server
            .onEach { baseRoom.roomMemberListFetcher.fetchRoomMembers(source = RoomMemberListFetcher.Source.CACHE) }
            .launchIn(roomCoroutineScope)
            .invokeOnCompletion {
                Timber.d("Observing membership changes for room $roomId stopped, reason: $it")
            }
    }

    override suspend fun createTimeline(
        createTimelineParams: CreateTimelineParams,
    ): Result<Timeline> = withContext(roomDispatcher) {
        val hideThreadedEvents = featureFlagService.isFeatureEnabled(FeatureFlags.Threads)
        val focus = when (createTimelineParams) {
            is CreateTimelineParams.PinnedOnly -> TimelineFocus.PinnedEvents(
                maxEventsToLoad = 100u,
                maxConcurrentRequests = 10u,
            )
            is CreateTimelineParams.MediaOnly -> TimelineFocus.Live(hideThreadedEvents = hideThreadedEvents)
            is CreateTimelineParams.Focused -> TimelineFocus.Event(
                eventId = createTimelineParams.focusedEventId.value,
                numContextEvents = 50u,
                hideThreadedEvents = hideThreadedEvents,
            )
            is CreateTimelineParams.MediaOnlyFocused -> TimelineFocus.Event(
                eventId = createTimelineParams.focusedEventId.value,
                numContextEvents = 50u,
                // Never hide threaded events in media focused timeline
                hideThreadedEvents = false,
            )
            is CreateTimelineParams.Threaded -> TimelineFocus.Thread(
                rootEventId = createTimelineParams.threadRootEventId.value,
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
            CreateTimelineParams.PinnedOnly,
            is CreateTimelineParams.Threaded -> TimelineFilter.All
        }

        val internalIdPrefix = when (createTimelineParams) {
            is CreateTimelineParams.PinnedOnly -> "pinned_events"
            is CreateTimelineParams.Focused -> "focus_${createTimelineParams.focusedEventId}"
            is CreateTimelineParams.MediaOnly -> "MediaGallery_"
            is CreateTimelineParams.MediaOnlyFocused -> "MediaGallery_${createTimelineParams.focusedEventId}"
            is CreateTimelineParams.Threaded -> "Thread_${createTimelineParams.threadRootEventId}"
        }

        // Note that for TimelineFilter.MediaOnlyFocused, the date separator will be filtered out,
        // but there is no way to exclude data separator at the moment.
        val dateDividerMode = when (createTimelineParams) {
            is CreateTimelineParams.MediaOnly,
            is CreateTimelineParams.MediaOnlyFocused -> DateDividerMode.MONTHLY
            is CreateTimelineParams.Focused,
            CreateTimelineParams.PinnedOnly,
            is CreateTimelineParams.Threaded -> DateDividerMode.DAILY
        }

        // Track read receipts only for focused timeline for performance optimization
        val trackReadReceipts = createTimelineParams is CreateTimelineParams.Focused

        runCatchingExceptions {
            innerRoom.timelineWithConfiguration(
                configuration = TimelineConfiguration(
                    focus = focus,
                    filter = filter,
                    internalIdPrefix = internalIdPrefix,
                    dateDividerMode = dateDividerMode,
                    trackReadReceipts = trackReadReceipts,
                    reportUtds = true,
                )
            ).let { innerTimeline ->
                val mode = when (createTimelineParams) {
                    is CreateTimelineParams.Focused -> Timeline.Mode.FocusedOnEvent(createTimelineParams.focusedEventId)
                    is CreateTimelineParams.MediaOnly -> Timeline.Mode.Media
                    is CreateTimelineParams.MediaOnlyFocused -> Timeline.Mode.FocusedOnEvent(createTimelineParams.focusedEventId)
                    CreateTimelineParams.PinnedOnly -> Timeline.Mode.PinnedEvents
                    is CreateTimelineParams.Threaded -> Timeline.Mode.Thread(createTimelineParams.threadRootEventId)
                }
                innerTimeline.map(mode = mode)
            }
        }.mapFailure {
            when (createTimelineParams) {
                is CreateTimelineParams.Focused,
                is CreateTimelineParams.MediaOnlyFocused,
                is CreateTimelineParams.Threaded -> it.toFocusEventException()
                CreateTimelineParams.MediaOnly,
                CreateTimelineParams.PinnedOnly -> it
            }
        }.onFailure {
            if (it is CancellationException) {
                throw it
            }
        }
    }

    override suspend fun editMessage(
        eventId: EventId,
        body: String,
        htmlBody: String?,
        intentionalMentions: List<IntentionalMention>
    ): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            MessageEventContent.from(body, htmlBody, intentionalMentions).use { newContent ->
                innerRoom.edit(eventId.value, newContent)
            }
        }
    }

    override suspend fun typingNotice(isTyping: Boolean) = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.typingNotice(isTyping)
        }
    }

    override suspend fun inviteUserById(id: UserId): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.inviteUserById(id.value)
        }
    }

    override suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.uploadAvatar(mimeType, data, null)
        }
    }

    override suspend fun removeAvatar(): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.removeAvatar()
        }
    }

    override suspend fun setName(name: String): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.setName(name)
        }
    }

    override suspend fun setTopic(topic: String): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.setTopic(topic)
        }
    }

    override suspend fun reportContent(eventId: EventId, reason: String, blockUserId: UserId?): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.reportContent(eventId = eventId.value, score = null, reason = reason)
            if (blockUserId != null) {
                innerRoom.ignoreUser(blockUserId.value)
            }
        }
    }

    override suspend fun updateRoomNotificationSettings(): Result<Unit> = withContext(roomDispatcher) {
        val currentState = roomNotificationSettingsStateFlow.value
        val currentRoomNotificationSettings = currentState.roomNotificationSettings()
        roomNotificationSettingsStateFlow.value = RoomNotificationSettingsState.Pending(prevRoomNotificationSettings = currentRoomNotificationSettings)
        runCatchingExceptions {
            val isEncrypted = roomInfoFlow.value.isEncrypted ?: getUpdatedIsEncrypted().getOrThrow()
            notificationSettingsService.getRoomNotificationSettings(roomId, isEncrypted, isOneToOne).getOrThrow()
        }.map {
            roomNotificationSettingsStateFlow.value = RoomNotificationSettingsState.Ready(it)
        }.onFailure {
            roomNotificationSettingsStateFlow.value = RoomNotificationSettingsState.Error(
                prevRoomNotificationSettings = currentRoomNotificationSettings,
                failure = it
            )
        }
    }

    override suspend fun updateCanonicalAlias(canonicalAlias: RoomAlias?, alternativeAliases: List<RoomAlias>): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.updateCanonicalAlias(canonicalAlias?.value, alternativeAliases.map { it.value })
        }
    }

    override suspend fun publishRoomAliasInRoomDirectory(roomAlias: RoomAlias): Result<Boolean> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.publishRoomAliasInRoomDirectory(roomAlias.value)
        }
    }

    override suspend fun removeRoomAliasFromRoomDirectory(roomAlias: RoomAlias): Result<Boolean> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.removeRoomAliasFromRoomDirectory(roomAlias.value)
        }
    }

    override suspend fun updateRoomVisibility(roomVisibility: RoomVisibility): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.updateRoomVisibility(roomVisibility.map())
        }
    }

    override suspend fun updateHistoryVisibility(historyVisibility: RoomHistoryVisibility): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.updateHistoryVisibility(historyVisibility.map())
        }
    }

    override suspend fun enableEncryption(): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.enableEncryption()
        }
    }

    override suspend fun updateJoinRule(joinRule: JoinRule): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.updateJoinRules(joinRule.map())
        }
    }

    override suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit> {
        return runCatchingExceptions {
            val powerLevelChanges = changes.map { UserPowerLevelUpdate(it.userId.value, it.powerLevel) }
            innerRoom.updatePowerLevelsForUsers(powerLevelChanges)
        }
    }

    override suspend fun updatePowerLevels(roomPowerLevelsValues: RoomPowerLevelsValues): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            val changes = RoomPowerLevelChanges(
                ban = roomPowerLevelsValues.ban,
                invite = roomPowerLevelsValues.invite,
                kick = roomPowerLevelsValues.kick,
                redact = roomPowerLevelsValues.redactEvents,
                eventsDefault = roomPowerLevelsValues.sendEvents,
                roomName = roomPowerLevelsValues.roomName,
                roomAvatar = roomPowerLevelsValues.roomAvatar,
                roomTopic = roomPowerLevelsValues.roomTopic,
            )
            innerRoom.applyPowerLevelChanges(changes)
        }
    }

    override suspend fun resetPowerLevels(): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.resetPowerLevels().let {}
        }
    }

    override suspend fun kickUser(userId: UserId, reason: String?): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.kickUser(userId.value, reason)
        }
    }

    override suspend fun banUser(userId: UserId, reason: String?): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.banUser(userId.value, reason)
        }
    }

    override suspend fun unbanUser(userId: UserId, reason: String?): Result<Unit> = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.unbanUser(userId.value, reason)
        }
    }

    override suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ) = withContext(roomDispatcher) {
        runCatchingExceptions {
            widgetSettings.generateWidgetWebViewUrl(innerRoom, clientId, languageTag, theme)
        }
    }

    override fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver> {
        return runCatchingExceptions {
            RustWidgetDriver(
                widgetSettings = widgetSettings,
                room = innerRoom,
                widgetCapabilitiesProvider = object : WidgetCapabilitiesProvider {
                    override fun acquireCapabilities(capabilities: WidgetCapabilities): WidgetCapabilities {
                        return getElementCallRequiredPermissions(sessionId.value, baseRoom.deviceId.value)
                    }
                },
            )
        }
    }

    override suspend fun setSendQueueEnabled(enabled: Boolean) {
        withContext(roomDispatcher) {
            Timber.d("setSendQueuesEnabled: $enabled")
            runCatchingExceptions {
                innerRoom.enableSendQueue(enabled)
            }
        }
    }

    override suspend fun ignoreDeviceTrustAndResend(devices: Map<UserId, List<DeviceId>>, sendHandle: SendHandle) = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.ignoreDeviceTrustAndResend(
                devices = devices.entries.associate { entry ->
                    entry.key.value to entry.value.map { it.value }
                },
                sendHandle = (sendHandle as RustSendHandle).inner,
            )
        }
    }

    override suspend fun withdrawVerificationAndResend(userIds: List<UserId>, sendHandle: SendHandle) = withContext(roomDispatcher) {
        runCatchingExceptions {
            innerRoom.withdrawVerificationAndResend(
                userIds = userIds.map { it.value },
                sendHandle = (sendHandle as RustSendHandle).inner,
            )
        }
    }

    override fun close() = destroy()

    override fun destroy() {
        baseRoom.destroy()
        liveInnerTimeline.destroy()
        Timber.d("Room $roomId destroyed")
    }

    private fun InnerTimeline.map(
        mode: Timeline.Mode,
    ): Timeline {
        val timelineCoroutineScope = roomCoroutineScope.childScope(coroutineDispatchers.main, "TimelineScope-$roomId-$this")
        return RustTimeline(
            mode = mode,
            joinedRoom = this@JoinedRustRoom,
            inner = this@map,
            systemClock = systemClock,
            coroutineScope = timelineCoroutineScope,
            dispatcher = roomDispatcher,
            roomContentForwarder = roomContentForwarder,
        )
    }
}
