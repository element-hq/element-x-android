/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.ui.text.AnnotatedString
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.eventformatter.api.PinnedMessagesBannerFormatter
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PinnedMessagesBannerItemFactory @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
    private val formatter: PinnedMessagesBannerFormatter,
) {
    suspend fun create(timelineItem: MatrixTimelineItem): PinnedMessagesBannerItem? = withContext(coroutineDispatchers.computation) {
        when (timelineItem) {
            is MatrixTimelineItem.Event -> {
                val eventId = timelineItem.eventId ?: return@withContext null
                val formatted = formatter.format(timelineItem.event)
                PinnedMessagesBannerItem(
                    eventId = eventId,
                    formatted = if (formatted is AnnotatedString) {
                        formatted
                    } else {
                        AnnotatedString(formatted.toString())
                    },
                )
            }
            else -> null
        }
    }
}
