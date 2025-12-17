/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContentProvider
import io.element.android.libraries.designsystem.components.media.WaveformPlaybackView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.time.isTalkbackActive
import io.element.android.libraries.voiceplayer.api.VoiceMessageEvents
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.VoiceMessageStateProvider
import kotlinx.coroutines.delay

@Composable
fun TimelineItemVoiceView(
    state: VoiceMessageState,
    content: TimelineItemVoiceContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun playPause() {
        state.eventSink(VoiceMessageEvents.PlayPause)
    }

    val a11y = stringResource(CommonStrings.common_voice_message)
    val a11yActionLabel = stringResource(
        when (state.button) {
            VoiceMessageState.Button.Play -> CommonStrings.a11y_play
            VoiceMessageState.Button.Pause -> CommonStrings.a11y_pause
            VoiceMessageState.Button.Downloading -> CommonStrings.common_downloading
            VoiceMessageState.Button.Retry -> CommonStrings.action_retry
            VoiceMessageState.Button.Disabled -> CommonStrings.error_unknown
        }
    )
    Row(
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = a11y
                if (state.button == VoiceMessageState.Button.Disabled) {
                    disabled()
                } else if (state.button in listOf(VoiceMessageState.Button.Play, VoiceMessageState.Button.Pause)) {
                    onClick(label = a11yActionLabel) {
                        playPause()
                        true
                    }
                }
            }
            .onSizeChanged {
                onContentLayoutChange(
                    ContentAvoidingLayoutData(
                        contentWidth = it.width,
                        contentHeight = it.height,
                    )
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isTalkbackActive()) {
            when (state.button) {
                VoiceMessageState.Button.Play -> PlayButton(onClick = ::playPause)
                VoiceMessageState.Button.Pause -> PauseButton(onClick = ::playPause)
                VoiceMessageState.Button.Downloading -> ProgressButton()
                VoiceMessageState.Button.Retry -> RetryButton(onClick = ::playPause)
                VoiceMessageState.Button.Disabled -> PlayButton(onClick = {}, enabled = false)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = state.time,
            color = ElementTheme.colors.textSecondary,
            style = ElementTheme.typography.fontBodySmMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(8.dp))
        WaveformPlaybackView(
            showCursor = state.showCursor,
            playbackProgress = state.progress,
            waveform = content.waveform,
            modifier = Modifier.height(34.dp),
            seekEnabled = !isTalkbackActive(),
            onSeek = { state.eventSink(VoiceMessageEvents.Seek(it)) },
        )
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
private fun CustomIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(color = ElementTheme.colors.bgCanvasDefault, shape = CircleShape)
            .size(36.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = ElementTheme.colors.iconSecondary,
            disabledContentColor = ElementTheme.colors.iconDisabled,
        ),
        content = content,
    )
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
        onContentLayoutChange = {},
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
                onContentLayoutChange = {},
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ProgressButtonPreview() = ElementPreview {
    Row {
        ProgressButton(displayImmediately = true)
        ProgressButton(displayImmediately = false)
    }
}
