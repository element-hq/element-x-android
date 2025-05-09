/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContentProvider
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.VoiceMessageStateProvider

data class TimelineItemVoiceViewParameters(
    val state: VoiceMessageState,
    val content: TimelineItemVoiceContent,
)

open class TimelineItemVoiceViewParametersProvider : PreviewParameterProvider<TimelineItemVoiceViewParameters> {
    private val voiceMessageStateProvider = VoiceMessageStateProvider()
    private val timelineItemVoiceContentProvider = TimelineItemVoiceContentProvider()
    override val values: Sequence<TimelineItemVoiceViewParameters>
        get() = timelineItemVoiceContentProvider.values.flatMap { content ->
            voiceMessageStateProvider.values.map { state ->
                TimelineItemVoiceViewParameters(
                    state = state,
                    content = content,
                )
            }
        }
}
