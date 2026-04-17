/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import dev.zacsweers.metro.Inject
import io.element.android.features.preferences.impl.developer.tracing.toLogLevel
import io.element.android.features.preferences.impl.developer.tracing.toLogLevelItem
import io.element.android.features.preferences.impl.model.EnabledFeature
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URL

@Inject
class AppDeveloperSettingsPresenter(
    private val featureFlagService: FeatureFlagService,
    private val rageshakePresenter: Presenter<RageshakePreferencesState>,
    private val appPreferencesStore: AppPreferencesStore,
    private val buildMeta: BuildMeta,
) : Presenter<AppDeveloperSettingsState> {
    @Composable
    override fun present(): AppDeveloperSettingsState {
        val rageshakeState = rageshakePresenter.present()
        val enabledFeatures = remember {
            mutableStateListOf<EnabledFeature>()
        }
        val customElementCallBaseUrl by remember {
            appPreferencesStore
                .getCustomElementCallBaseUrlFlow()
        }.collectAsState(initial = null)

        val tracingLogLevelFlow = remember {
            appPreferencesStore.getTracingLogLevelFlow().map { AsyncData.Success(it.toLogLevelItem()) }
        }
        val tracingLogLevel by tracingLogLevelFlow.collectAsState(initial = AsyncData.Uninitialized)
        val tracingLogPacks by produceState(persistentListOf()) {
            appPreferencesStore.getTracingLogPacksFlow()
                // Sort the entries alphabetically by its title
                .map { it.sortedBy { pack -> pack.title } }
                .collectLatest { value = it.toImmutableList() }
        }

        LaunchedEffect(Unit) {
            featureFlagService.getAvailableFeatures()
                .run {
                    // Never display room directory search in release builds for Play Store
                    if (buildMeta.flavorDescription == "GooglePlay" && buildMeta.buildType == BuildType.RELEASE) {
                        filterNot { it.key == FeatureFlags.RoomDirectorySearch.key }
                    } else {
                        this
                    }
                }
                .forEach { feature ->
                    enabledFeatures.add(EnabledFeature(feature, featureFlagService.isFeatureEnabled(feature)))
                }
        }
        val featureUiModels = createUiModels(enabledFeatures)
        val coroutineScope = rememberCoroutineScope()
        // Compute cache size each time the clear cache action value is changed

        fun handleEvent(event: AppDeveloperSettingsEvent) {
            when (event) {
                is AppDeveloperSettingsEvent.UpdateEnabledFeature -> coroutineScope.updateEnabledFeature(
                    enabledFeatures = enabledFeatures,
                    featureKey = event.feature.key,
                    enabled = event.isEnabled,
                )
                is AppDeveloperSettingsEvent.SetCustomElementCallBaseUrl -> coroutineScope.launch {
                    val urlToSave = event.baseUrl.takeIf { !it.isNullOrEmpty() }
                    appPreferencesStore.setCustomElementCallBaseUrl(urlToSave)
                }
                is AppDeveloperSettingsEvent.SetTracingLogLevel -> coroutineScope.launch {
                    appPreferencesStore.setTracingLogLevel(event.logLevel.toLogLevel())
                }
                is AppDeveloperSettingsEvent.ToggleTracingLogPack -> coroutineScope.launch {
                    val currentPacks = tracingLogPacks.toMutableSet()
                    if (currentPacks.contains(event.logPack)) {
                        currentPacks.remove(event.logPack)
                    } else {
                        currentPacks.add(event.logPack)
                    }
                    appPreferencesStore.setTracingLogPacks(currentPacks)
                }
            }
        }

        return AppDeveloperSettingsState(
            features = featureUiModels,
            rageshakeState = rageshakeState,
            customElementCallBaseUrlState = CustomElementCallBaseUrlState(
                baseUrl = customElementCallBaseUrl,
                validator = ::customElementCallUrlValidator,
            ),
            tracingLogLevel = tracingLogLevel,
            tracingLogPacks = tracingLogPacks,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun createUiModels(
        enabledFeatures: SnapshotStateList<EnabledFeature>,
    ): ImmutableList<FeatureUiModel> {
        return enabledFeatures.map { enabledFeature ->
            key(enabledFeature.feature.key) {
                remember(enabledFeature) {
                    FeatureUiModel(
                        key = enabledFeature.feature.key,
                        title = enabledFeature.feature.title,
                        description = enabledFeature.feature.description,
                        icon = null,
                        isEnabled = enabledFeature.isEnabled
                    )
                }
            }
        }.toImmutableList()
    }

    private fun CoroutineScope.updateEnabledFeature(
        enabledFeatures: SnapshotStateList<EnabledFeature>,
        featureKey: String,
        enabled: Boolean,
    ) = launch {
        val featureIndex = enabledFeatures.indexOfFirst { it.feature.key == featureKey }.takeIf { it != -1 } ?: return@launch
        val feature = enabledFeatures[featureIndex].feature
        if (featureFlagService.setFeatureEnabled(feature, enabled)) {
            enabledFeatures[featureIndex] = enabledFeatures[featureIndex].copy(isEnabled = enabled)
        }
    }
}

private fun customElementCallUrlValidator(url: String?): Boolean {
    return runCatchingExceptions {
        if (url.isNullOrEmpty()) return@runCatchingExceptions
        val parsedUrl = URL(url)
        if (parsedUrl.protocol !in listOf("http", "https")) error("Incorrect protocol")
        if (parsedUrl.host.isNullOrBlank()) error("Missing host")
    }.isSuccess
}
