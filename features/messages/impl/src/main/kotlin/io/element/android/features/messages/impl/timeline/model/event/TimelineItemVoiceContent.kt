/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

data class TimelineItemVoiceContent(
    val eventId: EventId?,
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val duration: Duration,
    val mediaSource: MediaSource,
    val mimeType: String,
    val waveform: ImmutableList<Float>,
) : TimelineItemEventContentWithAttachment {
    override val type: String = "TimelineItemAudioContent"
}
