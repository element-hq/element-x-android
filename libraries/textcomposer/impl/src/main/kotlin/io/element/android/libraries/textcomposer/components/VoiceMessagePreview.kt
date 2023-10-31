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

package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.media.WaveformPlaybackView
import io.element.android.libraries.designsystem.components.media.createFakeWaveform
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.applyScaleUp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.textcomposer.R
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun VoiceMessagePreview(
    isInteractive: Boolean,
    isPlaying: Boolean,
    waveform: ImmutableList<Float>,
    modifier: Modifier = Modifier,
    playbackProgress: Float = 0f,
    onPlayClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(start = 8.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .heightIn(26.dp.applyScaleUp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isPlaying) {
            PlayerButton(
                type = PlayerButtonType.Pause,
                onClick = onPauseClick,
                enabled = isInteractive,
            )
        } else {
            PlayerButton(
                type = PlayerButtonType.Play,
                onClick = onPlayClick,
                enabled = isInteractive
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // TODO Add timer UI

        Spacer(modifier = Modifier.width(12.dp))

        WaveformPlaybackView(
            modifier = Modifier
                .weight(1f)
                .height(26.dp.applyScaleUp()),
            playbackProgress = playbackProgress,
            showCursor = isInteractive,
            waveform = waveform,
            seekEnabled = false, // TODO enable seeking
            onSeek = onSeek,
        )
    }
}

private enum class PlayerButtonType {
    Play, Pause
}

@Composable
private fun PlayerButton(
    type: PlayerButtonType,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .background(color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
            .size(30.dp.applyScaleUp()),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = ElementTheme.colors.iconSecondary,
            disabledContentColor = ElementTheme.colors.iconDisabled,
        ),
    ) {
        when (type) {
            PlayerButtonType.Play -> PlayIcon()
            PlayerButtonType.Pause -> PauseIcon()
        }
    }
}

@Composable
private fun PauseIcon() = Icon(
    resourceId = R.drawable.ic_pause,
    contentDescription = stringResource(id = CommonStrings.a11y_pause),
    modifier = Modifier.size(20.dp.applyScaleUp()),
)

@Composable
private fun PlayIcon() = Icon(
    resourceId = R.drawable.ic_play,
    contentDescription = stringResource(id = CommonStrings.a11y_play),
    modifier = Modifier.size(20.dp.applyScaleUp()),
)

@PreviewsDayNight
@Composable
internal fun VoiceMessagePreviewPreview() = ElementPreview {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VoiceMessagePreview(isInteractive = true, isPlaying = true, waveform = createFakeWaveform())
        VoiceMessagePreview(isInteractive = true, isPlaying = false, waveform = createFakeWaveform())
        VoiceMessagePreview(isInteractive = false, isPlaying = false, waveform = createFakeWaveform())
    }
}
