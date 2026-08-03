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

    suspend fun setLiveLocationMinimumDistanceInMetersUpdate(value: Int)
    fun getLiveLocationMinimumDistanceInMetersUpdateFlow(): Flow<Int>

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

    /**
     * The list of account providers (homeserver URLs) the user has previously authenticated
     * against, most recent first. Used to default the account provider and to power autocomplete
     * suggestions during sign-in. Stored locally only; never synced across devices.
     */
    fun getHomeserverHistoryFlow(): Flow<List<String>>

    /**
     * Add [url] to the front of the account provider history (see [getHomeserverHistoryFlow]).
     * The value is normalised (trimmed + lowercased); existing case-insensitive duplicates are
     * moved to the front rather than duplicated, and the list is capped in size. No-op for blanks.
     */
    suspend fun addHomeserverToHistory(url: String)

    suspend fun setTracingLogPacks(targets: Set<TraceLogPack>)
    fun getTracingLogPacksFlow(): Flow<Set<TraceLogPack>>

    fun getMessageSoundFlow(): Flow<NotificationSound>

    /**
     * Atomically persists [sound] (with copy-time [title] for Custom; cleared otherwise) and
     * bumps the channel version. Single transaction so process death can't desync URI and version.
     */
    suspend fun setMessageSoundAndIncrementVersion(sound: NotificationSound, title: String?): Int

    /** Title captured at copy time. Null for SystemDefault / Silent or pre-title persisted data. */
    fun getMessageSoundDisplayNameFlow(): Flow<String?>

    fun getCallRingtoneFlow(): Flow<NotificationSound>

    /** See [setMessageSoundAndIncrementVersion]. */
    suspend fun setCallRingtoneAndIncrementVersion(sound: NotificationSound, title: String?): Int

    /** See [getMessageSoundDisplayNameFlow]. */
    fun getCallRingtoneDisplayNameFlow(): Flow<String?>

    /** Single-snapshot read of all sound prefs; used at boot to seed channels without N reads. */
    suspend fun getNotificationSoundChannelConfig(): NotificationSoundChannelConfig

    suspend fun reset()
}
