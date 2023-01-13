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

package io.element.android.x.features.messages.actionlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.x.architecture.Presenter
import io.element.android.x.features.messages.actionlist.model.TimelineItemAction
import io.element.android.x.features.messages.timeline.model.TimelineItem
import io.element.android.x.features.messages.timeline.model.content.TimelineItemRedactedContent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ActionListPresenter @Inject constructor() : Presenter<ActionListState> {

    @Composable
    override fun present(): ActionListState {
        val localCoroutineScope = rememberCoroutineScope()

        val target: MutableState<ActionListState.Target> = remember {
            mutableStateOf(ActionListState.Target.None)
        }

        fun handleEvents(event: ActionListEvents) {
            when (event) {
                ActionListEvents.Clear -> target.value = ActionListState.Target.None
                is ActionListEvents.ComputeForMessage -> localCoroutineScope.computeForMessage(event.messageEvent, target)
            }
        }

        return ActionListState(
            target = target.value,
            eventSink = ::handleEvents
        )
    }

    fun CoroutineScope.computeForMessage(timelineItem: TimelineItem.MessageEvent, target: MutableState<ActionListState.Target>) = launch {
        target.value = ActionListState.Target.Loading(timelineItem)
        val actions =
            if (timelineItem.content is TimelineItemRedactedContent) {
                emptyList()
            } else {
                mutableListOf(
                    TimelineItemAction.Reply,
                    TimelineItemAction.Forward,
                    TimelineItemAction.Copy,
                ).also {
                    if (timelineItem.isMine) {
                        it.add(TimelineItemAction.Edit)
                        it.add(TimelineItemAction.Redact)
                    }
                }
            }
        target.value = ActionListState.Target.Success(timelineItem, actions.toImmutableList())
    }
}
