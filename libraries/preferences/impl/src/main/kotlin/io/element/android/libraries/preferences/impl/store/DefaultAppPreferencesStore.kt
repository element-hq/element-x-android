/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val developerModeKey = booleanPreferencesKey("developerMode")
private val customElementCallBaseUrlKey = stringPreferencesKey("elementCallBaseUrl")
private val themeKey = stringPreferencesKey("theme")
private val hideInviteAvatarsKey = booleanPreferencesKey("hideInviteAvatars")
private val timelineMediaPreviewValueKey = stringPreferencesKey("timelineMediaPreviewValue")
private val logLevelKey = stringPreferencesKey("logLevel")
private val traceLogPacksKey = stringPreferencesKey("traceLogPacks")

@ContributesBinding(AppScope::class)
class DefaultAppPreferencesStore(
    private val buildMeta: BuildMeta,
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : AppPreferencesStore {
    private val store = preferenceDataStoreFactory.create("elementx_preferences")

    override suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        store.edit { prefs ->
            prefs[developerModeKey] = enabled
        }
    }

    override fun isDeveloperModeEnabledFlow(): Flow<Boolean> {
        return store.data.map { prefs ->
            // disabled by default on release and nightly, enabled by default on debug
            prefs[developerModeKey] ?: (buildMeta.buildType == BuildType.DEBUG)
        }
    }

    override suspend fun setCustomElementCallBaseUrl(string: String?) {
        store.edit { prefs ->
            if (string != null) {
                prefs[customElementCallBaseUrlKey] = string
            } else {
                prefs.remove(customElementCallBaseUrlKey)
            }
        }
    }

    override fun getCustomElementCallBaseUrlFlow(): Flow<String?> {
        return store.data.map { prefs ->
            prefs[customElementCallBaseUrlKey]
        }
    }

    override suspend fun setTheme(theme: String) {
        store.edit { prefs ->
            prefs[themeKey] = theme
        }
    }

    override fun getThemeFlow(): Flow<String?> {
        return store.data.map { prefs ->
            prefs[themeKey]
        }
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override fun getHideInviteAvatarsFlow(): Flow<Boolean?> {
        return store.data.map { prefs ->
            prefs[hideInviteAvatarsKey]
        }
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override suspend fun setHideInviteAvatars(hide: Boolean?) {
        store.edit { prefs ->
            if (hide != null) {
                prefs[hideInviteAvatarsKey] = hide
            } else {
                prefs.remove(hideInviteAvatarsKey)
            }
        }
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override suspend fun setTimelineMediaPreviewValue(mediaPreviewValue: MediaPreviewValue?) {
        store.edit { prefs ->
            if (mediaPreviewValue != null) {
                prefs[timelineMediaPreviewValueKey] = mediaPreviewValue.name
            } else {
                prefs.remove(timelineMediaPreviewValueKey)
            }
        }
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override fun getTimelineMediaPreviewValueFlow(): Flow<MediaPreviewValue?> {
        return store.data.map { prefs ->
            prefs[timelineMediaPreviewValueKey]?.let { MediaPreviewValue.valueOf(it) }
        }
    }

    override suspend fun setTracingLogLevel(logLevel: LogLevel) {
        store.edit { prefs ->
            prefs[logLevelKey] = logLevel.name
        }
    }

    override fun getTracingLogLevelFlow(): Flow<LogLevel> {
        return store.data.map { prefs ->
            prefs[logLevelKey]?.let { LogLevel.valueOf(it) } ?: buildMeta.defaultLogLevel()
        }
    }

    override suspend fun setTracingLogPacks(targets: Set<TraceLogPack>) {
        val value = targets.joinToString(",") { it.key }
        store.edit { prefs ->
            prefs[traceLogPacksKey] = value
        }
    }

    override fun getTracingLogPacksFlow(): Flow<Set<TraceLogPack>> {
        return store.data.map { prefs ->
            prefs[traceLogPacksKey]
                ?.split(",")
                ?.mapNotNull { value -> TraceLogPack.entries.find { it.key == value } }
                ?.toSet()
                ?: emptySet()
        }
    }

    override suspend fun reset() {
        store.edit { it.clear() }
    }
}

private fun BuildMeta.defaultLogLevel(): LogLevel {
    return when (buildType) {
        BuildType.DEBUG -> LogLevel.TRACE
        BuildType.NIGHTLY -> LogLevel.DEBUG
        BuildType.RELEASE -> LogLevel.INFO
    }
}
