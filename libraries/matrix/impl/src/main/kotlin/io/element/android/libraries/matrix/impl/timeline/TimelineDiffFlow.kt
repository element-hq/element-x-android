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

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.TimelineListener

internal fun Room.timelineDiffFlow(onInitialList: suspend (List<TimelineItem>) -> Unit): Flow<TimelineDiff> =
    mxCallbackFlow {
        val listener = object : TimelineListener {
            override fun onUpdate(diff: TimelineDiff) {
                trySendBlocking(diff)
            }
        }
        val result = addTimelineListener(listener)
        onInitialList(result.items)
        result.itemsStream
    }
