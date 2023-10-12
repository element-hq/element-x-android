/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleDown
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.voice.VoiceMessageEvents
import io.element.android.features.messages.impl.timeline.voice.VoiceMessageState
import io.element.android.features.messages.impl.timeline.voice.VoiceMessageStateProvider
import io.element.android.features.messages.impl.timeline.voice.WaveformProgressIndicator
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme

@Composable
fun TimelineItemVoiceView(
    state: VoiceMessageState,
    content: TimelineItemVoiceContent,
    extraPadding: ExtraPadding,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ElementTheme.materialColors.background)
                .clickable { state.eventSink(VoiceMessageEvents.PlayPause) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = when (state.button) {
                    VoiceMessageState.Button.Play -> Icons.Outlined.PlayArrow
                    VoiceMessageState.Button.Pause -> Icons.Outlined.Pause
                    VoiceMessageState.Button.Downloading -> Icons.Outlined.Downloading
                    VoiceMessageState.Button.Retry -> Icons.Outlined.ArrowCircleDown
                },
                contentDescription = null,
                tint = ElementTheme.materialColors.primary,
                modifier = Modifier
                    .size(16.dp),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = state.elapsed,
            color = ElementTheme.materialColors.secondary,
            style = ElementTheme.typography.fontBodySmRegular,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        WaveformProgressIndicator(
            modifier = Modifier
                .height(34.dp)
                .weight(1f),
            progress = state.progress,
            amplitudes = content.waveform,
            onSeek = { state.eventSink(VoiceMessageEvents.Seek(it)) }
        )
        Spacer(Modifier.width(extraPadding.getDpSize()))
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVoiceViewPreview(
    @PreviewParameter(VoiceMessageStateProvider::class) state: VoiceMessageState,
) = ElementPreview {
    TimelineItemVoiceView(
        state = state,
        content = aTimelineItemVoiceContent(),
        extraPadding = noExtraPadding,
    )
}
