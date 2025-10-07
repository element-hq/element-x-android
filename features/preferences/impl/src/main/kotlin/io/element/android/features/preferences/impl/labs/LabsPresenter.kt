/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.labs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.zacsweers.metro.Inject
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.features.preferences.impl.tasks.ClearCacheUseCase
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Inject
class LabsPresenter(
    private val stringProvider: StringProvider,
    private val featureFlagService: FeatureFlagService,
    private val clearCacheUseCase: ClearCacheUseCase,
) : Presenter<LabsState> {
    @Composable
    override fun present(): LabsState {
        val coroutineScope = rememberCoroutineScope()
        val features = remember {
            val entries = featureFlagService.getAvailableFeatures()
                .filter { it.isInLabs && !it.isFinished }
                .map { it.key to it }
            mutableStateMapOf(*entries.toTypedArray())
        }
        val enabledFeatures = remember {
            mutableStateMapOf<String, Boolean>()
        }

        LaunchedEffect(Unit) {
            for (feature in features.values) {
                val isEnabled = featureFlagService.isFeatureEnabled(feature)
                enabledFeatures[feature.key] = isEnabled
            }
        }

        var isApplyingChanges by remember { mutableStateOf(false) }

        val featureUiModels = createUiModels(features, enabledFeatures)

        fun handleEvent(event: LabsEvents) {
            when (event) {
                is LabsEvents.ToggleFeature -> coroutineScope.launch {
                    val feature = features[event.feature.key] ?: return@launch
                    val isEnabled = featureFlagService.isFeatureEnabled(feature)
                    featureFlagService.setFeatureEnabled(feature = feature, enabled = !isEnabled)
                    enabledFeatures[feature.key] = !isEnabled

                    when (feature.key) {
                        FeatureFlags.Threads.key -> {
                            // Threads require a cache clear to recreate the event cache
                            clearCacheUseCase()
                            isApplyingChanges = true
                        }
                    }
                }
            }
        }

        return LabsState(
            features = featureUiModels,
            isApplyingChanges = isApplyingChanges,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun createUiModels(
        features: SnapshotStateMap<String, Feature>,
        enabledFeatures: SnapshotStateMap<String, Boolean>
    ): ImmutableList<FeatureUiModel> {
        return features.values.map { feature ->
            key(feature.key) {
                val isEnabled = enabledFeatures[feature.key].orFalse()
                val title = when (feature) {
                    FeatureFlags.Threads -> stringProvider.getString(R.string.screen_labs_enable_threads)
                    else -> feature.title
                }
                val description = when (feature) {
                    FeatureFlags.Threads -> stringProvider.getString(R.string.screen_labs_enable_threads_description)
                    else -> feature.description
                }
                val icon = when (feature) {
                    FeatureFlags.Threads -> CompoundIcons.Threads()
                    else -> null
                }
                remember(feature, isEnabled) {
                    FeatureUiModel(
                        key = feature.key,
                        title = title,
                        description = description,
                        icon = icon?.let(IconSource::Vector),
                        isEnabled = isEnabled
                    )
                }
            }
        }.toImmutableList()
    }
}
