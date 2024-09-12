/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.messages.api.pinned.IsPinnedMessagesFeatureEnabled
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.canBeCopied
import io.element.android.features.messages.impl.timeline.model.event.canBeForwarded
import io.element.android.features.messages.impl.timeline.model.event.canReact
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface ActionListPresenter : Presenter<ActionListState> {
    interface Factory {
        fun create(postProcessor: TimelineItemActionPostProcessor): ActionListPresenter
    }
}

class DefaultActionListPresenter @AssistedInject constructor(
    @Assisted
    private val postProcessor: TimelineItemActionPostProcessor,
    private val appPreferencesStore: AppPreferencesStore,
    private val isPinnedMessagesFeatureEnabled: IsPinnedMessagesFeatureEnabled,
    private val room: MatrixRoom,
) : ActionListPresenter {
    @AssistedFactory
    @ContributesBinding(RoomScope::class)
    interface Factory : ActionListPresenter.Factory {
        override fun create(postProcessor: TimelineItemActionPostProcessor): DefaultActionListPresenter
    }

    @Composable
    override fun present(): ActionListState {
        val localCoroutineScope = rememberCoroutineScope()

        val target: MutableState<ActionListState.Target> = remember {
            mutableStateOf(ActionListState.Target.None)
        }

        val isDeveloperModeEnabled by appPreferencesStore.isDeveloperModeEnabledFlow().collectAsState(initial = false)
        val isPinnedEventsEnabled = isPinnedMessagesFeatureEnabled()
        val pinnedEventIds by remember {
            room.roomInfoFlow.map { it.pinnedEventIds }
        }.collectAsState(initial = persistentListOf())

        fun handleEvents(event: ActionListEvents) {
            when (event) {
                ActionListEvents.Clear -> target.value = ActionListState.Target.None
                is ActionListEvents.ComputeForMessage -> localCoroutineScope.computeForMessage(
                    timelineItem = event.event,
                    usersEventPermissions = event.userEventPermissions,
                    isDeveloperModeEnabled = isDeveloperModeEnabled,
                    isPinnedEventsEnabled = isPinnedEventsEnabled,
                    pinnedEventIds = pinnedEventIds,
                    target = target,
                )
            }
        }

        return ActionListState(
            target = target.value,
            eventSink = { handleEvents(it) }
        )
    }

    private fun CoroutineScope.computeForMessage(
        timelineItem: TimelineItem.Event,
        usersEventPermissions: UserEventPermissions,
        isDeveloperModeEnabled: Boolean,
        isPinnedEventsEnabled: Boolean,
        pinnedEventIds: ImmutableList<EventId>,
        target: MutableState<ActionListState.Target>
    ) = launch {
        target.value = ActionListState.Target.Loading(timelineItem)

        val actions = buildActions(
            timelineItem = timelineItem,
            usersEventPermissions = usersEventPermissions,
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            isPinnedEventsEnabled = isPinnedEventsEnabled,
            isEventPinned = pinnedEventIds.contains(timelineItem.eventId),
        )

        val displayEmojiReactions = usersEventPermissions.canSendReaction &&
            timelineItem.content.canReact()
        if (actions.isNotEmpty() || displayEmojiReactions) {
            target.value = ActionListState.Target.Success(
                event = timelineItem,
                displayEmojiReactions = displayEmojiReactions,
                actions = actions.toImmutableList()
            )
        } else {
            target.value = ActionListState.Target.None
        }
    }

    private fun buildActions(
        timelineItem: TimelineItem.Event,
        usersEventPermissions: UserEventPermissions,
        isDeveloperModeEnabled: Boolean,
        isPinnedEventsEnabled: Boolean,
        isEventPinned: Boolean,
    ): List<TimelineItemAction> {
        val canRedact = timelineItem.isMine && usersEventPermissions.canRedactOwn || !timelineItem.isMine && usersEventPermissions.canRedactOther
        return buildList {
            if (timelineItem.canBeRepliedTo && usersEventPermissions.canSendMessage) {
                if (timelineItem.isThreaded) {
                    add(TimelineItemAction.ReplyInThread)
                } else {
                    add(TimelineItemAction.Reply)
                }
            }
            if (timelineItem.isRemote && timelineItem.content.canBeForwarded()) {
                add(TimelineItemAction.Forward)
            }
            if (timelineItem.isEditable) {
                add(TimelineItemAction.Edit)
            }
            if (canRedact && timelineItem.content is TimelineItemPollContent && !timelineItem.content.isEnded) {
                add(TimelineItemAction.EndPoll)
            }
            val canPinUnpin = isPinnedEventsEnabled && usersEventPermissions.canPinUnpin && timelineItem.isRemote
            if (canPinUnpin) {
                if (isEventPinned) {
                    add(TimelineItemAction.Unpin)
                } else {
                    add(TimelineItemAction.Pin)
                }
            }
            if (timelineItem.content.canBeCopied()) {
                add(TimelineItemAction.Copy)
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
            .let(postProcessor::process)
    }
}

/**
 * Post filter the actions based on the content of the event.
 */
private fun List<TimelineItemAction>.postFilter(content: TimelineItemEventContent): List<TimelineItemAction> {
    return filter { action ->
        when (content) {
            is TimelineItemCallNotifyContent,
            is TimelineItemLegacyCallInviteContent,
            is TimelineItemStateContent,
            is TimelineItemRedactedContent -> {
                action == TimelineItemAction.ViewSource
            }
            else -> true
        }
    }
}
