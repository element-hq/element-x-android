/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent

/**
 * Layout decisions for the content of a message bubble, derived from the event content.
 */
internal data class BubbleLayoutSpec(
    val timestampPosition: TimestampPosition,
    val contentPadding: ContentPadding,
    val canShrinkContent: Boolean,
)

/**
 * Computes the [BubbleLayoutSpec] for the given event content.
 *
 * Note: for [TimelineItemLocationContent] the caller must resolve the live location state first
 * (see `ensureActiveLiveLocation`), since it can change over time.
 */
internal fun bubbleLayoutSpec(
    content: TimelineItemEventContent,
    hasContentValidationError: Boolean,
): BubbleLayoutSpec {
    // Gallery and attachments events render their own invalid content UI per item, so they keep their regular layout
    val needsInvalidContentLayout = content !is TimelineItemGalleryContent &&
        content !is TimelineItemAttachmentsContent &&
        hasContentValidationError
    if (needsInvalidContentLayout) {
        // The invalid content view will be displayed in all these cases, independent of the event content
        return BubbleLayoutSpec(
            timestampPosition = TimestampPosition.Aligned,
            contentPadding = ContentPadding.InvalidContent,
            canShrinkContent = content is TimelineItemVoiceContent,
        )
    }
    val timestampPosition = when (content) {
        is TimelineItemImageContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
        is TimelineItemVideoContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Overlay
        is TimelineItemGalleryContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Below
        is TimelineItemAttachmentsContent -> if (content.showCaption) TimestampPosition.Aligned else TimestampPosition.Below
        is TimelineItemStickerContent -> TimestampPosition.Overlay
        is TimelineItemLocationContent -> {
            val shouldHide = content.mode is TimelineItemLocationContent.Mode.Live &&
                content.mode.isActive &&
                content.mode.isOwnUser
            if (shouldHide) TimestampPosition.Hidden else TimestampPosition.Overlay
        }
        is TimelineItemPollContent -> TimestampPosition.Below
        else -> TimestampPosition.Default
    }
    val contentPadding = when (content) {
        is TimelineItemImageContent -> if (content.showCaption) ContentPadding.CaptionedMedia else ContentPadding.Media
        is TimelineItemVideoContent -> if (content.showCaption) ContentPadding.CaptionedMedia else ContentPadding.Media
        is TimelineItemGalleryContent -> ContentPadding.CaptionedMedia
        is TimelineItemAttachmentsContent -> ContentPadding.CaptionedMedia
        is TimelineItemStickerContent,
        is TimelineItemLocationContent -> ContentPadding.Media
        else -> ContentPadding.Textual
    }
    return BubbleLayoutSpec(
        timestampPosition = timestampPosition,
        contentPadding = contentPadding,
        canShrinkContent = content is TimelineItemVoiceContent,
    )
}
