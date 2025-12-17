/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.battery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.push.api.battery.BatteryOptimizationEvents
import io.element.android.libraries.push.api.battery.BatteryOptimizationState
import io.element.android.libraries.push.impl.push.MutableBatteryOptimizationStore
import io.element.android.libraries.push.impl.store.PushDataStore
import kotlinx.coroutines.launch

@Inject
class BatteryOptimizationPresenter(
    private val pushDataStore: PushDataStore,
    private val mutableBatteryOptimizationStore: MutableBatteryOptimizationStore,
    private val batteryOptimization: BatteryOptimization,
) : Presenter<BatteryOptimizationState> {
    @Composable
    override fun present(): BatteryOptimizationState {
        val coroutineScope = rememberCoroutineScope()
        var isRequestSent by remember { mutableStateOf(false) }
        var localShouldDisplayBanner by remember { mutableStateOf(true) }
        val storeShouldDisplayBanner by pushDataStore.shouldDisplayBatteryOptimizationBannerFlow.collectAsState(initial = false)
        var isSystemIgnoringBatteryOptimizations by remember {
            mutableStateOf(batteryOptimization.isIgnoringBatteryOptimizations())
        }

        LifecycleResumeEffect(Unit) {
            isSystemIgnoringBatteryOptimizations = batteryOptimization.isIgnoringBatteryOptimizations()
            if (isRequestSent) {
                localShouldDisplayBanner = false
            }
            onPauseOrDispose {}
        }

        fun handleEvent(event: BatteryOptimizationEvents) {
            when (event) {
                BatteryOptimizationEvents.Dismiss -> coroutineScope.launch {
                    mutableBatteryOptimizationStore.onOptimizationBannerDismissed()
                }
                BatteryOptimizationEvents.RequestDisableOptimizations -> {
                    isRequestSent = true
                    if (batteryOptimization.requestDisablingBatteryOptimization().not()) {
                        // If not able to perform the request, ensure that we do not display the banner again
                        coroutineScope.launch {
                            mutableBatteryOptimizationStore.onOptimizationBannerDismissed()
                        }
                    }
                }
            }
        }

        return BatteryOptimizationState(
            shouldDisplayBanner = localShouldDisplayBanner && storeShouldDisplayBanner && !isSystemIgnoringBatteryOptimizations,
            eventSink = ::handleEvent,
        )
    }
}
