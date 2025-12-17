/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.designsystem.components.media.WaveformPlaybackView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.formatShort
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun VoiceMessagePreview(
    isInteractive: Boolean,
    isPlaying: Boolean,
    showCursor: Boolean,
    waveform: ImmutableList<Float>,
    time: Duration,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    playbackProgress: Float = 0f,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(start = 8.dp, end = 20.dp, top = 6.dp, bottom = 6.dp)
            .heightIn(26.dp),
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

        Text(
            text = time.formatShort(),
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodySmMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.width(12.dp))

        WaveformPlaybackView(
            modifier = Modifier
                .weight(1f)
                .height(26.dp),
            playbackProgress = playbackProgress,
            showCursor = showCursor,
            waveform = waveform,
            seekEnabled = true,
            onSeek = onSeek,
        )
    }
}

private enum class PlayerButtonType {
    Play,
    Pause
}

@Composable
private fun PlayerButton(
    type: PlayerButtonType,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
            .size(30.dp),
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
    imageVector = CompoundIcons.PauseSolid(),
    contentDescription = stringResource(id = CommonStrings.a11y_pause),
    modifier = Modifier
        .size(20.dp)
        .padding(2.dp),
)

@Composable
private fun PlayIcon() = Icon(
    imageVector = CompoundIcons.PlaySolid(),
    contentDescription = stringResource(id = CommonStrings.a11y_play),
    modifier = Modifier
        .size(20.dp)
        .padding(2.dp),
)

@PreviewsDayNight
@Composable
internal fun VoiceMessagePreviewPreview() = ElementPreview {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AVoiceMessagePreview(
            isInteractive = true,
            isPlaying = true,
            time = 2.seconds,
            playbackProgress = 0.2f,
            showCursor = true,
            waveform = WaveFormSamples.longRealisticWaveForm,
        )
        AVoiceMessagePreview(
            isInteractive = true,
            isPlaying = false,
            time = 0.seconds,
            playbackProgress = 0.0f,
            showCursor = true,
            waveform = WaveFormSamples.longRealisticWaveForm,
        )
        AVoiceMessagePreview(
            isInteractive = false,
            isPlaying = false,
            time = 789.seconds,
            playbackProgress = 0.0f,
            showCursor = false,
            waveform = WaveFormSamples.longRealisticWaveForm,
        )
    }
}

@Composable
private fun AVoiceMessagePreview(
    isInteractive: Boolean,
    isPlaying: Boolean,
    time: Duration,
    playbackProgress: Float,
    showCursor: Boolean,
    waveform: ImmutableList<Float>,
) {
    VoiceMessagePreview(
        isInteractive = isInteractive,
        isPlaying = isPlaying,
        time = time,
        playbackProgress = playbackProgress,
        showCursor = showCursor,
        waveform = waveform,
        onPlayClick = {},
        onPauseClick = {},
        onSeek = {},
    )
}
