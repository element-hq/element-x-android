/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.Interaction
import im.vector.app.features.analytics.plan.PinUnpinAction
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.actionlist.ActionListState
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.link.LinkState
import io.element.android.features.messages.impl.pinned.DefaultPinnedEventsTimelineProvider
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.features.roomcall.api.aStandByCallState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.powerlevels.canPinUnpin
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOther
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOwn
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.ui.room.isDmAsState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class PinnedMessagesListPresenter(
    @Assisted private val navigator: PinnedMessagesListNavigator,
    private val room: JoinedRoom,
    timelineItemsFactoryCreator: TimelineItemsFactory.Creator,
    private val timelineProvider: DefaultPinnedEventsTimelineProvider,
    private val timelineProtectionPresenter: Presenter<TimelineProtectionState>,
    private val linkPresenter: Presenter<LinkState>,
    private val snackbarDispatcher: SnackbarDispatcher,
    @Assisted private val actionListPresenter: Presenter<ActionListState>,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService,
    private val featureFlagService: FeatureFlagService,
    private val htmlConverterProvider: HtmlConverterProvider,
) : Presenter<PinnedMessagesListState> {
    @AssistedFactory
    interface Factory {
        fun create(
            navigator: PinnedMessagesListNavigator,
            actionListPresenter: Presenter<ActionListState>,
        ): PinnedMessagesListPresenter
    }

    private val timelineItemsFactory: TimelineItemsFactory = timelineItemsFactoryCreator.create(
        config = TimelineItemsFactoryConfig(
            computeReadReceipts = false,
            computeReactions = false,
        )
    )

    @Composable
    override fun present(): PinnedMessagesListState {
        htmlConverterProvider.Update()
        val isDm by room.isDmAsState()

        val timelineRoomInfo = remember(isDm) {
            TimelineRoomInfo(
                isDm = isDm,
                name = room.info().name,
                // We don't need to compute those values
                userHasPermissionToSendMessage = false,
                userHasPermissionToSendReaction = false,
                // We do not care about the call state here.
                roomCallState = aStandByCallState(),
                // don't compute this value or the pin icon will be shown
                pinnedEventIds = persistentListOf(),
                typingNotificationState = TypingNotificationState(
                    renderTypingNotifications = false,
                    typingMembers = persistentListOf(),
                    reserveSpace = false,
                ),
                predecessorRoom = room.predecessorRoom(),
            )
        }
        val timelineProtectionState = timelineProtectionPresenter.present()
        val linkState = linkPresenter.present()
        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val userEventPermissions by userEventPermissions(syncUpdateFlow.value)

        val displayThreadSummaries by featureFlagService.isFeatureEnabledFlow(FeatureFlags.Threads).collectAsState(false)

        var pinnedMessageItems by remember {
            mutableStateOf<AsyncData<ImmutableList<TimelineItem>>>(AsyncData.Uninitialized)
        }
        PinnedMessagesListEffect(
            onItemsChange = { newItems ->
                pinnedMessageItems = newItems
            }
        )

        fun handleEvent(event: PinnedMessagesListEvents) {
            when (event) {
                is PinnedMessagesListEvents.HandleAction -> sessionCoroutineScope.handleTimelineAction(event.action, event.event)
            }
        }

        return pinnedMessagesListState(
            timelineRoomInfo = timelineRoomInfo,
            timelineProtectionState = timelineProtectionState,
            linkState = linkState,
            displayThreadSummaries = displayThreadSummaries,
            userEventPermissions = userEventPermissions,
            timelineItems = pinnedMessageItems,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.handleTimelineAction(
        action: TimelineItemAction,
        targetEvent: TimelineItem.Event,
    ) = launch {
        when (action) {
            TimelineItemAction.ViewSource -> {
                navigator.navigateToEventDebugInfo(targetEvent.eventId, targetEvent.debugInfo)
            }
            TimelineItemAction.Forward -> {
                targetEvent.eventId?.let { eventId ->
                    navigator.forwardEvent(eventId)
                }
            }
            TimelineItemAction.Unpin -> handleUnpinAction(targetEvent)
            TimelineItemAction.ViewInTimeline -> {
                targetEvent.eventId?.let { eventId ->
                    analyticsService.captureInteraction(Interaction.Name.PinnedMessageListViewTimeline)
                    navigator.viewInTimeline(eventId)
                }
            }
            else -> Unit
        }
    }

    private suspend fun handleUnpinAction(targetEvent: TimelineItem.Event) {
        if (targetEvent.eventId == null) return
        analyticsService.capture(
            PinUnpinAction(
                from = PinUnpinAction.From.MessagePinningList,
                kind = PinUnpinAction.Kind.Unpin,
            )
        )
        timelineProvider.invokeOnTimeline {
            unpinEvent(targetEvent.eventId)
                .onFailure {
                    Timber.e(it, "Failed to unpin event ${targetEvent.eventId}")
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_error))
                }
        }
    }

    @Composable
    private fun userEventPermissions(updateKey: Long): State<UserEventPermissions> {
        return produceState(UserEventPermissions.DEFAULT, key1 = updateKey) {
            value = UserEventPermissions(
                canSendMessage = false,
                canSendReaction = false,
                canRedactOwn = room.canRedactOwn().getOrElse { false },
                canRedactOther = room.canRedactOther().getOrElse { false },
                canPinUnpin = room.canPinUnpin().getOrElse { false },
            )
        }
    }

    @Composable
    private fun PinnedMessagesListEffect(onItemsChange: (AsyncData<ImmutableList<TimelineItem>>) -> Unit) {
        val updatedOnItemsChange by rememberUpdatedState(onItemsChange)

        val timelineState by timelineProvider.timelineStateFlow.collectAsState()

        LaunchedEffect(timelineState) {
            when (val asyncTimeline = timelineState) {
                AsyncData.Uninitialized -> flowOf(AsyncData.Uninitialized)
                is AsyncData.Failure -> flowOf(AsyncData.Failure(asyncTimeline.error))
                is AsyncData.Loading -> flowOf(AsyncData.Loading())
                is AsyncData.Success -> {
                    val timelineItemsFlow = asyncTimeline.data.timelineItems
                    combine(timelineItemsFlow, room.membersStateFlow) { items, membersState ->
                        timelineItemsFactory.replaceWith(
                            timelineItems = items,
                            roomMembers = membersState.roomMembers().orEmpty()
                        )
                    }.launchIn(this)

                    timelineItemsFactory.timelineItems.map { timelineItems ->
                        AsyncData.Success(timelineItems)
                    }
                }
            }
                .onEach { items ->
                    updatedOnItemsChange(items)
                }
                .launchIn(this)
        }
    }

    @Composable
    private fun pinnedMessagesListState(
        timelineRoomInfo: TimelineRoomInfo,
        timelineProtectionState: TimelineProtectionState,
        displayThreadSummaries: Boolean,
        linkState: LinkState,
        userEventPermissions: UserEventPermissions,
        timelineItems: AsyncData<ImmutableList<TimelineItem>>,
        eventSink: (PinnedMessagesListEvents) -> Unit
    ): PinnedMessagesListState {
        return when (timelineItems) {
            AsyncData.Uninitialized, is AsyncData.Loading -> PinnedMessagesListState.Loading
            is AsyncData.Failure -> PinnedMessagesListState.Failed
            is AsyncData.Success -> {
                if (timelineItems.data.isEmpty()) {
                    PinnedMessagesListState.Empty
                } else {
                    val actionListState = actionListPresenter.present()
                    PinnedMessagesListState.Filled(
                        timelineRoomInfo = timelineRoomInfo,
                        timelineProtectionState = timelineProtectionState,
                        displayThreadSummaries = displayThreadSummaries,
                        linkState = linkState,
                        userEventPermissions = userEventPermissions,
                        timelineItems = timelineItems.data,
                        actionListState = actionListState,
                        eventSink = eventSink
                    )
                }
            }
        }
    }
}
