/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.components.event.aTimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.model.event.aLiveLocationMode
import io.element.android.features.messages.impl.timeline.model.event.aStaticLocationMode
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import org.junit.Test

class BubbleLayoutSpecTest {
    @Test
    fun `text content uses default timestamp position and textual padding`() {
        val spec = bubbleLayoutSpec(aTimelineItemTextContent(), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Default)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.Textual)
        assertThat(spec.canShrinkContent).isFalse()
    }

    @Test
    fun `image content without caption overlays the timestamp`() {
        val spec = bubbleLayoutSpec(aTimelineItemImageContent(), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Overlay)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.Media)
    }

    @Test
    fun `image content with caption aligns the timestamp`() {
        val spec = bubbleLayoutSpec(aTimelineItemImageContent(caption = "caption"), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Aligned)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.CaptionedMedia)
    }

    @Test
    fun `video content without caption overlays the timestamp`() {
        val spec = bubbleLayoutSpec(aTimelineItemVideoContent(), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Overlay)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.Media)
    }

    @Test
    fun `video content with caption aligns the timestamp`() {
        val spec = bubbleLayoutSpec(aTimelineItemVideoContent(caption = "caption"), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Aligned)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.CaptionedMedia)
    }

    @Test
    fun `gallery content without caption places the timestamp below`() {
        val spec = bubbleLayoutSpec(aTimelineItemGalleryContent(), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Below)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.CaptionedMedia)
    }

    @Test
    fun `gallery content with caption aligns the timestamp`() {
        val spec = bubbleLayoutSpec(aTimelineItemGalleryContent(caption = "caption"), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Aligned)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.CaptionedMedia)
    }

    @Test
    fun `attachments content without caption places the timestamp below`() {
        val spec = bubbleLayoutSpec(aTimelineItemAttachmentsContent(), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Below)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.CaptionedMedia)
    }

    @Test
    fun `sticker content overlays the timestamp`() {
        val spec = bubbleLayoutSpec(aTimelineItemStickerContent(), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Overlay)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.Media)
    }

    @Test
    fun `static location content overlays the timestamp`() {
        val spec = bubbleLayoutSpec(aTimelineItemLocationContent(mode = aStaticLocationMode()), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Overlay)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.Media)
    }

    @Test
    fun `own active live location content hides the timestamp`() {
        val spec = bubbleLayoutSpec(
            aTimelineItemLocationContent(mode = aLiveLocationMode(isActive = true, isOwnUser = true)),
            hasContentValidationError = false,
        )
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Hidden)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.Media)
    }

    @Test
    fun `other user's active live location content overlays the timestamp`() {
        val spec = bubbleLayoutSpec(
            aTimelineItemLocationContent(mode = aLiveLocationMode(isActive = true, isOwnUser = false)),
            hasContentValidationError = false,
        )
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Overlay)
    }

    @Test
    fun `inactive live location content overlays the timestamp`() {
        val spec = bubbleLayoutSpec(
            aTimelineItemLocationContent(mode = aLiveLocationMode(isActive = false)),
            hasContentValidationError = false,
        )
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Overlay)
    }

    @Test
    fun `poll content places the timestamp below`() {
        val spec = bubbleLayoutSpec(aTimelineItemPollContent(), hasContentValidationError = false)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Below)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.Textual)
    }

    @Test
    fun `voice content can shrink`() {
        val spec = bubbleLayoutSpec(aTimelineItemVoiceContent(), hasContentValidationError = false)
        assertThat(spec.canShrinkContent).isTrue()
    }

    @Test
    fun `content validation error forces the invalid content layout`() {
        val spec = bubbleLayoutSpec(aTimelineItemImageContent(), hasContentValidationError = true)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Aligned)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.InvalidContent)
    }

    @Test
    fun `content validation error keeps the regular layout for gallery content`() {
        val spec = bubbleLayoutSpec(aTimelineItemGalleryContent(), hasContentValidationError = true)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Below)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.CaptionedMedia)
    }

    @Test
    fun `content validation error keeps the regular layout for attachments content`() {
        val spec = bubbleLayoutSpec(aTimelineItemAttachmentsContent(), hasContentValidationError = true)
        assertThat(spec.timestampPosition).isEqualTo(TimestampPosition.Below)
        assertThat(spec.contentPadding).isEqualTo(ContentPadding.CaptionedMedia)
    }
}
