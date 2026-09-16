/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.api

data class VoiceMessageState(
    val buttonType: ButtonType,
    val progress: Float,
    val time: String,
    val showCursor: Boolean,
    val playbackSpeed: Float,
    val eventSink: (event: VoiceMessageEvent) -> Unit,
) {
    enum class ButtonType {
        Play,
        Pause,
        Downloading,
        Retry,
        Disabled,
    }
}
