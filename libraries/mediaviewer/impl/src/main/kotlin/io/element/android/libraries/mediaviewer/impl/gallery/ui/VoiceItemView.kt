/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.media.WaveformPlaybackView
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemVoice
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.voiceplayer.api.VoiceMessageEvents
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.VoiceMessageStateProvider
import io.element.android.libraries.voiceplayer.api.aVoiceMessageState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@Composable
fun VoiceItemView(
    state: VoiceMessageState,
    voice: MediaItem.Voice,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        VoiceInfoRow(
            state = state,
            voice = voice,
            onLongClick = onLongClick,
        )
        val caption = voice.mediaInfo.caption
        if (caption != null) {
            CaptionView(caption)
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }
        HorizontalDivider()
    }
}

@Composable
private fun VoiceInfoRow(
    state: VoiceMessageState,
    voice: MediaItem.Voice,
    onLongClick: () -> Unit,
) {
    fun playPause() {
        state.eventSink(VoiceMessageEvents.PlayPause)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = ElementTheme.colors.bgSubtleSecondary,
                shape = RoundedCornerShape(12.dp),
            )
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick,
                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
            )
            .onKeyboardContextMenuAction(onLongClick)
            .fillMaxWidth()
            .padding(start = 12.dp, end = 36.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (state.button) {
            VoiceMessageState.Button.Play -> PlayButton(onClick = ::playPause)
            VoiceMessageState.Button.Pause -> PauseButton(onClick = ::playPause)
            VoiceMessageState.Button.Downloading -> ProgressButton()
            VoiceMessageState.Button.Retry -> RetryButton(onClick = ::playPause)
            VoiceMessageState.Button.Disabled -> PlayButton(onClick = {}, enabled = false)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (state.progress > 0f) state.time else voice.mediaInfo.duration ?: state.time,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodyMdMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(8.dp))
        WaveformPlaybackView(
            modifier = Modifier
                .weight(1f)
                .height(34.dp),
            showCursor = state.showCursor,
            playbackProgress = state.progress,
            waveform = voice.mediaInfo.waveform.orEmpty().toImmutableList(),
            onSeek = {
                state.eventSink(VoiceMessageEvents.Seek(it))
            },
            seekEnabled = true,
        )
    }
}

/**
 * Progress button is shown when the voice message is being downloaded.
 *
 * The progress indicator is optimistic and displays a pause button (which
 * indicates the audio is playing) for 2 seconds before revealing the
 * actual progress indicator.
 */
@Composable
private fun ProgressButton(
    displayImmediately: Boolean = false,
) {
    var canDisplay by remember { mutableStateOf(displayImmediately) }
    LaunchedEffect(Unit) {
        delay(2000L)
        canDisplay = true
    }
    CustomIconButton(
        onClick = {},
        enabled = false,
    ) {
        if (canDisplay) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(2.dp)
                    .size(16.dp),
                color = ElementTheme.colors.iconSecondary,
                strokeWidth = 2.dp,
            )
        } else {
            ControlIcon(
                imageVector = CompoundIcons.PauseSolid(),
                contentDescription = stringResource(id = CommonStrings.a11y_pause),
            )
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    CustomIconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.PlaySolid(),
            contentDescription = stringResource(id = CommonStrings.a11y_play),
        )
    }
}

@Composable
private fun PauseButton(
    onClick: () -> Unit,
) {
    CustomIconButton(
        onClick = onClick,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.PauseSolid(),
            contentDescription = stringResource(id = CommonStrings.a11y_pause),
        )
    }
}

@Composable
private fun RetryButton(
    onClick: () -> Unit,
) {
    CustomIconButton(
        onClick = onClick,
    ) {
        ControlIcon(
            imageVector = CompoundIcons.Restart(),
            contentDescription = stringResource(id = CommonStrings.action_retry),
        )
    }
}

@Composable
private fun ControlIcon(
    imageVector: ImageVector,
    contentDescription: String?,
) {
    Icon(
        modifier = Modifier.padding(vertical = 10.dp),
        imageVector = imageVector,
        contentDescription = contentDescription,
    )
}

@Composable
private fun CustomIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
            .border(
                width = 1.dp,
                color = ElementTheme.colors.borderInteractiveSecondary,
                shape = CircleShape,
            )
            .size(36.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = ElementTheme.colors.iconSecondary,
            disabledContentColor = ElementTheme.colors.iconDisabled,
        ),
        content = content,
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceItemViewPreview(
    @PreviewParameter(MediaItemVoiceProvider::class) voice: MediaItem.Voice,
) = ElementPreview {
    VoiceItemView(
        state = aVoiceMessageState(),
        voice = voice,
        onLongClick = {},
    )
}

@PreviewsDayNight
@Composable
internal fun VoiceItemViewPlayPreview(
    @PreviewParameter(VoiceMessageStateProvider::class) state: VoiceMessageState,
) = ElementPreview {
    VoiceItemView(
        state = state,
        voice = aMediaItemVoice(),
        onLongClick = {},
    )
}
