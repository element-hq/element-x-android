/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

sealed interface VoiceMessagePlayerEvent {
    data object Play : VoiceMessagePlayerEvent
    data object Pause : VoiceMessagePlayerEvent

    data class Seek(
        val position: Float
    ) : VoiceMessagePlayerEvent
}
