/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

object VoicePlayerConfig {
    // Available playback speeds for voice messages, the first one is the default speed, and
    // the UI will allow to change to the next speed in the list, in loop.
    val availablePlaybackSpeeds = listOf(1.0f, 1.5f, 2.0f, 0.5f)
}
