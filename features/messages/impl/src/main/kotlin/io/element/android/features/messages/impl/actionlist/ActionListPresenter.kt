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

package io.element.android.features.messages.impl.actionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
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
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActionListPresenter @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
    private val featureFlagsService: FeatureFlagService,
) : Presenter<ActionListState> {
    @Composable
    override fun present(): ActionListState {
        val localCoroutineScope = rememberCoroutineScope()

        val target: MutableState<ActionListState.Target> = remember {
            mutableStateOf(ActionListState.Target.None)
        }

        val isDeveloperModeEnabled by appPreferencesStore.isDeveloperModeEnabledFlow().collectAsState(initial = false)
        val isPinnedEventsEnabled by featureFlagsService.isFeatureEnabledFlow(FeatureFlags.PinnedEvents).collectAsState(initial = false)

        fun handleEvents(event: ActionListEvents) {
            when (event) {
                ActionListEvents.Clear -> target.value = ActionListState.Target.None
                is ActionListEvents.ComputeForMessage -> localCoroutineScope.computeForMessage(
                    timelineItem = event.event,
                    usersEventPermissions = event.userEventPermissions,
                    isDeveloperModeEnabled = isDeveloperModeEnabled,
                    isPinnedEventsEnabled = isPinnedEventsEnabled,
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
        target: MutableState<ActionListState.Target>
    ) = launch {
        target.value = ActionListState.Target.Loading(timelineItem)

        val actions = buildActions(
            timelineItem = timelineItem,
            usersEventPermissions = usersEventPermissions,
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            isPinnedEventsEnabled = isPinnedEventsEnabled,
        )
        val displayEmojiReactions = usersEventPermissions.canSendReaction &&
            timelineItem.isRemote &&
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
}

private fun buildActions(
    timelineItem: TimelineItem.Event,
    usersEventPermissions: UserEventPermissions,
    isDeveloperModeEnabled: Boolean,
    isPinnedEventsEnabled: Boolean,
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
        // TODO: handle unpin
        val canPin = isPinnedEventsEnabled && usersEventPermissions.canPin && timelineItem.isRemote
        if (canPin) {
            add(TimelineItemAction.Pin)
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
    }.postFilter(timelineItem.content)
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
