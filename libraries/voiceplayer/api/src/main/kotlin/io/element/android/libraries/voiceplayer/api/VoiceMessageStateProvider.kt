/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class VoiceMessageStateProvider : PreviewParameterProvider<VoiceMessageState> {
    override val values: Sequence<VoiceMessageState>
        get() = sequenceOf(
            aVoiceMessageState(
                VoiceMessageState.Button.Downloading,
                progress = 0f,
                time = "0:00",
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Retry,
                progress = 0.5f,
                time = "0:01",
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Play,
                progress = 1f,
                time = "1:00",
                showCursor = true,
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Pause,
                progress = 0.2f,
                time = "10:00",
                showCursor = true,
            ),
            aVoiceMessageState(
                VoiceMessageState.Button.Disabled,
                progress = 0.2f,
                time = "30:00",
            ),
        )
}

fun aVoiceMessageState(
    button: VoiceMessageState.Button = VoiceMessageState.Button.Play,
    progress: Float = 0f,
    time: String = "1:00",
    showCursor: Boolean = false,
) = VoiceMessageState(
    button = button,
    progress = progress,
    time = time,
    showCursor = showCursor,
    eventSink = {},
)
