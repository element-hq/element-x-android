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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContentProvider
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageEvents
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageState
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageStateProvider
import io.element.android.features.messages.impl.voicemessages.timeline.Waveform
import io.element.android.features.messages.impl.voicemessages.timeline.WaveformPlaybackView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun TimelineItemVoiceView(
    state: VoiceMessageState,
    content: TimelineItemVoiceContent,
    extraPadding: ExtraPadding,
    modifier: Modifier = Modifier,
) {
    fun playPause() {
        state.eventSink(VoiceMessageEvents.PlayPause)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state.button) {
            VoiceMessageState.Button.Play -> PlayButton(onClick = ::playPause)
            VoiceMessageState.Button.Pause -> PauseButton(onClick = ::playPause)
            VoiceMessageState.Button.Downloading -> ProgressButton()
            VoiceMessageState.Button.Retry -> RetryButton(onClick = ::playPause)
            VoiceMessageState.Button.Disabled -> DisabledPlayButton()
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = state.time,
            color = ElementTheme.materialColors.secondary,
            style = ElementTheme.typography.fontBodySmMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        WaveformPlaybackView(
            showCursor = state.button == VoiceMessageState.Button.Pause,
            playbackProgress = state.progress,
            waveform = Waveform(data = content.waveform),
            modifier = Modifier
                .height(34.dp)
                .weight(1f),
            onSeek = { state.eventSink(VoiceMessageEvents.Seek(it)) },
        )
        Spacer(Modifier.width(extraPadding.getDpSize()))
    }
}

@Composable
private fun PlayButton(
    onClick: (() -> Unit)
) {
    IconButton(
        drawableRes = R.drawable.play,
        contentDescription = stringResource(id = CommonStrings.a11y_play),
        onClick = onClick
    )
}

@Composable
private fun PauseButton(
    onClick: (() -> Unit)
) {
    IconButton(
        drawableRes = R.drawable.pause,
        contentDescription = stringResource(id = CommonStrings.a11y_play),
        onClick = onClick
    )
}

@Composable
private fun RetryButton(
    onClick: (() -> Unit)
) {
    IconButton(
        drawableRes = R.drawable.retry,
        contentDescription = stringResource(id = CommonStrings.action_retry),
        onClick = onClick
    )
}

@Composable
private fun ProgressButton() {
    Button {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(2.dp)
                .size(16.dp),
            color = ElementTheme.colors.iconSecondary,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun DisabledPlayButton() {
    IconButton(
        drawableRes = R.drawable.play,
        contentDescription = null,
        onClick = null,
    )
}

@Composable
private fun IconButton(
    @DrawableRes drawableRes: Int,
    contentDescription: String?,
    onClick: (() -> Unit)?,
) {
    Button(
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            tint = ElementTheme.colors.iconSecondary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun Button(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(ElementTheme.materialColors.background)
            .let {
                if (onClick != null) it.clickable(onClick = onClick) else it
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

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

data class TimelineItemVoiceViewParameters(
    val state: VoiceMessageState,
    val content: TimelineItemVoiceContent,
)

@PreviewsDayNight
@Composable
internal fun TimelineItemVoiceViewPreview(
    @PreviewParameter(TimelineItemVoiceViewParametersProvider::class) timelineItemVoiceViewParameters: TimelineItemVoiceViewParameters,
) = ElementPreview {
    TimelineItemVoiceView(
        state = timelineItemVoiceViewParameters.state,
        content = timelineItemVoiceViewParameters.content,
        extraPadding = noExtraPadding,
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemVoiceViewUnifiedPreview() = ElementPreview {
    val timelineItemVoiceViewParametersProvider = TimelineItemVoiceViewParametersProvider()
    Column {
        timelineItemVoiceViewParametersProvider.values.forEach {
            TimelineItemVoiceView(
                state = it.state,
                content = it.content,
                extraPadding = noExtraPadding,
            )
        }
    }
}
