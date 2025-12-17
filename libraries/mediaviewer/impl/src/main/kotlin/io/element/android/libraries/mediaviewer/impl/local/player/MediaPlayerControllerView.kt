/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.libraries.dateformatter.api.toHumanReadableDuration
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Slider
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.mediaviewer.impl.util.bgCanvasWithTransparency
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

@Composable
fun MediaPlayerControllerView(
    state: MediaPlayerControllerState,
    onTogglePlay: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    audioFocus: AudioFocus?,
    modifier: Modifier = Modifier,
) {
    if (audioFocus != null) {
        val latestOnTogglePlay by rememberUpdatedState(onTogglePlay)
        LaunchedEffect(state.isPlaying) {
            if (state.isPlaying) {
                audioFocus.requestAudioFocus(
                    requester = AudioFocusRequester.MediaViewer,
                    onFocusLost = {
                        Timber.w("Audio focus lost")
                        latestOnTogglePlay()
                    },
                )
            } else {
                audioFocus.releaseAudioFocus()
            }
        }
    }

    AnimatedVisibility(
        visible = state.isVisible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .background(color = bgCanvasWithTransparency)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 480.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val bgColor = if (state.isPlaying) {
                    ElementTheme.colors.bgCanvasDefault
                } else {
                    ElementTheme.colors.textPrimary
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = bgColor,
                            shape = CircleShape,
                        )
                        .clip(CircleShape)
                        .clickable { onTogglePlay() }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isPlaying) {
                        Icon(
                            imageVector = CompoundIcons.PauseSolid(),
                            tint = ElementTheme.colors.iconPrimary,
                            contentDescription = stringResource(CommonStrings.a11y_pause)
                        )
                    } else {
                        Icon(
                            imageVector = CompoundIcons.PlaySolid(),
                            tint = ElementTheme.colors.iconOnSolidPrimary,
                            contentDescription = stringResource(CommonStrings.a11y_play)
                        )
                    }
                }
                Text(
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .padding(horizontal = 8.dp),
                    text = state.progressInMillis.toHumanReadableDuration(),
                    textAlign = TextAlign.Center,
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontBodyXsMedium,
                )
                var lastSelectedValue by remember { mutableFloatStateOf(-1f) }
                Slider(
                    modifier = Modifier.weight(1f),
                    valueRange = 0f..state.durationInMillis.toFloat(),
                    value = lastSelectedValue.takeIf { it >= 0 } ?: state.progressInMillis.toFloat(),
                    onValueChange = {
                        lastSelectedValue = it
                    },
                    onValueChangeFinish = {
                        onSeekChange(lastSelectedValue)
                        lastSelectedValue = -1f
                    },
                    useCustomLayout = true,
                )
                val formattedDuration = remember(state.durationInMillis) {
                    state.durationInMillis.toHumanReadableDuration()
                }
                Text(
                    modifier = Modifier
                        .widthIn(min = 48.dp)
                        .padding(horizontal = 8.dp),
                    text = formattedDuration,
                    textAlign = TextAlign.Center,
                    color = ElementTheme.colors.textPrimary,
                    style = ElementTheme.typography.fontBodyXsMedium,
                )
                if (state.canMute) {
                    IconButton(
                        onClick = onToggleMute,
                    ) {
                        if (state.isMuted) {
                            Icon(
                                imageVector = CompoundIcons.VolumeOffSolid(),
                                tint = ElementTheme.colors.iconPrimary,
                                contentDescription = stringResource(CommonStrings.common_unmute)
                            )
                        } else {
                            Icon(
                                imageVector = CompoundIcons.VolumeOnSolid(),
                                tint = ElementTheme.colors.iconPrimary,
                                contentDescription = stringResource(CommonStrings.common_mute)
                            )
                        }
                    }
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun MediaPlayerControllerViewPreview(
    @PreviewParameter(MediaPlayerControllerStateProvider::class) state: MediaPlayerControllerState
) = ElementPreview {
    MediaPlayerControllerView(
        state = state,
        onTogglePlay = {},
        onSeekChange = {},
        onToggleMute = {},
        audioFocus = null,
    )
}
