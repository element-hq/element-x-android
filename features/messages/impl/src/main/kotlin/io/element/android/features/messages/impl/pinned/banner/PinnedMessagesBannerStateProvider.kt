/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
            aLoadingPinnedMessagesBannerState(knownPinnedMessagesCount = 5),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 1, currentPinnedMessageIndex = 0),
            aLoadedPinnedMessagesBannerState(
                knownPinnedMessagesCount = 2,
                currentPinnedMessageIndex = 0,
                message = "This is a pinned long message to check the wrapping behavior",
            ),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 3, currentPinnedMessageIndex = 0),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 5, currentPinnedMessageIndex = 0),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 5, currentPinnedMessageIndex = 1),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 5, currentPinnedMessageIndex = 2),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 5, currentPinnedMessageIndex = 3),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 5, currentPinnedMessageIndex = 4),
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
