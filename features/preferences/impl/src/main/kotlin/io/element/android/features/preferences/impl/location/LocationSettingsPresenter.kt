/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class LocationSettingsPresenter(
    private val appPreferencesStore: AppPreferencesStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<LocationSettingsState> {
    @Composable
    override fun present(): LocationSettingsState {
        val liveLocationMinimumDistanceUpdate by produceState(10) {
            appPreferencesStore.getLiveLocationMinimumDistanceInMetersUpdateFlow().collect { value = it }
        }

        fun handleEvent(event: LocationSettingsEvent) {
            when (event) {
                is LocationSettingsEvent.SetLiveLocationMinimumDistanceUpdate -> sessionCoroutineScope.launch {
                    appPreferencesStore.setLiveLocationMinimumDistanceInMetersUpdate(event.value)
                }
            }
        }

        return LocationSettingsState(
            liveLocationMinimumDistanceUpdate = liveLocationMinimumDistanceUpdate,
            eventSink = ::handleEvent,
        )
    }
}
