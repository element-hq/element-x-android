/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.media.WaveformPlaybackView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.textcomposer.R
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.formatShort
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Recording UI shown in hold mode — timer + "< Slide to cancel" hint.
 */
@Composable
internal fun VoiceMessageRecordingHold(
    duration: Duration,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 8.dp)
            .heightIn(26.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RedRecordingDot()

        Spacer(Modifier.size(8.dp))

        Text(
            text = duration.formatShort(),
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodySmMedium,
        )

        Spacer(Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = CompoundIcons.ChevronLeft(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
            Text(
                text = stringResource(R.string.voice_message_slide_to_cancel),
                color = ElementTheme.colors.textSecondary,
                style = ElementTheme.typography.fontBodySmRegular,
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

/**
 * Recording UI shown in locked mode (WhatsApp-style).
 *
 * Three sub-states:
 * - Recording: [timer] [live waveform] on top, [delete] [pause] [send] on bottom.
 * - Paused: [play] [dot] [static waveform] [timer] on top, [delete] [resume] [send] on bottom.
 * - Playback review: [play/pause] [seekable waveform + progress] [timer] on top, [delete] [send] on bottom.
 */
@Composable
internal fun VoiceMessageRecordingLocked(
    duration: Duration,
    levels: ImmutableList<Float>,
    isPaused: Boolean,
    isPlayingBack: Boolean,
    isPlaying: Boolean,
    playbackProgress: Float,
    playbackTime: Duration,
    waveform: ImmutableList<Float>?,
    onDeleteClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPausePlaybackClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Top row: waveform area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(26.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                isPlayingBack -> {
                    // Playback review: [play/pause] [seekable waveform] [time]
                    IconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = if (isPlaying) onPausePlaybackClick else onPlayClick,
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = if (isPlaying) CompoundIcons.Pause() else CompoundIcons.PlaySolid(),
                            contentDescription = stringResource(
                                if (isPlaying) CommonStrings.a11y_pause else CommonStrings.a11y_play
                            ),
                            tint = ElementTheme.colors.iconSecondary,
                        )
                    }
                    Spacer(Modifier.size(4.dp))
                    WaveformPlaybackView(
                        modifier = Modifier
                            .height(26.dp)
                            .weight(1f),
                        playbackProgress = playbackProgress,
                        showCursor = isPlaying,
                        waveform = waveform ?: levels,
                        onSeek = onSeek,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (isPlaying) playbackTime.formatShort() else duration.formatShort(),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodySmMedium,
                    )
                }
                isPaused -> {
                    // Paused: [play] [dot] [waveform] [timer]
                    IconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = onPlayClick,
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.PlaySolid(),
                            contentDescription = stringResource(CommonStrings.a11y_play),
                            tint = ElementTheme.colors.iconSecondary,
                        )
                    }
                    Spacer(Modifier.size(4.dp))
                    RedRecordingDot()
                    Spacer(Modifier.size(8.dp))
                    LiveWaveformView(
                        modifier = Modifier
                            .height(26.dp)
                            .weight(1f),
                        levels = levels,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = duration.formatShort(),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodySmMedium,
                    )
                }
                else -> {
                    // Recording: [timer] [live waveform]
                    Text(
                        text = duration.formatShort(),
                        color = ElementTheme.colors.textSecondary,
                        style = ElementTheme.typography.fontBodySmMedium,
                    )
                    Spacer(Modifier.size(12.dp))
                    LiveWaveformView(
                        modifier = Modifier
                            .height(26.dp)
                            .weight(1f),
                        levels = levels,
                    )
                }
            }
        }

        // Bottom row: action buttons spread evenly
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Delete button (left)
            IconButton(
                onClick = onDeleteClick,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = CompoundIcons.Delete(),
                    contentDescription = stringResource(CommonStrings.a11y_delete),
                    tint = ElementTheme.colors.iconSecondary,
                )
            }

            if (!isPlayingBack) {
                // Pause/Resume button (center) — only when recording or paused, not during playback
                IconButton(
                    onClick = if (isPaused) onResumeClick else onPauseClick,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = ElementTheme.colors.textCriticalPrimary,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = if (isPaused) CompoundIcons.MicOn() else CompoundIcons.Pause(),
                            contentDescription = stringResource(
                                if (isPaused) CommonStrings.a11y_voice_message_record else CommonStrings.a11y_pause
                            ),
                            tint = ElementTheme.colors.iconOnSolidPrimary,
                    )
                }
                }
            }

            // Send button (right)
            IconButton(
                onClick = onSendClick,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = ElementTheme.colors.bgActionPrimaryRest,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = CompoundIcons.Send(),
                        contentDescription = stringResource(CommonStrings.action_send),
                        tint = ElementTheme.colors.iconOnSolidPrimary,
                    )
                }
            }
        }
    }
}

@Composable
internal fun RedRecordingDot() {
    val infiniteTransition = rememberInfiniteTransition("RedRecordingDot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = InfiniteRepeatableSpec(
            animation = TweenSpec(durationMillis = 1_000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "RedRecordingDotAlpha",
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .alpha(alpha)
            .background(color = ElementTheme.colors.textCriticalPrimary, shape = CircleShape)
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageRecordingHoldPreview() = ElementPreview {
    VoiceMessageRecordingHold(duration = 5.seconds)
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageRecordingLockedPreview() = ElementPreview {
    VoiceMessageRecordingLocked(
        duration = 12.seconds,
        levels = List(50) { it.toFloat() / 50 }.toImmutableList(),
        isPaused = false,
        isPlayingBack = false,
        isPlaying = false,
        playbackProgress = 0f,
        playbackTime = 0.seconds,
        waveform = null,
        onDeleteClick = {},
        onPauseClick = {},
        onResumeClick = {},
        onPlayClick = {},
        onPausePlaybackClick = {},
        onSeek = {},
        onSendClick = {},
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageRecordingLockedPausedPreview() = ElementPreview {
    VoiceMessageRecordingLocked(
        duration = 12.seconds,
        levels = List(50) { it.toFloat() / 50 }.toImmutableList(),
        isPaused = true,
        isPlayingBack = false,
        isPlaying = false,
        playbackProgress = 0f,
        playbackTime = 0.seconds,
        waveform = null,
        onDeleteClick = {},
        onPauseClick = {},
        onResumeClick = {},
        onPlayClick = {},
        onPausePlaybackClick = {},
        onSeek = {},
        onSendClick = {},
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceMessageRecordingLockedPlaybackPreview() = ElementPreview {
    VoiceMessageRecordingLocked(
        duration = 12.seconds,
        levels = List(50) { it.toFloat() / 50 }.toImmutableList(),
        isPaused = true,
        isPlayingBack = true,
        isPlaying = true,
        playbackProgress = 0.4f,
        playbackTime = 5.seconds,
        waveform = List(50) { it.toFloat() / 50 }.toImmutableList(),
        onDeleteClick = {},
        onPauseClick = {},
        onResumeClick = {},
        onPlayClick = {},
        onPausePlaybackClick = {},
        onSeek = {},
        onSendClick = {},
    )
}
