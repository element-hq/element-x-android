/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.test

import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryAppPreferencesStore(
    isDeveloperModeEnabled: Boolean = false,
    hideImagesAndVideos: Boolean = false,
    customElementCallBaseUrl: String? = null,
    theme: String? = null,
    logLevel: LogLevel = LogLevel.INFO,
    traceLockPacks: Set<TraceLogPack> = emptySet(),
) : AppPreferencesStore {
    private val isDeveloperModeEnabled = MutableStateFlow(isDeveloperModeEnabled)
    private val hideImagesAndVideos = MutableStateFlow(hideImagesAndVideos)
    private val customElementCallBaseUrl = MutableStateFlow(customElementCallBaseUrl)
    private val theme = MutableStateFlow(theme)
    private val logLevel = MutableStateFlow(logLevel)
    private val tracingLogPacks = MutableStateFlow(traceLockPacks)

    override suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        isDeveloperModeEnabled.value = enabled
    }

    override fun isDeveloperModeEnabledFlow(): Flow<Boolean> {
        return isDeveloperModeEnabled
    }

    override suspend fun setCustomElementCallBaseUrl(string: String?) {
        customElementCallBaseUrl.tryEmit(string)
    }

    override fun getCustomElementCallBaseUrlFlow(): Flow<String?> {
        return customElementCallBaseUrl
    }

    override suspend fun setTheme(theme: String) {
        this.theme.value = theme
    }

    override fun getThemeFlow(): Flow<String?> {
        return theme
    }

    override suspend fun setHideImagesAndVideos(value: Boolean) {
        hideImagesAndVideos.value = value
    }

    override fun doesHideImagesAndVideosFlow(): Flow<Boolean> {
        return hideImagesAndVideos
    }

    override suspend fun setTracingLogLevel(logLevel: LogLevel) {
        this.logLevel.value = logLevel
    }

    override fun getTracingLogLevelFlow(): Flow<LogLevel> {
        return logLevel
    }

    override suspend fun setTracingLogPacks(targets: Set<TraceLogPack>) {
        tracingLogPacks.value = targets
    }

    override fun getTracingLogPacksFlow(): Flow<Set<TraceLogPack>> {
        return tracingLogPacks
    }

    override suspend fun reset() {
        // No op
    }
}
