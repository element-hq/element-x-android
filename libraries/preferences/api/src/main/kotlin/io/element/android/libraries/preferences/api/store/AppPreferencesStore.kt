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

/**
 * Local, app-wide user preferences that are not tied to a session, stored on the device only.
 *
 * Each getter returns a flow that emits the current value straight away and then again on every change.
 * The default each preference falls back to when it has never been set is given on the getter.
 */
interface AppPreferencesStore {
    /**
     * @param enabled true to expose the developer options in the settings.
     */
    suspend fun setDeveloperModeEnabled(enabled: Boolean)

    /** Whether developer mode is on; defaults to `true` on debug builds and `false` otherwise. */
    fun isDeveloperModeEnabledFlow(): Flow<Boolean>

    /**
     * @param string the Element Call deployment to use, or `null` to go back to the one from the homeserver.
     */
    suspend fun setCustomElementCallBaseUrl(string: String?)

    /** The Element Call URL the user has overridden, or `null` when the homeserver's own value should be used. */
    fun getCustomElementCallBaseUrlFlow(): Flow<String?>

    /**
     * @param theme the name of the theme to apply: light, dark, or follow the system.
     */
    suspend fun setTheme(theme: String)

    /** The name of the chosen theme, or `null` when the user has not picked one and the system setting should be followed. */
    fun getThemeFlow(): Flow<String?>

    /**
     * @param value the distance in metres the user must move before a new live location is published.
     */
    suspend fun setLiveLocationMinimumDistanceInMetersUpdate(value: Int)

    /** The minimum distance in metres between two live location updates; defaults to 10. */
    fun getLiveLocationMinimumDistanceInMetersUpdateFlow(): Flow<Int>

    /**
     * Only used to clear the local value once it has been migrated to the server.
     *
     * @param hide the local setting, or `null` to erase it.
     */
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    suspend fun setHideInviteAvatars(hide: Boolean?)

    /** The local setting to migrate to the server, or `null` once there is nothing left to migrate. */
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    fun getHideInviteAvatarsFlow(): Flow<Boolean?>

    /**
     * Only used to clear the local value once it has been migrated to the server.
     *
     * @param mediaPreviewValue the local setting, or `null` to erase it.
     */
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    suspend fun setTimelineMediaPreviewValue(mediaPreviewValue: MediaPreviewValue?)

    /** The local setting to migrate to the server, or `null` once there is nothing left to migrate. */
    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    fun getTimelineMediaPreviewValueFlow(): Flow<MediaPreviewValue?>

    /**
     * @param logLevel how verbose the SDK and app logs should be.
     */
    suspend fun setTracingLogLevel(logLevel: LogLevel)

    /** The configured log level; defaults to the one the build was compiled with. */
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

    /**
     * @param targets the extra log packs to enable on top of the default targets.
     */
    suspend fun setTracingLogPacks(targets: Set<TraceLogPack>)

    /** The extra log packs enabled by the user; defaults to an empty set. */
    fun getTracingLogPacksFlow(): Flow<Set<TraceLogPack>>

    /** The sound played for message notifications. */
    fun getMessageSoundFlow(): Flow<NotificationSound>

    /**
     * Atomically persists [sound] (with copy-time [title] for Custom; cleared otherwise) and
     * bumps the channel version. Single transaction so process death can't desync URI and version.
     *
     * @param sound the sound to persist.
     * @param title the display name captured when the sound was copied, or `null` for the built-in choices.
     * @return the new channel version, to be passed to the channel recreation.
     */
    suspend fun setMessageSoundAndIncrementVersion(sound: NotificationSound, title: String?): Int

    /** Title captured at copy time. Null for SystemDefault / Silent or pre-title persisted data. */
    fun getMessageSoundDisplayNameFlow(): Flow<String?>

    /** The sound played for incoming call notifications. */
    fun getCallRingtoneFlow(): Flow<NotificationSound>

    /**
     * See [setMessageSoundAndIncrementVersion].
     *
     * @param sound the ringtone to persist.
     * @param title the display name captured when the sound was copied, or `null` for the built-in choices.
     * @return the new channel version, to be passed to the channel recreation.
     */
    suspend fun setCallRingtoneAndIncrementVersion(sound: NotificationSound, title: String?): Int

    /** See [getMessageSoundDisplayNameFlow]. */
    fun getCallRingtoneDisplayNameFlow(): Flow<String?>

    /** Single-snapshot read of all sound prefs; used at boot to seed channels without N reads. */
    suspend fun getNotificationSoundChannelConfig(): NotificationSoundChannelConfig

    /** Erases every app preference, so they all fall back to their defaults. */
    suspend fun reset()
}
