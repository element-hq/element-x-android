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
            aLoadingPinnedMessagesBannerState(knownPinnedMessagesCount = 4),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 1, currentPinnedMessageIndex = 0),
            aLoadedPinnedMessagesBannerState(knownPinnedMessagesCount = 2, currentPinnedMessageIndex = 0),
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
    currentPinnedMessage: PinnedMessagesBannerItem = PinnedMessagesBannerItem(
        eventId = EventId("\$" + Random.nextInt().toString()),
        formatted = AnnotatedString("This is a pinned message")
    ),
    eventSink: (PinnedMessagesBannerEvents) -> Unit = {}
) = PinnedMessagesBannerState.Loaded(
    currentPinnedMessage = currentPinnedMessage,
    currentPinnedMessageIndex = currentPinnedMessageIndex,
    loadedPinnedMessagesCount = knownPinnedMessagesCount,
    eventSink = eventSink
)
