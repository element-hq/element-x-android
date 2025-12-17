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

    suspend fun reset()
}
