/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableMap
import javax.inject.Inject

class ConfigureTracingPresenter @Inject constructor(
    private val tracingConfigurationStore: TracingConfigurationStore,
    private val targetLogLevelMapBuilder: TargetLogLevelMapBuilder,
) : Presenter<ConfigureTracingState> {
    @Composable
    override fun present(): ConfigureTracingState {
        val modifiedMap = remember { mutableStateOf(targetLogLevelMapBuilder.getCurrentMap()) }

        fun handleEvents(event: ConfigureTracingEvents) {
            when (event) {
                is ConfigureTracingEvents.UpdateFilter -> {
                    modifiedMap.value = modifiedMap.value.toMutableMap()
                        .apply { this[event.target] = event.logLevel }
                    tracingConfigurationStore.storeLogLevel(event.target, event.logLevel)
                }
                ConfigureTracingEvents.ResetFilters -> {
                    modifiedMap.value = targetLogLevelMapBuilder.getDefaultMap()
                    tracingConfigurationStore.reset()
                }
            }
        }

        return ConfigureTracingState(
            targetsToLogLevel = modifiedMap.value.toImmutableMap(),
            eventSink = ::handleEvents
        )
    }
}
