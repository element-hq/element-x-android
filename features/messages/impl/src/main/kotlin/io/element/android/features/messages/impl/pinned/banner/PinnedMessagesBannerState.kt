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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import io.element.android.libraries.designsystem.text.toAnnotatedString
import io.element.android.libraries.ui.strings.CommonStrings

@Immutable
sealed interface PinnedMessagesBannerState {
    data object Hidden : PinnedMessagesBannerState
    data class Loading(val realPinnedMessagesCount: Int) : PinnedMessagesBannerState
    data class Loaded(
        val currentPinnedMessage: PinnedMessagesBannerItem,
        val currentPinnedMessageIndex: Int,
        val knownPinnedMessagesCount: Int,
        val eventSink: (PinnedMessagesBannerEvents) -> Unit
    ) : PinnedMessagesBannerState

    fun pinnedMessagesCount() = when (this) {
        is Hidden -> 0
        is Loading -> realPinnedMessagesCount
        is Loaded -> knownPinnedMessagesCount
    }

    fun currentPinnedMessageIndex() = when (this) {
        is Hidden -> 0
        is Loading -> 0
        is Loaded -> currentPinnedMessageIndex
    }

    @Composable
    fun formattedMessage() = when (this) {
        is Hidden -> AnnotatedString("")
        is Loading -> stringResource(id = CommonStrings.screen_room_pinned_banner_loading_description).toAnnotatedString()
        is Loaded -> currentPinnedMessage.formatted
    }
}
