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

import org.matrix.rustcomponents.sdk.TimelineChange
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineItem
import uniffi.matrix_sdk_ui.EventItemOrigin

/**
 * Tries to get an event origin from the TimelineDiff.
 * If there is multiple events in the diff, uses the first one as it should be a good indicator.
 */
internal fun TimelineDiff.eventOrigin(): EventItemOrigin? {
    return when (change()) {
        TimelineChange.APPEND -> {
            append()?.firstOrNull()?.eventOrigin()
        }
        TimelineChange.PUSH_BACK -> {
            pushBack()?.eventOrigin()
        }
        TimelineChange.PUSH_FRONT -> {
            pushFront()?.eventOrigin()
        }
        TimelineChange.SET -> {
            set()?.item?.eventOrigin()
        }
        TimelineChange.INSERT -> {
            insert()?.item?.eventOrigin()
        }
        TimelineChange.RESET -> {
            reset()?.firstOrNull()?.eventOrigin()
        }
        else -> null
    }
}

private fun TimelineItem.eventOrigin(): EventItemOrigin? {
    return asEvent()?.origin()
}
