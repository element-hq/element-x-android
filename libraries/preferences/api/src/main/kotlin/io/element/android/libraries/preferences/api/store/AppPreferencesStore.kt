/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import kotlinx.coroutines.flow.Flow

interface AppPreferencesStore {
    suspend fun setDeveloperModeEnabled(enabled: Boolean)
    fun isDeveloperModeEnabledFlow(): Flow<Boolean>

    suspend fun setCustomElementCallBaseUrl(string: String?)
    fun getCustomElementCallBaseUrlFlow(): Flow<String?>

    suspend fun setTheme(theme: String)
    fun getThemeFlow(): Flow<String?>

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    suspend fun setHideInviteAvatars(hide: Boolean?)
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    fun getHideInviteAvatarsFlow(): Flow<Boolean?>
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    suspend fun setTimelineMediaPreviewValue(mediaPreviewValue: MediaPreviewValue?)
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    fun getTimelineMediaPreviewValueFlow(): Flow<MediaPreviewValue?>

    suspend fun setTracingLogLevel(logLevel: LogLevel)
    fun getTracingLogLevelFlow(): Flow<LogLevel>

    suspend fun setTracingLogPacks(targets: Set<TraceLogPack>)
    fun getTracingLogPacksFlow(): Flow<Set<TraceLogPack>>

    fun getMessageSoundFlow(): Flow<NotificationSound>

    /**
     * Atomically persists [sound] as the user's chosen message sound and bumps the channel version
     * counter. Returns the new version. Combined into a single transaction so process death
     * between persistence and channel recreation cannot leave the URI and version out of sync.
     */
    suspend fun setMessageSoundAndIncrementVersion(sound: NotificationSound): Int

    fun getCallRingtoneFlow(): Flow<NotificationSound>

    /** See [setMessageSoundAndIncrementVersion]. */
    suspend fun setCallRingtoneAndIncrementVersion(sound: NotificationSound): Int

    /**
     * One-shot read of all four notification-sound preferences in a single underlying DataStore
     * read. Used at app boot when channels are seeded — avoids four separate `runBlocking` reads.
     */
    suspend fun getNotificationSoundChannelConfig(): NotificationSoundChannelConfig

    suspend fun reset()
}
