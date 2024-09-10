/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

data class VoiceMessageState(
    val button: Button,
    val progress: Float,
    val time: String,
    val showCursor: Boolean,
    val eventSink: (event: VoiceMessageEvents) -> Unit,
) {
    enum class Button {
        Play,
        Pause,
        Downloading,
        Retry,
        Disabled,
    }
}
