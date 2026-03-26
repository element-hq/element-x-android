/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.test

import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.TimelineLayoutMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemoryAppPreferencesStore(
    isDeveloperModeEnabled: Boolean = false,
    customElementCallBaseUrl: String? = null,
    hideInviteAvatars: Boolean? = null,
    timelineMediaPreviewValue: MediaPreviewValue? = null,
    theme: String? = null,
    isDynamicColorEnabled: Boolean = true,
    isHighContrastEnabled: Boolean = false,
    logLevel: LogLevel = LogLevel.INFO,
    traceLockPacks: Set<TraceLogPack> = emptySet(),
    timelineLayoutMode: TimelineLayoutMode = TimelineLayoutMode.Bubble,
    dndUntilTimestamp: Long = 0L,
) : AppPreferencesStore {
    private val isDeveloperModeEnabled = MutableStateFlow(isDeveloperModeEnabled)
    private val customElementCallBaseUrl = MutableStateFlow(customElementCallBaseUrl)
    private val theme = MutableStateFlow(theme)
    private val isDynamicColorEnabled = MutableStateFlow(isDynamicColorEnabled)
    private val isHighContrastEnabled = MutableStateFlow(isHighContrastEnabled)
    private val logLevel = MutableStateFlow(logLevel)
    private val tracingLogPacks = MutableStateFlow(traceLockPacks)
    private val hideInviteAvatars = MutableStateFlow(hideInviteAvatars)
    private val timelineMediaPreviewValue = MutableStateFlow(timelineMediaPreviewValue)
    private val timelineLayoutMode = MutableStateFlow(timelineLayoutMode)
    private val dndUntilTimestamp = MutableStateFlow(dndUntilTimestamp)

    override suspend fun setTimelineLayoutMode(mode: TimelineLayoutMode) {
        timelineLayoutMode.value = mode
    }

    override fun getTimelineLayoutModeFlow(): Flow<TimelineLayoutMode> {
        return timelineLayoutMode
    }

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

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        isDynamicColorEnabled.value = enabled
    }

    override fun isDynamicColorEnabledFlow(): Flow<Boolean> {
        return isDynamicColorEnabled
    }

    override suspend fun setHighContrastEnabled(enabled: Boolean) {
        isHighContrastEnabled.value = enabled
    }

    override fun isHighContrastEnabledFlow(): Flow<Boolean> {
        return isHighContrastEnabled
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override fun getHideInviteAvatarsFlow(): Flow<Boolean?> {
        return hideInviteAvatars
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override fun getTimelineMediaPreviewValueFlow(): Flow<MediaPreviewValue?> {
        return timelineMediaPreviewValue
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override suspend fun setHideInviteAvatars(hide: Boolean?) {
        hideInviteAvatars.value = hide
    }

    @Deprecated("Use MediaPreviewService instead. Kept only for migration.")
    override suspend fun setTimelineMediaPreviewValue(mediaPreviewValue: MediaPreviewValue?) {
        timelineMediaPreviewValue.value = mediaPreviewValue
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

    override fun getDndUntilTimestamp(): Flow<Long> {
        return dndUntilTimestamp
    }

    override suspend fun setDndUntilTimestamp(timestamp: Long) {
        dndUntilTimestamp.value = timestamp
    }

    override suspend fun reset() {
        // No op
    }
}
