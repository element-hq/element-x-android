/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.ensureActiveLiveLocation
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionEvent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.mediaSources
import io.element.android.libraries.matrix.ui.media.contentvalidation.collectOverallState
import io.element.android.libraries.matrix.ui.media.contentvalidation.rememberEventContentValidationState
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToView
import io.element.android.libraries.matrix.ui.messages.reply.content
import io.element.android.libraries.matrix.ui.messages.reply.eventId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.a11y.isTalkbackActive
import io.element.android.libraries.ui.utils.text.detect

@Composable
internal fun MessageEventBubbleContent(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode,
    timelineProtectionState: TimelineProtectionState,
    hasContentValidationError: Boolean,
    callbacks: TimelineItemCallbacks,
    eventContentView: EventContentView,
    @SuppressLint("ModifierParameter")
    // need to rename this modifier to prevent linter false positives
    @Suppress("ModifierNaming")
    bubbleModifier: Modifier = Modifier,
) {
    // The live location state can change over time, so it must be resolved before computing the layout
    val resolvedContent = when (val content = event.content) {
        is TimelineItemLocationContent -> content.ensureActiveLiveLocation()
        else -> content
    }
    val layoutSpec = bubbleLayoutSpec(
        content = resolvedContent,
        hasContentValidationError = hasContentValidationError,
    )
    CommonLayout(
        event = event,
        showThreadDecoration = timelineMode !is Timeline.Mode.Thread && event.threadInfo is TimelineItemThreadInfo.ThreadResponse,
        timestampPosition = layoutSpec.timestampPosition,
        paddingBehaviour = layoutSpec.contentPadding,
        inReplyToDetails = event.inReplyTo,
        canShrinkContent = layoutSpec.canShrinkContent,
        timelineProtectionState = timelineProtectionState,
        callbacks = callbacks,
        eventContentView = eventContentView,
        modifier = bubbleModifier,
    )
}

/** Groups the different components in a Column with some space between them. */
@Composable
private fun CommonLayout(
    event: TimelineItem.Event,
    timestampPosition: TimestampPosition,
    showThreadDecoration: Boolean,
    paddingBehaviour: ContentPadding,
    inReplyToDetails: InReplyToDetails?,
    timelineProtectionState: TimelineProtectionState,
    callbacks: TimelineItemCallbacks,
    eventContentView: EventContentView,
    modifier: Modifier = Modifier,
    canShrinkContent: Boolean = false,
) {
    val timestampLayoutModifier =
        if (inReplyToDetails != null && timestampPosition == TimestampPosition.Overlay) {
            Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        } else {
            Modifier
        }

    val topPadding = if (inReplyToDetails != null) 0.dp else 8.dp
    val contentModifier = when (paddingBehaviour) {
        ContentPadding.Textual ->
            Modifier.padding(start = 12.dp, end = 12.dp, top = topPadding, bottom = 8.dp)
        ContentPadding.Media -> {
            if (inReplyToDetails == null) {
                Modifier
            } else {
                Modifier.clip(RoundedCornerShape(10.dp))
            }
        }
        ContentPadding.CaptionedMedia ->
            Modifier.padding(start = 8.dp, end = 8.dp, top = topPadding, bottom = 8.dp)
        ContentPadding.InvalidContent -> Modifier.padding(top = topPadding, bottom = 8.dp)
    }

    val threadDecoration = @Composable {
        if (showThreadDecoration) {
            ThreadDecoration(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 12.dp, end = 12.dp))
        }
    }
    val contentWithTimestamp = @Composable {
        WithTimestampLayout(
            timestampPosition = timestampPosition,
            event = event,
            callbacks = callbacks,
            canShrinkContent = canShrinkContent,
            modifier = timestampLayoutModifier
                .semantics(mergeDescendants = false) {
                    isTraversalGroup = true
                    traversalIndex = -1f
                },
            content = { onContentLayoutChange ->
                eventContentView(event, timelineProtectionState, contentModifier, onContentLayoutChange, callbacks)
            }
        )
    }

    Column(modifier = modifier.width(IntrinsicSize.Max), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        threadDecoration()
        if (inReplyToDetails != null) {
            InReplyToBox(
                modifier = Modifier.fillMaxWidth(),
                inReplyTo = inReplyToDetails,
                topPadding = if (showThreadDecoration) 0.dp else 8.dp,
                timelineProtectionState = timelineProtectionState,
                callbacks = callbacks,
            )
        }
        contentWithTimestamp()
    }
}

@Composable
private fun InReplyToBox(
    inReplyTo: InReplyToDetails,
    topPadding: Dp,
    timelineProtectionState: TimelineProtectionState,
    callbacks: TimelineItemCallbacks,
    modifier: Modifier = Modifier,
) {
    val currentContentValidationState by rememberEventContentValidationState(eventId = inReplyTo.eventId(), eventContent = inReplyTo.content())
        .collectOverallState()
    val shape = RoundedCornerShape(6.dp)
    val inReplyToModifier = modifier
        .padding(top = topPadding, start = 8.dp, end = 8.dp)
        .clip(shape)

    val talkbackCompatModifier = if (isTalkbackActive()) {
        // Use z-index to make the replied to text being read after the message
        // Usually, you'd use traversalIndex for that, but it's not working for some reason
        inReplyToModifier.zIndex(1f)
    } else {
        inReplyToModifier.clickable(onClick = { callbacks.inReplyToClick(inReplyTo.eventId()) })
    }

    val contentHasError = currentContentValidationState.hasError()
    val borderColor = if (contentHasError) ElementTheme.colors.borderCriticalSubtle else ElementTheme.colors.separatorPrimary
    val backgroundColor = if (contentHasError) ElementTheme.colors.bgCriticalSubtle else ElementTheme.colors.bgCanvasDefault
    Box(
        modifier = talkbackCompatModifier
            .border(1.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .padding(4.dp)
    ) {
        val contentValidationState = rememberEventContentValidationState(eventId = inReplyTo.eventId(), eventContent = inReplyTo.content())
        LaunchedEffect(inReplyTo) {
            val mediaSources = inReplyTo.content()?.mediaSources() ?: return@LaunchedEffect
            timelineProtectionState.eventSink(TimelineProtectionEvent.ValidateContent(inReplyTo.eventId(), mediaSources, contentValidationState))
        }
        InReplyToView(
            inReplyTo = inReplyTo,
            contentValidationValue = currentContentValidationState,
            hideImage = timelineProtectionState.hideMediaContent(inReplyTo.eventId()),
        )
    }
}

@Composable
private fun WithTimestampLayout(
    timestampPosition: TimestampPosition,
    event: TimelineItem.Event,
    callbacks: TimelineItemCallbacks,
    modifier: Modifier = Modifier,
    canShrinkContent: Boolean = false,
    content: @Composable (onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit) -> Unit,
) {
    @Suppress("NAME_SHADOWING")
    val content = remember { movableContentOf(content) }
    when (timestampPosition) {
        TimestampPosition.Overlay ->
            Box(modifier, contentAlignment = Alignment.Center) {
                content {}
                TimelineEventTimestampView(
                    event = event,
                    callbacks = callbacks,
                    modifier = Modifier
                        // Outer padding
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .background(ElementTheme.colors.bgSubtleSecondary, RoundedCornerShape(10.0.dp))
                        .align(Alignment.BottomEnd)
                        // Inner padding
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        TimestampPosition.Aligned -> @Composable {
            val originalLayoutDirection = LocalLayoutDirection.current
            // Detect if the direction of the text content (if any) does not match the layout direction, to place the content and timestamp correctly
            val contentDirection = if (event.content is TimelineItemTextContent) {
                remember(event.content.body) {
                    when (TextDirection.detect(event.content.body)) {
                        TextDirection.Ltr, TextDirection.ContentOrLtr -> LayoutDirection.Ltr
                        TextDirection.Rtl, TextDirection.ContentOrRtl -> LayoutDirection.Rtl
                        else -> originalLayoutDirection
                    }
                }
            } else {
                originalLayoutDirection
            }

            CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                ContentAvoidingLayout(
                    modifier = modifier.fillMaxWidth(),
                    // The spacing is negative to make the content overlap the empty space at the start of the timestamp
                    spacing = (-8).dp,
                    overlayOffset = DpOffset(0.dp, -1.dp),
                    shrinkContent = canShrinkContent,
                    content = { content(this::onContentLayoutChange) },
                    overlay = {
                        // Use the original layout direction for the timestamp
                        CompositionLocalProvider(LocalLayoutDirection provides originalLayoutDirection) {
                            TimelineEventTimestampView(
                                event = event,
                                callbacks = callbacks,
                                isLayoutDirectionMismatched = originalLayoutDirection != contentDirection,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                )
            }
        }
        TimestampPosition.Below ->
            Column(modifier) {
                content {}
                TimelineEventTimestampView(
                    event = event,
                    callbacks = callbacks,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        TimestampPosition.Hidden -> Box(modifier) { content {} }
    }
}

@Composable
private fun ThreadDecoration(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.height(14.dp),
            imageVector = CompoundIcons.Threads(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
        )
        Text(
            text = stringResource(CommonStrings.common_thread),
            style = ElementTheme.typography.fontBodyXsRegular,
            color = ElementTheme.colors.textPrimary,
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}
