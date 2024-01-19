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

package io.element.android.features.rageshake.impl.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesEvents
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesPresenter
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.features.rageshake.api.rageshake.RageShake
import io.element.android.features.rageshake.api.rageshake.RageshakeDataStore
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRageshakePreferencesPresenter @Inject constructor(
    private val rageshake: RageShake,
    private val rageshakeDataStore: RageshakeDataStore,
) : RageshakePreferencesPresenter {
    @Composable
    override fun present(): RageshakePreferencesState {
        val localCoroutineScope = rememberCoroutineScope()
        val isSupported: MutableState<Boolean> = rememberSaveable {
            mutableStateOf(rageshake.isAvailable())
        }
        val isEnabled = rageshakeDataStore
            .isEnabled()
            .collectAsState(initial = false)

        val sensitivity = rageshakeDataStore
            .sensitivity()
            .collectAsState(initial = 0f)

        fun handleEvents(event: RageshakePreferencesEvents) {
            when (event) {
                is RageshakePreferencesEvents.SetIsEnabled -> localCoroutineScope.setIsEnabled(event.isEnabled)
                is RageshakePreferencesEvents.SetSensitivity -> localCoroutineScope.setSensitivity(event.sensitivity)
            }
        }

        return RageshakePreferencesState(
            isEnabled = isEnabled.value,
            isSupported = isSupported.value,
            sensitivity = sensitivity.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.setSensitivity(sensitivity: Float) = launch {
        rageshakeDataStore.setSensitivity(sensitivity)
    }

    private fun CoroutineScope.setIsEnabled(enabled: Boolean) = launch {
        rageshakeDataStore.setIsEnabled(enabled)
    }
}
