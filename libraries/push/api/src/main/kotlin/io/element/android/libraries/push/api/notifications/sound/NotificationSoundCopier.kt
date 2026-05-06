/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications.sound

/**
 * Copies a picked notification sound into app-private storage and re-exposes it via the app's
 * FileProvider, so the persisted reference outlives the original source.
 */
interface NotificationSoundCopier {
    suspend fun copyToAppFiles(sourceUriString: String, slot: SoundSlot): CopyResult

    /**
     * Removes any previously copied file for [slot] from app-private storage. No-op when no file
     * exists. Call this when persisting a non-[NotificationSound.Custom] pick (SystemDefault /
     * Silent) so the previous Custom copy doesn't linger as orphaned bytes — [copyToAppFiles]
     * already sweeps stale files inline for Custom-to-Custom transitions.
     */
    suspend fun deleteStoredSoundFor(slot: SoundSlot)

    enum class SoundSlot { Message, Call }

    sealed interface CopyResult {
        /**
         * @property fileProviderUriString FileProvider URI to persist as the channel sound.
         * @property displayName [android.media.Ringtone.getTitle] for the source, or null. The
         * picker substitutes a localised fallback label, so localisation stays in the UI layer.
         */
        data class Success(val fileProviderUriString: String, val displayName: String?) : CopyResult

        /** [android.media.RingtoneManager] could not open the source URI as a Ringtone. */
        data object UnplayableSource : CopyResult

        /** The bytes copied successfully, but the resulting file is not a playable Ringtone. */
        data object UnplayableCopy : CopyResult

        /** Source exceeded the 5 MB cap. */
        data object FileTooLarge : CopyResult

        /** Any other I/O or unexpected failure. */
        data class Failure(val cause: Throwable) : CopyResult
    }
}
