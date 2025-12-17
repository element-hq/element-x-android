/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.isEdited
import io.element.android.features.messages.impl.timeline.model.event.isRedacted
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.isCritical
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineEventTimestampView(
    event: TimelineItem.Event,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedTime = event.sentTime
    val hasError = event.failedToSend
    val hasEncryptionCritical = event.messageShield?.isCritical.orFalse()
    val isMessageEdited = event.content.isEdited()
    val isMessageRedacted = event.content.isRedacted()
    val tint = if (hasError || hasEncryptionCritical && !isMessageRedacted) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textSecondary
    Row(
        modifier = Modifier
            .padding(PaddingValues(start = TimelineEventTimestampViewDefaults.spacing))
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isMessageEdited) {
            Text(
                stringResource(CommonStrings.common_edited_suffix),
                style = ElementTheme.typography.fontBodyXsRegular,
                color = tint,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            formattedTime,
            style = ElementTheme.typography.fontBodyXsRegular,
            color = tint,
        )
        if (hasError) {
            val isVerifiedUserSendFailure = event.localSendState is LocalEventSendState.Failed.VerifiedUser
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = CompoundIcons.ErrorSolid(),
                contentDescription = stringResource(id = CommonStrings.common_sending_failed),
                tint = tint,
                modifier = Modifier
                    .size(15.dp, 18.dp)
                    .clickable(
                        enabled = isVerifiedUserSendFailure,
                        onClickLabel = stringResource(CommonStrings.action_open_context_menu),
                    ) {
                        eventSink(TimelineEvents.ComputeVerifiedUserSendFailure(event))
                    }
            )
        }

        if (!isMessageRedacted) {
            event.messageShield?.let { shield ->
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = shield.toIcon(),
                    contentDescription = stringResource(id = CommonStrings.a11y_encryption_details),
                    modifier = Modifier
                        .size(15.dp)
                        .clickable(
                            onClickLabel = stringResource(CommonStrings.a11y_view_details),
                        ) {
                            eventSink(TimelineEvents.ShowShieldDialog(shield))
                        },
                    tint = shield.toIconColor(),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineEventTimestampViewPreview(@PreviewParameter(TimelineItemEventForTimestampViewProvider::class) event: TimelineItem.Event) = ElementPreview {
    TimelineEventTimestampView(
        event = event,
        eventSink = {},
    )
}

object TimelineEventTimestampViewDefaults {
    val spacing = 16.dp
}
