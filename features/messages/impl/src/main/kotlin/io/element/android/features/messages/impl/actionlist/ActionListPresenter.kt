/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionComparator
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailureFactory
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.canBeCopied
import io.element.android.features.messages.impl.timeline.model.event.canBeForwarded
import io.element.android.features.messages.impl.timeline.model.event.canReact
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.recentemojis.api.GetRecentEmojis
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface ActionListPresenter : Presenter<ActionListState> {
    interface Factory {
        fun create(
            postProcessor: TimelineItemActionPostProcessor,
            timelineMode: Timeline.Mode,
        ): ActionListPresenter
    }
}

@AssistedInject
class DefaultActionListPresenter(
    @Assisted
    private val postProcessor: TimelineItemActionPostProcessor,
    @Assisted
    private val timelineMode: Timeline.Mode,
    private val appPreferencesStore: AppPreferencesStore,
    private val room: BaseRoom,
    private val userSendFailureFactory: VerifiedUserSendFailureFactory,
    private val dateFormatter: DateFormatter,
    private val featureFlagService: FeatureFlagService,
    private val getRecentEmojis: GetRecentEmojis,
) : ActionListPresenter {
    @AssistedFactory
    @ContributesBinding(RoomScope::class)
    interface Factory : ActionListPresenter.Factory {
        override fun create(
            postProcessor: TimelineItemActionPostProcessor,
            timelineMode: Timeline.Mode,
        ): DefaultActionListPresenter
    }

    private val comparator = TimelineItemActionComparator()

    private val suggestedEmojis = persistentListOf("👍️", "👎️", "🔥", "❤️", "👏")

    @Composable
    override fun present(): ActionListState {
        val localCoroutineScope = rememberCoroutineScope()

        val target: MutableState<ActionListState.Target> = remember {
            mutableStateOf(ActionListState.Target.None)
        }

        val isDeveloperModeEnabled by remember {
            appPreferencesStore.isDeveloperModeEnabledFlow()
        }.collectAsState(initial = false)
        val pinnedEventIds by remember {
            room.roomInfoFlow.map { it.pinnedEventIds }
        }.collectAsState(initial = persistentListOf())

        val isThreadsEnabled = featureFlagService.isFeatureEnabledFlow(FeatureFlags.Threads).collectAsState(false)

        fun handleEvent(event: ActionListEvents) {
            when (event) {
                ActionListEvents.Clear -> target.value = ActionListState.Target.None
                is ActionListEvents.ComputeForMessage -> localCoroutineScope.computeForMessage(
                    timelineItem = event.event,
                    usersEventPermissions = event.userEventPermissions,
                    isDeveloperModeEnabled = isDeveloperModeEnabled,
                    pinnedEventIds = pinnedEventIds,
                    target = target,
                    isThreadsEnabled = isThreadsEnabled.value,
                )
            }
        }

        return ActionListState(
            target = target.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.computeForMessage(
        timelineItem: TimelineItem.Event,
        usersEventPermissions: UserEventPermissions,
        isDeveloperModeEnabled: Boolean,
        pinnedEventIds: ImmutableList<EventId>,
        target: MutableState<ActionListState.Target>,
        isThreadsEnabled: Boolean,
    ) = launch {
        target.value = ActionListState.Target.Loading(timelineItem)

        val actions = buildActions(
            timelineItem = timelineItem,
            usersEventPermissions = usersEventPermissions,
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            isEventPinned = pinnedEventIds.contains(timelineItem.eventId),
            isThreadsEnabled = isThreadsEnabled,
        )

        val verifiedUserSendFailure = userSendFailureFactory.create(timelineItem.localSendState)
        val displayEmojiReactions = usersEventPermissions.canSendReaction && timelineItem.content.canReact()

        if (actions.isNotEmpty() || displayEmojiReactions || verifiedUserSendFailure != VerifiedUserSendFailure.None) {
            val recentEmojis = getRecentEmojis().getOrNull()?.toImmutableList() ?: persistentListOf()
            target.value = ActionListState.Target.Success(
                event = timelineItem,
                sentTimeFull = dateFormatter.format(
                    timelineItem.sentTimeMillis,
                    DateFormatterMode.Full,
                    useRelative = true,
                ),
                displayEmojiReactions = displayEmojiReactions,
                verifiedUserSendFailure = verifiedUserSendFailure,
                actions = actions.toImmutableList(),
                // Merge suggested and recent emojis, removing duplicates and returning at most 100
                recentEmojis = (suggestedEmojis + recentEmojis).distinct()
                    .take(100)
                    .toImmutableList()
            )
        } else {
            target.value = ActionListState.Target.None
        }
    }

    private fun buildActions(
        timelineItem: TimelineItem.Event,
        usersEventPermissions: UserEventPermissions,
        isDeveloperModeEnabled: Boolean,
        isEventPinned: Boolean,
        isThreadsEnabled: Boolean,
    ): List<TimelineItemAction> {
        val canRedact = timelineItem.isMine && usersEventPermissions.canRedactOwn || !timelineItem.isMine && usersEventPermissions.canRedactOther
        return buildSet {
            if (timelineItem.canBeRepliedTo && usersEventPermissions.canSendMessage) {
                if (isThreadsEnabled && timelineMode !is Timeline.Mode.Thread && timelineItem.isRemote) {
                    // If threads are enabled, we can reply in thread if the item is remote
                    add(TimelineItemAction.ReplyInThread)
                    add(TimelineItemAction.Reply)
                } else {
                    if (!isThreadsEnabled && timelineItem.threadInfo is TimelineItemThreadInfo.ThreadResponse) {
                        // If threads are not enabled, we can reply in a thread if the item is already in the thread
                        add(TimelineItemAction.ReplyInThread)
                    } else {
                        // Otherwise, we can only reply in the room
                        add(TimelineItemAction.Reply)
                    }
                }
            }
            if (timelineItem.isRemote && timelineItem.content.canBeForwarded()) {
                add(TimelineItemAction.Forward)
            }
            if (timelineItem.isEditable && usersEventPermissions.canSendMessage) {
                if (timelineItem.content is TimelineItemEventContentWithAttachment) {
                    // Caption
                    if (timelineItem.content.caption == null) {
                        add(TimelineItemAction.AddCaption)
                    } else {
                        add(TimelineItemAction.EditCaption)
                        add(TimelineItemAction.RemoveCaption)
                    }
                } else if (timelineItem.content is TimelineItemPollContent) {
                    add(TimelineItemAction.EditPoll)
                } else {
                    add(TimelineItemAction.Edit)
                }
            }
            if (canRedact && timelineItem.content is TimelineItemPollContent && !timelineItem.content.isEnded) {
                add(TimelineItemAction.EndPoll)
            }
            val canPinUnpin = usersEventPermissions.canPinUnpin && timelineItem.isRemote
            if (canPinUnpin) {
                if (isEventPinned) {
                    add(TimelineItemAction.Unpin)
                } else {
                    add(TimelineItemAction.Pin)
                }
            }
            if (timelineItem.content.canBeCopied()) {
                add(TimelineItemAction.CopyText)
            } else if ((timelineItem.content as? TimelineItemEventContentWithAttachment)?.caption.isNullOrBlank().not()) {
                add(TimelineItemAction.CopyCaption)
            }
            if (timelineItem.isRemote) {
                add(TimelineItemAction.CopyLink)
            }
            if (isDeveloperModeEnabled) {
                add(TimelineItemAction.ViewSource)
            }
            if (!timelineItem.isMine) {
                add(TimelineItemAction.ReportContent)
            }
            if (canRedact) {
                add(TimelineItemAction.Redact)
            }
        }
            .postFilter(timelineItem.content)
            .sortedWith(comparator)
            .let(postProcessor::process)
    }
}

/**
 * Post filter the actions based on the content of the event.
 */
private fun Iterable<TimelineItemAction>.postFilter(content: TimelineItemEventContent): Iterable<TimelineItemAction> {
    return filter { action ->
        when (content) {
            is TimelineItemRtcNotificationContent,
            is TimelineItemLegacyCallInviteContent,
            is TimelineItemStateContent -> action == TimelineItemAction.ViewSource
            is TimelineItemRedactedContent -> {
                action == TimelineItemAction.ViewSource || action == TimelineItemAction.Unpin
            }
            else -> true
        }
    }
}
