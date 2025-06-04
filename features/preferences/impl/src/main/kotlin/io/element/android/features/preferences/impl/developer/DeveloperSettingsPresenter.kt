/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import io.element.android.features.preferences.impl.developer.tracing.toLogLevel
import io.element.android.features.preferences.impl.developer.tracing.toLogLevelItem
import io.element.android.features.preferences.impl.tasks.ClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.ComputeCacheSizeUseCase
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.matrix.api.tracing.TraceLogPack
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

class DeveloperSettingsPresenter @Inject constructor(
    private val featureFlagService: FeatureFlagService,
    private val computeCacheSizeUseCase: ComputeCacheSizeUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val rageshakePresenter: Presenter<RageshakePreferencesState>,
    private val appPreferencesStore: AppPreferencesStore,
    private val buildMeta: BuildMeta,
) : Presenter<DeveloperSettingsState> {
    @Composable
    override fun present(): DeveloperSettingsState {
        val rageshakeState = rageshakePresenter.present()

        val features = remember {
            mutableStateMapOf<String, Feature>()
        }
        val enabledFeatures = remember {
            mutableStateMapOf<String, Boolean>()
        }
        val cacheSize = remember {
            mutableStateOf<AsyncData<String>>(AsyncData.Uninitialized)
        }
        val clearCacheAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val customElementCallBaseUrl by remember {
            appPreferencesStore
                .getCustomElementCallBaseUrlFlow()
        }.collectAsState(initial = null)

        val tracingLogLevelFlow = remember {
            appPreferencesStore.getTracingLogLevelFlow().map { AsyncData.Success(it.toLogLevelItem()) }
        }
        val tracingLogLevel by tracingLogLevelFlow.collectAsState(initial = AsyncData.Uninitialized)
        val tracingLogPacks by produceState(persistentListOf<TraceLogPack>()) {
            appPreferencesStore.getTracingLogPacksFlow()
                // Sort the entries alphabetically by its title
                .map { it.sortedBy { it.title }.toPersistentList() }
                .collectLatest { value = it }
        }

        LaunchedEffect(Unit) {
            FeatureFlags.entries
                .filter { it.isFinished.not() }
                .run {
                    // Never display room directory search in release builds for Play Store
                    if (buildMeta.flavorDescription == "GooglePlay" && buildMeta.buildType == BuildType.RELEASE) {
                        filterNot { it.key == FeatureFlags.RoomDirectorySearch.key }
                    } else {
                        this
                    }
                }
                .forEach { feature ->
                    features[feature.key] = feature
                    enabledFeatures[feature.key] = featureFlagService.isFeatureEnabled(feature)
                }
        }
        val featureUiModels = createUiModels(features, enabledFeatures)
        val coroutineScope = rememberCoroutineScope()
        // Compute cache size each time the clear cache action value is changed
        LaunchedEffect(clearCacheAction.value.isSuccess()) {
            computeCacheSize(cacheSize)
        }

        fun handleEvents(event: DeveloperSettingsEvents) {
            when (event) {
                is DeveloperSettingsEvents.UpdateEnabledFeature -> coroutineScope.updateEnabledFeature(
                    features,
                    enabledFeatures,
                    event.feature,
                    event.isEnabled,
                    triggerClearCache = { handleEvents(DeveloperSettingsEvents.ClearCache) }
                )
                is DeveloperSettingsEvents.SetCustomElementCallBaseUrl -> coroutineScope.launch {
                    val urlToSave = event.baseUrl.takeIf { !it.isNullOrEmpty() }
                    appPreferencesStore.setCustomElementCallBaseUrl(urlToSave)
                }
                DeveloperSettingsEvents.ClearCache -> coroutineScope.clearCache(clearCacheAction)
                is DeveloperSettingsEvents.SetTracingLogLevel -> coroutineScope.launch {
                    appPreferencesStore.setTracingLogLevel(event.logLevel.toLogLevel())
                }
                is DeveloperSettingsEvents.ToggleTracingLogPack -> coroutineScope.launch {
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

        return DeveloperSettingsState(
            features = featureUiModels.toImmutableList(),
            cacheSize = cacheSize.value,
            clearCacheAction = clearCacheAction.value,
            rageshakeState = rageshakeState,
            customElementCallBaseUrlState = CustomElementCallBaseUrlState(
                baseUrl = customElementCallBaseUrl,
                validator = ::customElementCallUrlValidator,
            ),
            tracingLogLevel = tracingLogLevel,
            tracingLogPacks = tracingLogPacks,
            eventSink = ::handleEvents
        )
    }

    @Composable
    private fun createUiModels(
        features: SnapshotStateMap<String, Feature>,
        enabledFeatures: SnapshotStateMap<String, Boolean>
    ): List<FeatureUiModel> {
        return features.values.map { feature ->
            key(feature.key) {
                val isEnabled = enabledFeatures[feature.key].orFalse()
                remember(feature, isEnabled) {
                    FeatureUiModel(
                        key = feature.key,
                        title = feature.title,
                        description = feature.description,
                        isEnabled = isEnabled
                    )
                }
            }
        }
    }

    private fun CoroutineScope.updateEnabledFeature(
        features: SnapshotStateMap<String, Feature>,
        enabledFeatures: SnapshotStateMap<String, Boolean>,
        featureUiModel: FeatureUiModel,
        enabled: Boolean,
        @Suppress("UNUSED_PARAMETER") triggerClearCache: () -> Unit,
    ) = launch {
        val feature = features[featureUiModel.key] ?: return@launch
        if (featureFlagService.setFeatureEnabled(feature, enabled)) {
            enabledFeatures[featureUiModel.key] = enabled
        }
    }

    private fun CoroutineScope.computeCacheSize(cacheSize: MutableState<AsyncData<String>>) = launch {
        suspend {
            computeCacheSizeUseCase()
        }.runCatchingUpdatingState(cacheSize)
    }

    private fun CoroutineScope.clearCache(clearCacheAction: MutableState<AsyncAction<Unit>>) = launch {
        suspend {
            clearCacheUseCase()
        }.runCatchingUpdatingState(clearCacheAction)
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
