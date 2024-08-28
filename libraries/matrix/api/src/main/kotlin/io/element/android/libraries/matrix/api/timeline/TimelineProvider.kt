/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.api.timeline

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * This interface defines a way to get the active timeline.
 * It could be the live timeline, a pinned timeline or a detached timeline.
 * By default, the active timeline is the live timeline.
 */
interface TimelineProvider {
    fun activeTimelineFlow(): StateFlow<Timeline?>
}

suspend fun TimelineProvider.getActiveTimeline(): Timeline = activeTimelineFlow().filterNotNull().first()
