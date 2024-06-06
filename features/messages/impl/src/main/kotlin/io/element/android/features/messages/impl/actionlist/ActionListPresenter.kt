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
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.canBeCopied
import io.element.android.features.messages.impl.timeline.model.event.canReact
import io.element.android.features.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActionListPresenter @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
) : Presenter<ActionListState> {
    @Composable
    override fun present(): ActionListState {
        val localCoroutineScope = rememberCoroutineScope()

        val target: MutableState<ActionListState.Target> = remember {
            mutableStateOf(ActionListState.Target.None)
        }

        val isDeveloperModeEnabled by appPreferencesStore.isDeveloperModeEnabledFlow().collectAsState(initial = false)

        fun handleEvents(event: ActionListEvents) {
            when (event) {
                ActionListEvents.Clear -> target.value = ActionListState.Target.None
                is ActionListEvents.ComputeForMessage -> localCoroutineScope.computeForMessage(
                    timelineItem = event.event,
                    userCanRedactOwn = event.canRedactOwn,
                    userCanRedactOther = event.canRedactOther,
                    userCanSendMessage = event.canSendMessage,
                    userCanSendReaction = event.canSendReaction,
                    isDeveloperModeEnabled = isDeveloperModeEnabled,
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
        userCanRedactOwn: Boolean,
        userCanRedactOther: Boolean,
        userCanSendMessage: Boolean,
        userCanSendReaction: Boolean,
        isDeveloperModeEnabled: Boolean,
        target: MutableState<ActionListState.Target>
    ) = launch {
        target.value = ActionListState.Target.Loading(timelineItem)
        val canRedact = timelineItem.isMine && userCanRedactOwn || !timelineItem.isMine && userCanRedactOther
        val actions =
            when (timelineItem.content) {
                is TimelineItemCallNotifyContent -> {
                    if (isDeveloperModeEnabled) {
                        listOf(TimelineItemAction.ViewSource)
                    } else {
                        emptyList()
                    }
                }
                is TimelineItemRedactedContent -> {
                    if (isDeveloperModeEnabled) {
                        listOf(TimelineItemAction.ViewSource)
                    } else {
                        emptyList()
                    }
                }
                is TimelineItemStateContent -> {
                    buildList {
                        add(TimelineItemAction.Copy)
                        add(TimelineItemAction.CopyLink)
                        if (isDeveloperModeEnabled) {
                            add(TimelineItemAction.ViewSource)
                        }
                    }
                }
                is TimelineItemPollContent -> {
                    val canEndPoll = timelineItem.isRemote &&
                        !timelineItem.content.isEnded &&
                        (timelineItem.isMine || canRedact)
                    buildList {
                        if (timelineItem.isRemote) {
                            // Can only reply or forward messages already uploaded to the server
                            add(TimelineItemAction.Reply)
                        }
                        if (timelineItem.isRemote && timelineItem.isEditable) {
                            add(TimelineItemAction.Edit)
                        }
                        if (canEndPoll) {
                            add(TimelineItemAction.EndPoll)
                        }
                        if (timelineItem.content.canBeCopied()) {
                            add(TimelineItemAction.Copy)
                        }
                        add(TimelineItemAction.CopyLink)
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
                }
                is TimelineItemVoiceContent -> {
                    buildList {
                        if (timelineItem.isRemote) {
                            add(TimelineItemAction.Reply)
                            add(TimelineItemAction.Forward)
                        }
                        add(TimelineItemAction.CopyLink)
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
                }
                is TimelineItemLegacyCallInviteContent -> {
                    buildList {
                        if (isDeveloperModeEnabled) {
                            add(TimelineItemAction.ViewSource)
                        }
                    }
                }
                else -> buildList<TimelineItemAction> {
                    if (timelineItem.isRemote) {
                        // Can only reply or forward messages already uploaded to the server
                        if (userCanSendMessage) {
                            if (timelineItem.isThreaded) {
                                add(TimelineItemAction.ReplyInThread)
                            } else {
                                add(TimelineItemAction.Reply)
                            }
                        }
                        // Stickers can't be forwarded (yet) so we don't show the option
                        // See https://github.com/element-hq/element-x-android/issues/2161
                        if (!timelineItem.isSticker) {
                            add(TimelineItemAction.Forward)
                        }
                    }
                    if (timelineItem.isMine && timelineItem.isTextMessage) {
                        add(TimelineItemAction.Edit)
                    }
                    if (timelineItem.content.canBeCopied()) {
                        add(TimelineItemAction.Copy)
                    }
                    add(TimelineItemAction.CopyLink)
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
            }
        val displayEmojiReactions = userCanSendReaction &&
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
