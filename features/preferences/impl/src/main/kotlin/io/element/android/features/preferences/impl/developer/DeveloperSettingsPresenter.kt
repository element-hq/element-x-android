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

package io.element.android.features.preferences.impl.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import io.element.android.features.preferences.impl.tasks.ClearCacheUseCase
import io.element.android.features.preferences.impl.tasks.ComputeCacheSizeUseCase
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.execute
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeveloperSettingsPresenter @Inject constructor(
    private val featureFlagService: FeatureFlagService,
    private val computeCacheSizeUseCase: ComputeCacheSizeUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
) : Presenter<DeveloperSettingsState> {

    @Composable
    override fun present(): DeveloperSettingsState {

        val features = remember {
            mutableStateMapOf<String, Feature>()
        }
        val enabledFeatures = remember {
            mutableStateMapOf<String, Boolean>()
        }
        val cacheSize = remember {
            mutableStateOf<Async<String>>(Async.Uninitialized)
        }
        val clearCacheAction = remember {
            mutableStateOf<Async<Unit>>(Async.Uninitialized)
        }
        LaunchedEffect(Unit) {
            FeatureFlags.values().forEach { feature ->
                features[feature.key] = feature
                enabledFeatures[feature.key] = featureFlagService.isFeatureEnabled(feature)
            }
        }
        val featureUiModels = createUiModels(features, enabledFeatures)
        val coroutineScope = rememberCoroutineScope()
        // Compute cache size each time the clear cache action value is changed
        LaunchedEffect(clearCacheAction.value) {
            computeCacheSize(cacheSize)
        }

        fun handleEvents(event: DeveloperSettingsEvents) {
            when (event) {
                is DeveloperSettingsEvents.UpdateEnabledFeature -> coroutineScope.updateEnabledFeature(
                    features,
                    enabledFeatures,
                    event.feature,
                    event.isEnabled
                )
                DeveloperSettingsEvents.ClearCache -> coroutineScope.clearCache(clearCacheAction)
            }
        }

        return DeveloperSettingsState(
            features = featureUiModels.toImmutableList(),
            cacheSize = cacheSize.value,
            clearCacheAction = clearCacheAction.value,
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
        enabled: Boolean
    ) = launch {
        val feature = features[featureUiModel.key] ?: return@launch
        if (featureFlagService.setFeatureEnabled(feature, enabled)) {
            enabledFeatures[featureUiModel.key] = enabled
        }
    }

    private fun CoroutineScope.computeCacheSize(cacheSize: MutableState<Async<String>>) = launch {
        suspend {
            computeCacheSizeUseCase()
        }.execute(cacheSize)
    }

    private fun CoroutineScope.clearCache(clearCacheAction: MutableState<Async<Unit>>) = launch {
        suspend {
            clearCacheUseCase()
        }.execute(clearCacheAction)
    }
}



