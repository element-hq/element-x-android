/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.element.android.features.messages.impl.timeline.components.aCriticalShield
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.aFakeTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.toImmutableList

@PreviewsDayNight
@Composable
internal fun TimelineViewMessageShieldPreview() = ElementPreview {
    val timelineItems = aTimelineItemList(aTimelineItemTextContent())
    // For consistency, ensure that there is a message in the timeline (the last one) with an error.
    val messageShield = aCriticalShield()
    val items = listOf(
        (timelineItems.first() as TimelineItem.Event).copy(messageShield = messageShield)
    ) + timelineItems.drop(1)
    CompositionLocalProvider(
        LocalTimelineItemPresenterFactories provides aFakeTimelineItemPresenterFactories(),
    ) {
        TimelineView(
            state = aTimelineState(
                timelineItems = items.toImmutableList(),
                messageShield = messageShield,
            ),
            onUserDataClick = {},
            onLinkClick = {},
            onMessageClick = {},
            onMessageLongClick = {},
            onSwipeToReply = {},
            onReactionClick = { _, _ -> },
            onReactionLongClick = { _, _ -> },
            onMoreReactionsClick = {},
            onReadReceiptClick = {},
            onJoinCallClick = {},
            forceJumpToBottomVisibility = true,
        )
    }
}
