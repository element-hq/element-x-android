/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

sealed interface VoiceMessageEvents {
    data object PlayPause : VoiceMessageEvents
    data class Seek(val percentage: Float) : VoiceMessageEvents
}
