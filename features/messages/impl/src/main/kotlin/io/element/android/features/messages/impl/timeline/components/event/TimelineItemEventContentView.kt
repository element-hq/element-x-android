/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannedString
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.rememberPresenter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAttachmentsContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.captionOrNull
import io.element.android.features.messages.impl.timeline.model.event.ensureActiveLiveLocation
import io.element.android.features.messages.impl.timeline.model.event.formattedCaptionOrNull
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionEvent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.components.EqualWidthColumn
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.ui.media.contentvalidation.ContentValidationState
import io.element.android.libraries.matrix.ui.media.contentvalidation.InvalidContentView
import io.element.android.libraries.matrix.ui.media.contentvalidation.NotFoundContentView
import io.element.android.libraries.matrix.ui.media.contentvalidation.collectOverallState
import io.element.android.libraries.matrix.ui.media.contentvalidation.rememberEventContentValidationState
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.wysiwyg.compose.EditorStyledText
import io.element.android.wysiwyg.link.Link

@Composable
fun TimelineItemEventContentView(
    eventId: EventId?,
    content: TimelineItemEventContent,
    onContentClick: (() -> Unit)?,
    onGalleryItemClick: ((Int) -> Unit),
    timelineProtectionState: TimelineProtectionState,
    onLongClick: (() -> Unit)?,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    val hideMediaContent = remember(eventId, timelineProtectionState.protectionState) {
        timelineProtectionState.hideMediaContent(eventId)
    }

    val onShowContentClick = remember(timelineProtectionState.eventSink) {
        {
            timelineProtectionState.eventSink(TimelineProtectionEvent.ShowContent(eventId))
        }
    }

    val contentValidationState = rememberEventContentValidationState(eventId = eventId, needsValidation = content.isMedia)
    val overallValidationState by contentValidationState.collectOverallState()
    val needsContentValidationPerItem = remember(content) { content is TimelineItemGalleryContent || content is TimelineItemAttachmentsContent }

    if (eventId != null) {
        ValidateMediaHelper(eventId, content, contentValidationState, eventSink)
    }

    val caption = content.captionOrNull()
    val showCaption = caption != null && content !is TimelineItemStickerContent && content !is TimelineItemVoiceContent
    // If a caption is added, it will be used to get the free space for the overlay, so we don't need to add onContentLayoutChange to the actual content
    val calculatedOnContentLayoutChange = remember(onContentLayoutChange, showCaption) {
        if (showCaption) {
            {}
        } else {
            onContentLayoutChange
        }
    }

    val displayInvalidContent = overallValidationState.isInvalid() && !needsContentValidationPerItem
    val displayContentNotFound = overallValidationState.hasUnrecoverableError() && !needsContentValidationPerItem
    EqualWidthColumn(modifier = modifier) {
        if (displayInvalidContent) {
            InvalidContentView(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                onTextLayout = ContentAvoidingLayout.measureLastTextLine(
                    onContentLayoutChange = calculatedOnContentLayoutChange,
                    // Icon + horizontal paddings
                    extraWidth = 24.dp + 20.dp,
                )
            )
        } else if (displayContentNotFound) {
            NotFoundContentView(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                onTextLayout = ContentAvoidingLayout.measureLastTextLine(
                    onContentLayoutChange = calculatedOnContentLayoutChange,
                    // Icon + horizontal paddings
                    extraWidth = 24.dp + 20.dp,
                )
            )
        } else {
            val presenterFactories = LocalTimelineItemPresenterFactories.current
            when (content) {
                is TimelineItemEncryptedContent -> TimelineItemEncryptedView(
                    content = content,
                    onContentLayoutChange = calculatedOnContentLayoutChange,
                )
                is TimelineItemRedactedContent -> TimelineItemRedactedView(
                    content = content,
                    onContentLayoutChange = calculatedOnContentLayoutChange,
                )
                is TimelineItemTextBasedContent -> TimelineItemTextView(
                    content = content,
                    onLinkClick = onLinkClick,
                    onLinkLongClick = onLinkLongClick,
                    onContentLayoutChange = calculatedOnContentLayoutChange,
                )
                is TimelineItemUnknownContent -> TimelineItemUnknownView(
                    content = content,
                    onContentLayoutChange = calculatedOnContentLayoutChange,
                )
                is TimelineItemLocationContent -> {
                    TimelineItemLocationView(
                        content = content.ensureActiveLiveLocation(),
                        onStopLiveLocationClick = { eventSink(TimelineEvent.StopLiveLocationShare) },
                    )
                }
                is TimelineItemImageContent -> {
                    TimelineItemImageView(
                        content = content,
                        hideMediaContent = hideMediaContent,
                        onContentClick = onContentClick,
                        onLongClick = onLongClick,
                        onShowContentClick = onShowContentClick,
                        contentValidationState = contentValidationState,
                    )
                }
                is TimelineItemStickerContent -> {
                    TimelineItemStickerView(
                        content = content,
                        hideMediaContent = hideMediaContent,
                        onContentClick = onContentClick,
                        onLongClick = onLongClick,
                        onShowClick = onShowContentClick,
                        contentValidationState = contentValidationState,
                    )
                }
                is TimelineItemVideoContent -> {
                    TimelineItemVideoView(
                        content = content,
                        hideMediaContent = hideMediaContent,
                        onContentClick = onContentClick,
                        onLongClick = onLongClick,
                        onShowContentClick = onShowContentClick,
                        contentValidationState = contentValidationState,
                    )
                }
                is TimelineItemGalleryContent -> TimelineItemGalleryView(
                    eventId = eventId,
                    content = content,
                    onGalleryItemClick = { index -> onGalleryItemClick(index) },
                    onLongClick = onLongClick,
                )
                is TimelineItemAttachmentsContent -> TimelineItemAttachmentsListView(
                    eventId = eventId,
                    content = content,
                    onGalleryItemClick = { index -> onGalleryItemClick(index) },
                )
                is TimelineItemFileContent -> {
                    TimelineItemFileView(
                        content = content,
                        onContentLayoutChange = calculatedOnContentLayoutChange,
                        contentValidationValue = overallValidationState,
                    )
                }
                is TimelineItemAudioContent -> {
                    TimelineItemAudioView(
                        content = content,
                        onContentLayoutChange = calculatedOnContentLayoutChange,
                        contentValidationValue = overallValidationState,
                    )
                }
                is TimelineItemLegacyCallInviteContent -> TimelineItemLegacyCallInviteView()
                is TimelineItemStateContent -> TimelineItemStateView(
                    content = content,
                )
                is TimelineItemPollContent -> TimelineItemPollView(
                    content = content,
                    eventSink = eventSink,
                )
                is TimelineItemVoiceContent -> {
                    val presenter: Presenter<VoiceMessageState> = presenterFactories.rememberPresenter(content)
                    TimelineItemVoiceView(
                        state = presenter.present(),
                        content = content,
                        onContentLayoutChange = calculatedOnContentLayoutChange,
                        contentValidationValue = overallValidationState,
                    )
                }
                is TimelineItemRtcNotificationContent -> error("This shouldn't be rendered as the content of a bubble")
            }
        }

        if (showCaption) {
            val padding = if (displayInvalidContent || displayContentNotFound) {
                PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 0.dp)
            } else {
                when (content) {
                    is TimelineItemImageContent,
                    is TimelineItemVideoContent,
                    is TimelineItemGalleryContent,
                    is TimelineItemAttachmentsContent -> PaddingValues(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 0.dp)
                    else -> PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                }
            }
            CaptionView(
                modifier = Modifier.padding(padding),
                caption = caption,
                formattedCaption = content.formattedCaptionOrNull(),
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
                onContentLayoutChange = onContentLayoutChange,
            )
        }
    }
}

@Composable
private fun CaptionView(
    caption: String,
    formattedCaption: CharSequence?,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val caption = if (LocalInspectionMode.current) {
        SpannedString(caption)
    } else {
        formattedCaption ?: SpannedString(caption)
    }
    CompositionLocalProvider(
        LocalContentColor provides ElementTheme.colors.textPrimary,
        LocalTextStyle provides ElementTheme.typography.fontBodyLgRegular
    ) {
        EditorStyledText(
            modifier = modifier,
            text = caption,
            style = ElementRichTextEditorStyle.textStyle(),
            onLinkClickedListener = onLinkClick,
            onLinkLongClickedListener = onLinkLongClick,
            releaseOnDetach = false,
            onTextLayout = ContentAvoidingLayout.measureLegacyLastTextLine(onContentLayoutChange = onContentLayoutChange),
        )
    }
}

@Composable
private fun ValidateMediaHelper(
    eventId: EventId,
    content: TimelineItemEventContent,
    contentValidationState: ContentValidationState,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
) {
    val mediaSources = when (content) {
        is TimelineItemImageContent -> listOfNotNull(content.thumbnailSource, content.mediaSource)
        is TimelineItemStickerContent -> listOfNotNull(content.thumbnailSource, content.mediaSource)
        is TimelineItemVideoContent -> listOfNotNull(content.thumbnailSource, content.mediaSource)
        is TimelineItemFileContent -> listOfNotNull(content.thumbnailSource, content.mediaSource)
        is TimelineItemAudioContent -> listOf(content.mediaSource)
        is TimelineItemVoiceContent -> listOf(content.mediaSource)
        is TimelineItemGalleryContent -> content.items.flatMap { listOfNotNull(it.thumbnailSource, it.mediaSource) }
        is TimelineItemAttachmentsContent -> content.attachments.flatMap { listOfNotNull(it.thumbnailSource, it.mediaSource) }
        else -> return
    }
    val updatedEventSink by rememberUpdatedState(eventSink)
    LaunchedEffect(eventId, mediaSources) {
        updatedEventSink(TimelineEvent.ValidateMedia(eventId, mediaSources, contentValidationState))
    }
}
