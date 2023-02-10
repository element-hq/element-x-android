/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.messages.actionlist

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.actionlist.model.TimelineItemAction
import io.element.android.features.messages.timeline.model.TimelineItem
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class ActionListState(
    val target: Target,
    val eventSink: (ActionListEvents) -> Unit,
) {

    sealed interface Target {
        object None : Target
        data class Loading(val messageEvent: TimelineItem.MessageEvent) : Target
        data class Success(
            val messageEvent: TimelineItem.MessageEvent,
            val actions: ImmutableList<TimelineItemAction>,
        ) : Target
    }
}

fun anActionListState() = ActionListState(
    target = ActionListState.Target.None,
    eventSink = {}
)
