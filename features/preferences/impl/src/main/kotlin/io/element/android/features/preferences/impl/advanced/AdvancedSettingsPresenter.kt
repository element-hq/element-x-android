/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.mapToTheme
import io.element.android.features.preferences.api.store.AppPreferencesStore
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.push.api.PushService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

class AdvancedSettingsPresenter @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val matrixClient: MatrixClient,
    private val pushService: PushService,
) : Presenter<AdvancedSettingsState> {
    @Composable
    override fun present(): AdvancedSettingsState {
        val localCoroutineScope = rememberCoroutineScope()
        val isDeveloperModeEnabled by appPreferencesStore
            .isDeveloperModeEnabledFlow()
            .collectAsState(initial = false)
        val isSharePresenceEnabled by sessionPreferencesStore
            .isSharePresenceEnabled()
            .collectAsState(initial = true)
        val theme by remember {
            appPreferencesStore.getThemeFlow().mapToTheme()
        }
            .collectAsState(initial = Theme.System)
        var showChangeThemeDialog by remember { mutableStateOf(false) }

        var currentPushProvider by remember { mutableStateOf<String?>(null) }
        var distributors by remember { mutableStateOf<List<String>>(emptyList()) }
        var refreshPushProvider by remember { mutableIntStateOf(0) }

        LaunchedEffect(refreshPushProvider) {
            val p = pushService.getCurrentPushProvider()
            currentPushProvider = p?.getCurrentDistributor(matrixClient)?.name
            distributors = pushService.getAvailablePushProviders()
                .flatMap { pushProvider ->
                    pushProvider.getDistributors().map { it.name }
                }
        }

        var showChangePushProviderDialog by remember { mutableStateOf(false) }
        fun handleEvents(event: AdvancedSettingsEvents) {
            when (event) {
                is AdvancedSettingsEvents.SetDeveloperModeEnabled -> localCoroutineScope.launch {
                    appPreferencesStore.setDeveloperModeEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetSharePresenceEnabled -> localCoroutineScope.launch {
                    sessionPreferencesStore.setSharePresence(event.enabled)
                }
                AdvancedSettingsEvents.CancelChangeTheme -> showChangeThemeDialog = false
                AdvancedSettingsEvents.ChangeTheme -> showChangeThemeDialog = true
                is AdvancedSettingsEvents.SetTheme -> localCoroutineScope.launch {
                    appPreferencesStore.setTheme(event.theme.name)
                    showChangeThemeDialog = false
                }
                AdvancedSettingsEvents.ChangePushProvider -> showChangePushProviderDialog = true
                AdvancedSettingsEvents.CancelChangePushProvider -> showChangePushProviderDialog = false
                is AdvancedSettingsEvents.SetPushProvider -> {
                    localCoroutineScope.launch {
                        // Retrieve the push provider
                        // TODO rework this
                        val pushProvider = pushService.getAvailablePushProviders().firstOrNull { pushProvider ->
                            pushProvider.getDistributors().any { it.name == event.distributorName }
                        } ?: return@launch
                        val distributor = pushProvider.getDistributors().firstOrNull { it.name == event.distributorName } ?: return@launch
                        pushService.registerWith(
                            matrixClient,
                            pushProvider = pushProvider,
                            distributor = distributor
                        )
                        showChangePushProviderDialog = false
                        refreshPushProvider++
                    }
                }
            }
        }

        return AdvancedSettingsState(
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            isSharePresenceEnabled = isSharePresenceEnabled,
            theme = theme,
            showChangeThemeDialog = showChangeThemeDialog,
            pushDistributor = currentPushProvider ?: "",
            pushDistributors = distributors.toImmutableList(),
            showChangePushProviderDialog = showChangePushProviderDialog,
            eventSink = { handleEvents(it) }
        )
    }
}
