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
