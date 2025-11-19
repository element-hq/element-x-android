/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.toArgb
import dev.zacsweers.metro.Inject
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.preferences.impl.developer.tracing.toLogLevel
import io.element.android.features.preferences.impl.developer.tracing.toLogLevelItem
import io.element.android.features.preferences.impl.model.EnabledFeature
import io.element.android.features.preferences.impl.tasks.ClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.ComputeCacheSizeUseCase
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.matrix.api.core.SessionId
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
class DeveloperSettingsPresenter(
    private val sessionId: SessionId,
    private val featureFlagService: FeatureFlagService,
    private val computeCacheSizeUseCase: ComputeCacheSizeUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val rageshakePresenter: Presenter<RageshakePreferencesState>,
    private val appPreferencesStore: AppPreferencesStore,
    private val buildMeta: BuildMeta,
    private val enterpriseService: EnterpriseService,
) : Presenter<DeveloperSettingsState> {
    @Composable
    override fun present(): DeveloperSettingsState {
        val rageshakeState = rageshakePresenter.present()
        val enabledFeatures = remember {
            mutableStateListOf<EnabledFeature>()
        }
        val cacheSize = remember {
            mutableStateOf<AsyncData<String>>(AsyncData.Uninitialized)
        }
        val clearCacheAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        var showColorPicker by remember {
            mutableStateOf(false)
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
        LaunchedEffect(clearCacheAction.value.isSuccess()) {
            computeCacheSize(cacheSize)
        }

        fun handleEvent(event: DeveloperSettingsEvents) {
            when (event) {
                is DeveloperSettingsEvents.UpdateEnabledFeature -> coroutineScope.updateEnabledFeature(
                    enabledFeatures = enabledFeatures,
                    featureKey = event.feature.key,
                    enabled = event.isEnabled,
                    triggerClearCache = { handleEvent(DeveloperSettingsEvents.ClearCache) }
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
                is DeveloperSettingsEvents.ChangeBrandColor -> coroutineScope.launch {
                    showColorPicker = false
                    val color = event.color
                        ?.toArgb()
                        ?.toHexString(HexFormat.UpperCase)
                        ?.substring(2, 8)
                        ?.padStart(7, '#')
                    enterpriseService.overrideBrandColor(sessionId, color)
                }
                is DeveloperSettingsEvents.SetShowColorPicker -> {
                    showColorPicker = event.show
                }
            }
        }

        return DeveloperSettingsState(
            features = featureUiModels,
            cacheSize = cacheSize.value,
            clearCacheAction = clearCacheAction.value,
            rageshakeState = rageshakeState,
            customElementCallBaseUrlState = CustomElementCallBaseUrlState(
                baseUrl = customElementCallBaseUrl,
                validator = ::customElementCallUrlValidator,
            ),
            tracingLogLevel = tracingLogLevel,
            tracingLogPacks = tracingLogPacks,
            isEnterpriseBuild = enterpriseService.isEnterpriseBuild,
            showColorPicker = showColorPicker,
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
        @Suppress("UNUSED_PARAMETER") triggerClearCache: () -> Unit,
    ) = launch {
        val featureIndex = enabledFeatures.indexOfFirst { it.feature.key == featureKey }.takeIf { it != -1 } ?: return@launch
        val feature = enabledFeatures[featureIndex].feature
        if (featureFlagService.setFeatureEnabled(feature, enabled)) {
            enabledFeatures[featureIndex] = enabledFeatures[featureIndex].copy(isEnabled = enabled)
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
