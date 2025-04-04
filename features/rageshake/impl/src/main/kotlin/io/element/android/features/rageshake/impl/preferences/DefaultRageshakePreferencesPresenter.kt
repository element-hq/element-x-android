/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.rageshake.api.RageshakeFeatureAvailability
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesEvents
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesPresenter
import io.element.android.features.rageshake.api.preferences.RageshakePreferencesState
import io.element.android.features.rageshake.impl.rageshake.RageShake
import io.element.android.features.rageshake.impl.rageshake.RageshakeDataStore
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultRageshakePreferencesPresenter @Inject constructor(
    private val rageshake: RageShake,
    private val rageshakeDataStore: RageshakeDataStore,
    private val rageshakeFeatureAvailability: RageshakeFeatureAvailability,
) : RageshakePreferencesPresenter {
    @Composable
    override fun present(): RageshakePreferencesState {
        val localCoroutineScope = rememberCoroutineScope()
        val isSupported: MutableState<Boolean> = rememberSaveable {
            mutableStateOf(rageshake.isAvailable())
        }
        val isFeatureAvailable = remember { rageshakeFeatureAvailability.isAvailable() }
        val isEnabled by remember {
            rageshakeDataStore.isEnabled()
        }.collectAsState(initial = false)

        val sensitivity by remember {
            rageshakeDataStore.sensitivity()
        }.collectAsState(initial = 0f)

        fun handleEvents(event: RageshakePreferencesEvents) {
            when (event) {
                is RageshakePreferencesEvents.SetIsEnabled -> localCoroutineScope.setIsEnabled(event.isEnabled)
                is RageshakePreferencesEvents.SetSensitivity -> localCoroutineScope.setSensitivity(event.sensitivity)
            }
        }

        return RageshakePreferencesState(
            isFeatureEnabled = isFeatureAvailable,
            isEnabled = isEnabled,
            isSupported = isSupported.value,
            sensitivity = sensitivity,
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
