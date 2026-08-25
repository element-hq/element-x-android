/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.event.ElementTimelineItemPreview
import io.element.android.features.messages.impl.timeline.components.event.aGalleryItem
import io.element.android.features.messages.impl.timeline.components.event.aTimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.aFakeTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.anAttachmentItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationValue
import io.element.android.libraries.matrix.ui.media.contentvalidation.DefaultContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.InMemoryEventContentValidationCache
import io.element.android.libraries.matrix.ui.media.contentvalidation.LocalEventContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.NoopContentValidationState
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails

private val AN_EVENT_ID = EventId($$"$eventId")

@PreviewsDayNight
@Composable
internal fun TimelineItemImageViewScanningContentPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Loading)))
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemImageContent()),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemStickerViewScanningContentPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Loading)))
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemStickerContent(aspectRatio = 1.5f)),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVideoViewScanningContentPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Loading)))
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemVideoContent()),
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemFileViewScanningContentPreview() {
    ElementTimelineItemPreview {
        val cache = remember {
            InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Loading)))
        }
        CompositionLocalProvider(
            LocalEventContentValidationState provides cache
        ) {
            ATimelineItemEventRow(event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemFileContent()))
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemAudioViewScanningContentPreview() {
    ElementTimelineItemPreview {
        val cache = remember {
            InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Loading)))
        }
        CompositionLocalProvider(
            LocalEventContentValidationState provides cache
        ) {
            ATimelineItemEventRow(event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemAudioContent()))
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVoiceViewScanningContentPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Loading)))
    }
    ElementTimelineItemPreview {
        CompositionLocalProvider(
            LocalEventContentValidationState provides cache,
            LocalTimelineItemPresenterFactories provides aFakeTimelineItemPresenterFactories(),
        ) {
            ATimelineItemEventRow(
                event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemVoiceContent()),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemScanningContentFailedPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Invalid)))
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ATimelineItemEventRow(event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemImageContent()))
            ATimelineItemEventRow(event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemImageContent(caption = "A caption")))
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemScanningContentNotFoundPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(initial = mapOf(
            AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.UnrecoverableError(IllegalStateException("Not found")))
        ))
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ATimelineItemEventRow(event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemImageContent()))
            ATimelineItemEventRow(event = aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemImageContent(caption = "A caption")))
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemScanningContentWithInvalidRepliesPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.Invalid)))
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    content = aTimelineItemTextContent(),
                    inReplyTo = inReplyToInvalidContent(),
                )
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    eventId = AN_EVENT_ID,
                    content = aTimelineItemImageContent(),
                    inReplyTo = inReplyToInvalidContent(),
                )
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    eventId = AN_EVENT_ID,
                    content = aTimelineItemImageContent(),
                    inReplyTo = inReplyToTextContent(),
                )
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemScanningContentWithRepliesFailedPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(
            initial = mapOf(AN_EVENT_ID to NoopContentValidationState(ContentValidationValue.UnrecoverableError(IllegalStateException("BOOM"))))
        )
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    content = aTimelineItemTextContent(),
                    inReplyTo = inReplyToInvalidContent(),
                )
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    eventId = AN_EVENT_ID,
                    content = aTimelineItemImageContent(),
                    inReplyTo = inReplyToInvalidContent(),
                )
            )
            ATimelineItemEventRow(
                event = aTimelineItemEvent(
                    eventId = AN_EVENT_ID,
                    content = aTimelineItemImageContent(),
                    inReplyTo = inReplyToTextContent(),
                )
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGalleryViewScanningContentFailedPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(
            initial =
                mapOf(
                    AN_EVENT_ID to DefaultContentValidationState(mapOf(
                        "invalid" to ContentValidationValue.Invalid,
                        "error" to ContentValidationValue.UnrecoverableError(IllegalStateException("BOOM")),
                        "" to ContentValidationValue.Valid
                    ))
                )
        )
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                eventId = AN_EVENT_ID,
                content = aTimelineItemGalleryContent(
                    items = listOf(
                        aGalleryItem(),
                        aGalleryItem(mediaSource = MediaSource("invalid", "{}")),
                        aGalleryItem(),
                        aGalleryItem(mediaSource = MediaSource("invalid", "{}")),
                        aGalleryItem(mediaSource = MediaSource("error", "{}")),
                        aGalleryItem(mediaSource = MediaSource("invalid", "{}")),
                        aGalleryItem(),
                    )
                )
            )
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemAttachmentsViewScanningContentFailedPreview() = ElementPreview {
    val cache = remember {
        InMemoryEventContentValidationCache(
            initial =
            mapOf(
                AN_EVENT_ID to DefaultContentValidationState(mapOf(
                    "invalid" to ContentValidationValue.Invalid,
                    "error" to ContentValidationValue.UnrecoverableError(IllegalStateException("BOOM")),
                    "" to ContentValidationValue.Valid
                ))
            )
        )
    }
    CompositionLocalProvider(LocalEventContentValidationState provides cache) {
        ATimelineItemEventRow(
            event = aTimelineItemEvent(
                eventId = AN_EVENT_ID,
                content = aTimelineItemAttachmentsContent(
                    attachments = listOf(
                        anAttachmentItem(),
                        anAttachmentItem(mediaSource = MediaSource("invalid", "{}")),
                        anAttachmentItem(),
                        anAttachmentItem(),
                        anAttachmentItem(mediaSource = MediaSource("error", "{}")),
                        anAttachmentItem(mediaSource = MediaSource("invalid", "{}")),
                    )
                )
            )
        )
    }
}

private fun inReplyToInvalidContent(): InReplyToDetails.Ready = InReplyToDetails.Ready(
    eventId = AN_EVENT_ID,
    senderId = UserId("@sender:matrix.org"),
    eventContent = MessageContent(
        body = "A body",
        inReplyTo = null,
        isEdited = false,
        threadInfo = null,
        type = ImageMessageType(
            filename = "A file",
            caption = "A caption",
            formattedCaption = null,
            source = MediaSource("", ""),
            info = null,
        )
    ),
    textContent = "A text content",
    senderProfile = ProfileDetails.Ready(
        displayName = "Sender",
        displayNameAmbiguous = false,
        avatarUrl = null,
        displayedStatus = null
    )
)

private fun inReplyToTextContent(): InReplyToDetails.Ready = InReplyToDetails.Ready(
    eventId = EventId($$"$text_eventId"),
    senderId = UserId("@sender:matrix.org"),
    eventContent = MessageContent(
        body = "A body",
        inReplyTo = null,
        isEdited = false,
        threadInfo = null,
        type = TextMessageType(
            body = "A body",
            formatted = null,
        )
    ),
    textContent = "A text content",
    senderProfile = ProfileDetails.Ready(
        displayName = "Sender",
        displayNameAmbiguous = false,
        avatarUrl = null,
        displayedStatus = null
    )
)
