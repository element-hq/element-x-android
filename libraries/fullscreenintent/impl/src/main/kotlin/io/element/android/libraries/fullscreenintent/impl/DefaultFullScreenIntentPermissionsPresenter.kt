/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.fullscreenintent.impl

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsPresenter
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.noop.NoopPermissionsPresenter
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultFullScreenIntentPermissionsPresenter @Inject constructor(
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
    private val externalIntentLauncher: ExternalIntentLauncher,
    private val buildMeta: BuildMeta,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    preferencesDataStoreFactory: PreferenceDataStoreFactory,
) : FullScreenIntentPermissionsPresenter {
    companion object {
        private const val PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED = "PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED"
    }
    private val permissionsPresenter = if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)) {
        permissionsPresenterFactory.create(permission = Manifest.permission.USE_FULL_SCREEN_INTENT)
    } else {
        NoopPermissionsPresenter(isGranted = true)
    }

    private val dataStore = preferencesDataStoreFactory.create("full_screen_intent_permissions")

    private val isFullScreenIntentBannerDismissed = dataStore.data.map { prefs ->
        prefs[booleanPreferencesKey(PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED)] ?: false
    }

    private suspend fun dismissFullScreenIntentBanner() {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED)] = true
        }
    }

    @Composable
    override fun present(): FullScreenIntentPermissionsState {
        val coroutineScope = rememberCoroutineScope()
        val isGranted = permissionsPresenter.present().permissionGranted
        val isBannerDismissed by isFullScreenIntentBannerDismissed.collectAsState(initial = true)
        return FullScreenIntentPermissionsState(
            permissionGranted = isGranted,
            shouldDisplayBanner = !isBannerDismissed && !isGranted,
            dismissFullScreenIntentBanner = {
                coroutineScope.launch {
                    dismissFullScreenIntentBanner()
                }
            },
            openFullScreenIntentSettings = ::openFullScreenIntentSettings,
        )
    }

    private fun openFullScreenIntentSettings() {
        if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:${buildMeta.applicationId}")
                )
                externalIntentLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, buildMeta.applicationId)
                externalIntentLauncher.launch(intent)
            }
        }
    }
}
