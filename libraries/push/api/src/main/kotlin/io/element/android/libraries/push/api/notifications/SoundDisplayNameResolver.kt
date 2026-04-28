/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

/**
 * Resolves the user-facing title of a custom notification sound URI (the title shown by the
 * system ringtone picker). Lifts the [android.media.RingtoneManager] side effect out of UI code
 * so views and presenters stay testable.
 */
interface SoundDisplayNameResolver {
    /** Returns the title of the ringtone at [uri], or null if it cannot be resolved. */
    suspend fun resolveCustomSoundTitle(uri: String): String?
}
