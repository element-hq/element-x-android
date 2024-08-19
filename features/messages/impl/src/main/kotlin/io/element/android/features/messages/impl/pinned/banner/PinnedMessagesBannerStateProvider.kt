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
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.EventId
import kotlin.random.Random

internal class PinnedMessagesBannerStateProvider : PreviewParameterProvider<PinnedMessagesBannerState> {
    override val values: Sequence<PinnedMessagesBannerState>
        get() = sequenceOf(
            aHiddenPinnedMessagesBannerState(),
            aLoadingPinnedMessagesBannerState(knownPinnedMessagesCount = 1),
            aLoadingPinnedMessagesBannerState(knownPinnedMessagesCount = 4),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 1, currentPinnedMessageIndex = 0),
            aLoadedPinnedMessagesBannerState(
                knownPinnedMessagesCount = 2,
                currentPinnedMessageIndex = 0,
                message = "This is a pinned long message to check the wrapping behavior",
            ),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 3, currentPinnedMessageIndex = 0),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 4, currentPinnedMessageIndex = 0),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 4, currentPinnedMessageIndex = 1),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 4, currentPinnedMessageIndex = 2),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 4, currentPinnedMessageIndex = 3),
        )
}

internal fun aHiddenPinnedMessagesBannerState() = PinnedMessagesBannerState.Hidden

internal fun aLoadingPinnedMessagesBannerState(
    knownPinnedMessagesCount: Int = 4
) = PinnedMessagesBannerState.Loading(
    expectedPinnedMessagesCount = knownPinnedMessagesCount
)

internal fun aLoadedPinnedMessagesBannerState(
    currentPinnedMessageIndex: Int = 0,
    knownPinnedMessagesCount: Int = 1,
    message: String = "This is a pinned message",
    currentPinnedMessage: PinnedMessagesBannerItem = PinnedMessagesBannerItem(
        eventId = EventId("\$" + Random.nextInt().toString()),
        formatted = AnnotatedString(message)
    ),
    eventSink: (PinnedMessagesBannerEvents) -> Unit = {}
) = PinnedMessagesBannerState.Loaded(
    currentPinnedMessage = currentPinnedMessage,
    currentPinnedMessageIndex = currentPinnedMessageIndex,
    loadedPinnedMessagesCount = knownPinnedMessagesCount,
    eventSink = eventSink
)
