/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
