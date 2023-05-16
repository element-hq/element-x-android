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

package io.element.android.libraries.eventformatter.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import javax.inject.Inject

/**
 * For now use the same formatter than for the room list using [RoomLastMessageFormatter].
 * We will change this if we want to have a different rendering in the timeline.
 */
@ContributesBinding(SessionScope::class)
class DefaultTimelineEventFormatter @Inject constructor(
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
) : TimelineEventFormatter {

    override fun format(event: EventTimelineItem): CharSequence? {
        return roomLastMessageFormatter.format(
            event,
            /* We do not want to distinguish DM and room here */
            isDmRoom = false,
        )
    }
}
