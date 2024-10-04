/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "elementx_preferences")

private val developerModeKey = booleanPreferencesKey("developerMode")
private val customElementCallBaseUrlKey = stringPreferencesKey("elementCallBaseUrl")
private val themeKey = stringPreferencesKey("theme")
private val simplifiedSlidingSyncKey = booleanPreferencesKey("useSimplifiedSlidingSync")
private val hideImagesAndVideosKey = booleanPreferencesKey("hideImagesAndVideos")

@ContributesBinding(AppScope::class)
class DefaultAppPreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
    private val buildMeta: BuildMeta,
) : AppPreferencesStore {
    private val store = context.dataStore

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

    override suspend fun setSimplifiedSlidingSyncEnabled(enabled: Boolean) {
        store.edit { prefs ->
            prefs[simplifiedSlidingSyncKey] = enabled
        }
    }

    override fun isSimplifiedSlidingSyncEnabledFlow(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[simplifiedSlidingSyncKey] ?: true
        }
    }

    override suspend fun setHideImagesAndVideos(value: Boolean) {
        store.edit { prefs ->
            prefs[hideImagesAndVideosKey] = value
        }
    }

    override fun doesHideImagesAndVideosFlow(): Flow<Boolean> {
        return store.data.map { prefs ->
            prefs[hideImagesAndVideosKey] ?: false
        }
    }

    override suspend fun reset() {
        store.edit { it.clear() }
    }
}
