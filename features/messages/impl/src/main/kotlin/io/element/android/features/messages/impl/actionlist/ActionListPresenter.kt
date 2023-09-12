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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.canBeCopied
import io.element.android.features.messages.impl.timeline.model.event.canReact
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActionListPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
) : Presenter<ActionListState> {

    @Composable
    override fun present(): ActionListState {
        val localCoroutineScope = rememberCoroutineScope()

        val target: MutableState<ActionListState.Target> = remember {
            mutableStateOf(ActionListState.Target.None)
        }

        val displayEmojiReactions by remember {
            derivedStateOf {
                val event = (target.value as? ActionListState.Target.Success)?.event
                event?.isRemote == true && event.content.canReact()
            }
        }

        fun handleEvents(event: ActionListEvents) {
            when (event) {
                ActionListEvents.Clear -> target.value = ActionListState.Target.None
                is ActionListEvents.ComputeForMessage -> localCoroutineScope.computeForMessage(
                    timelineItem = event.event,
                    userCanRedact = event.canRedact,
                    userCanSendMessage = event.canSendMessage,
                    target = target,
                )
            }
        }

        return ActionListState(
            target = target.value,
            displayEmojiReactions = displayEmojiReactions,
            eventSink = { handleEvents(it) }
        )
    }

    private fun CoroutineScope.computeForMessage(
        timelineItem: TimelineItem.Event,
        userCanRedact: Boolean,
        userCanSendMessage: Boolean,
        target: MutableState<ActionListState.Target>
    ) = launch {
        target.value = ActionListState.Target.Loading(timelineItem)
        val actions =
            when (timelineItem.content) {
                is TimelineItemRedactedContent -> {
                    if (buildMeta.isDebuggable) {
                        listOf(TimelineItemAction.Developer)
                    } else {
                        emptyList()
                    }
                }
                is TimelineItemStateContent -> {
                    buildList {
                        add(TimelineItemAction.Copy)
                        if (buildMeta.isDebuggable) {
                            add(TimelineItemAction.Developer)
                        }
                    }
                }
                is TimelineItemPollContent -> {
                    buildList {
                        val isMineOrCanRedact = timelineItem.isMine || userCanRedact

                        // TODO Poll: Reply to poll. Ensure to update `fun TimelineItemEventContent.canBeReplied()`
                        //  when touching this
                        // if (timelineItem.isRemote) {
                        //     // Can only reply or forward messages already uploaded to the server
                        //     add(TimelineItemAction.Reply)
                        // }
                        if (!timelineItem.content.isEnded && timelineItem.isRemote && isMineOrCanRedact) {
                            add(TimelineItemAction.EndPoll)
                        }
                        if (timelineItem.content.canBeCopied()) {
                            add(TimelineItemAction.Copy)
                        }
                        if (buildMeta.isDebuggable) {
                            add(TimelineItemAction.Developer)
                        }
                        if (!timelineItem.isMine) {
                            add(TimelineItemAction.ReportContent)
                        }
                        if (isMineOrCanRedact) {
                            add(TimelineItemAction.Redact)
                        }
                    }
                }
                else -> buildList<TimelineItemAction> {
                    if (timelineItem.isRemote) {
                        // Can only reply or forward messages already uploaded to the server
                        if (userCanSendMessage) {
                            add(TimelineItemAction.Reply)
                        }
                        add(TimelineItemAction.Forward)
                    }
                    if (timelineItem.isMine && timelineItem.isTextMessage) {
                        add(TimelineItemAction.Edit)
                    }
                    if (timelineItem.content.canBeCopied()) {
                        add(TimelineItemAction.Copy)
                    }
                    if (buildMeta.isDebuggable) {
                        add(TimelineItemAction.Developer)
                    }
                    if (!timelineItem.isMine) {
                        add(TimelineItemAction.ReportContent)
                    }
                    if (timelineItem.isMine || userCanRedact) {
                        add(TimelineItemAction.Redact)
                    }
                }
            }
        if (actions.isNotEmpty()) {
            target.value = ActionListState.Target.Success(timelineItem, actions.toImmutableList())
        } else {
            target.value = ActionListState.Target.None
        }
    }
}
