/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.api

/**
 * Manages audio routing and proximity sensor for voice messages.
 */
interface VoiceMessageAudioManager {
    /**
     * Start audio routing for voice message playback.
     */
    fun startRouting()

    /**
     * Stop audio routing for voice message playback.
     */
    fun stopRouting()
}
