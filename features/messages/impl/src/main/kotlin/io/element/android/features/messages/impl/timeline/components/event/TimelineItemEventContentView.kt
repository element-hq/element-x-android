/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.di.LocalTimelineItemPresenterFactories
import io.element.android.features.messages.impl.timeline.di.rememberPresenter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
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
import io.element.android.features.messages.impl.timeline.model.event.ensureActiveLiveLocation
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionEvent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.rememberEventContentValidationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.wysiwyg.link.Link

@Composable
fun TimelineItemEventContentView(
    eventId: EventId?,
    content: TimelineItemEventContent,
    onContentClick: (() -> Unit)?,
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

    val contentValidationState = rememberEventContentValidationState(eventId)

    if (eventId != null) {
        ValidateMediaHelper(eventId, content, contentValidationState.state, eventSink)
    }

    val presenterFactories = LocalTimelineItemPresenterFactories.current
    when (content) {
        is TimelineItemEncryptedContent -> TimelineItemEncryptedView(
            content = content,
            onContentLayoutChange = onContentLayoutChange,
            modifier = modifier
        )
        is TimelineItemRedactedContent -> TimelineItemRedactedView(
            content = content,
            onContentLayoutChange = onContentLayoutChange,
            modifier = modifier
        )
        is TimelineItemTextBasedContent -> TimelineItemTextView(
            content = content,
            modifier = modifier,
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
            onContentLayoutChange = onContentLayoutChange
        )
        is TimelineItemUnknownContent -> TimelineItemUnknownView(
            content = content,
            onContentLayoutChange = onContentLayoutChange,
            modifier = modifier
        )
        is TimelineItemLocationContent -> {
            TimelineItemLocationView(
                content = content.ensureActiveLiveLocation(),
                onStopLiveLocationClick = { eventSink(TimelineEvent.StopLiveLocationShare) },
                modifier = modifier
            )
        }
        is TimelineItemImageContent -> {
            TimelineItemImageView(
                content = content,
                hideMediaContent = hideMediaContent,
                onContentClick = onContentClick,
                onLongClick = onLongClick,
                onShowContentClick = onShowContentClick,
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
                onContentLayoutChange = onContentLayoutChange,
                contentValidationState = contentValidationState,
                modifier = modifier,
            )
        }
        is TimelineItemStickerContent -> {
            TimelineItemStickerView(
                content = content,
                hideMediaContent = hideMediaContent,
                onContentClick = onContentClick,
                onLongClick = onLongClick,
                onShowClick = onShowContentClick,
                modifier = modifier,
                isDangerousContent = contentValidationState.isInvalid(),
            )
        }
        is TimelineItemVideoContent -> {
            TimelineItemVideoView(
                content = content,
                hideMediaContent = hideMediaContent,
                onContentClick = onContentClick,
                onLongClick = onLongClick,
                onShowContentClick = onShowContentClick,
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
                onContentLayoutChange = onContentLayoutChange,
                contentValidationState = contentValidationState,
                modifier = modifier
            )
        }
        is TimelineItemFileContent -> {
            TimelineItemFileView(
                content = content,
                onContentLayoutChange = onContentLayoutChange,
                modifier = modifier,
                isDangerous = contentValidationState.isInvalid(),
            )
        }
        is TimelineItemAudioContent -> {
            TimelineItemAudioView(
                content = content,
                onContentLayoutChange = onContentLayoutChange,
                modifier = modifier,
                isDangerous = contentValidationState.isInvalid(),
            )
        }
        is TimelineItemLegacyCallInviteContent -> TimelineItemLegacyCallInviteView(modifier = modifier)
        is TimelineItemStateContent -> TimelineItemStateView(
            content = content,
            modifier = modifier
        )
        is TimelineItemPollContent -> TimelineItemPollView(
            content = content,
            eventSink = eventSink,
            modifier = modifier,
        )
        is TimelineItemVoiceContent -> {
            val presenter: Presenter<VoiceMessageState> = presenterFactories.rememberPresenter(content)
            TimelineItemVoiceView(
                state = presenter.present(),
                content = content,
                onContentLayoutChange = onContentLayoutChange,
                modifier = modifier,
                isDangerousContent = contentValidationState.isInvalid(),
            )
        }
        is TimelineItemRtcNotificationContent -> error("This shouldn't be rendered as the content of a bubble")
    }
}

@Composable
private fun ValidateMediaHelper(
    eventId: EventId,
    content: TimelineItemEventContent,
    contentValidationState: MutableState<AsyncData<Boolean>>,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
) {
    val mediaSource = when (content) {
        is TimelineItemImageContent -> content.mediaSource
        is TimelineItemStickerContent -> content.mediaSource
        is TimelineItemVideoContent -> content.mediaSource
        is TimelineItemFileContent -> content.mediaSource
        is TimelineItemAudioContent -> content.mediaSource
        is TimelineItemVoiceContent -> content.mediaSource
        else -> return
    }
    LaunchedEffect(eventId, mediaSource) {
        eventSink(TimelineEvent.ValidateMedia(eventId, mediaSource, contentValidationState))
    }
}
