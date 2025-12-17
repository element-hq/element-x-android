/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline.voicemessages.composer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import kotlin.time.Duration.Companion.seconds

open class VoiceMessageComposerStateProvider : PreviewParameterProvider<VoiceMessageComposerState> {
    override val values: Sequence<VoiceMessageComposerState>
        get() = sequenceOf(
            aVoiceMessageComposerState(voiceMessageState = VoiceMessageState.Recording(duration = 61.seconds, levels = WaveFormSamples.allRangeWaveForm)),
        )
}

fun aVoiceMessageComposerState(
    voiceMessageState: VoiceMessageState = VoiceMessageState.Idle,
    keepScreenOn: Boolean = false,
    showPermissionRationaleDialog: Boolean = false,
    showSendFailureDialog: Boolean = false,
) = VoiceMessageComposerState(
    voiceMessageState = voiceMessageState,
    showPermissionRationaleDialog = showPermissionRationaleDialog,
    showSendFailureDialog = showSendFailureDialog,
    keepScreenOn = keepScreenOn,
    eventSink = {},
)

fun aVoiceMessagePreviewState() = VoiceMessageState.Preview(
    isSending = false,
    isPlaying = false,
    showCursor = false,
    playbackProgress = 0f,
    time = 10.seconds,
    waveform = WaveFormSamples.realisticWaveForm,
)
