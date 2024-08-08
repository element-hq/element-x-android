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

package io.element.android.features.messages.impl.pinned.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface PinnedMessagesListState {
    data object Failed : PinnedMessagesListState
    data object Loading : PinnedMessagesListState
    data object Empty : PinnedMessagesListState
    data class Filled(
        val timelineRoomInfo: TimelineRoomInfo,
        val timelineItems: ImmutableList<TimelineItem>,
        val eventSink: (PinnedMessagesListEvents) -> Unit,
    ) : PinnedMessagesListState {
        val loadedPinnedMessagesCount = timelineItems.count { timelineItem -> timelineItem is TimelineItem.Event }
    }

    @Composable
    fun title(): String {
        return when (this) {
            is Filled -> {
                pluralStringResource(id = CommonPlurals.screen_pinned_timeline_screen_title, loadedPinnedMessagesCount, loadedPinnedMessagesCount)
            }
            else -> stringResource(id = CommonStrings.screen_pinned_timeline_screen_title_empty)
        }
    }
}
