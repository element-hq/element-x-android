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

package io.element.android.libraries.matrix.impl.notification

import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.impl.timeline.MatrixTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventMessageMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.EventTimelineItemMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.virtual.VirtualTimelineItemMapper
import org.matrix.rustcomponents.sdk.NotificationItem
import org.matrix.rustcomponents.sdk.use
import javax.inject.Inject

class NotificationMapper @Inject constructor() {
    // TODO Inject and remove duplicate?
    private val timelineItemFactory = MatrixTimelineItemMapper(
        virtualTimelineItemMapper = VirtualTimelineItemMapper(),
        eventTimelineItemMapper = EventTimelineItemMapper(
            contentMapper = TimelineEventContentMapper(
                eventMessageMapper = EventMessageMapper()
            )
        )
    )

    fun map(notificationItem: NotificationItem): NotificationData {
        return notificationItem.use {
            NotificationData(
                item = timelineItemFactory.map(it.item),
                title = it.title,
                subtitle = it.subtitle,
                isNoisy = it.isNoisy,
                avatarUrl = it.avatarUrl,
            )
        }
    }
}
