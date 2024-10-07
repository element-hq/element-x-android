/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.preferences.api.store

import kotlinx.coroutines.flow.Flow

interface AppPreferencesStore {
    suspend fun setDeveloperModeEnabled(enabled: Boolean)
    fun isDeveloperModeEnabledFlow(): Flow<Boolean>

    suspend fun setCustomElementCallBaseUrl(string: String?)
    fun getCustomElementCallBaseUrlFlow(): Flow<String?>

    suspend fun setTheme(theme: String)
    fun getThemeFlow(): Flow<String?>

    suspend fun setSimplifiedSlidingSyncEnabled(enabled: Boolean)
    fun isSimplifiedSlidingSyncEnabledFlow(): Flow<Boolean>

    suspend fun setHideImagesAndVideos(value: Boolean)
    fun doesHideImagesAndVideosFlow(): Flow<Boolean>

    suspend fun reset()
}
