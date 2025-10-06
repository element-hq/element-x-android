/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.battery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.push.api.battery.PushNotificationsWarningEvents
import io.element.android.libraries.push.api.battery.PushNotificationsWarningState
import io.element.android.libraries.push.impl.push.MutableBatteryOptimizationStore
import io.element.android.libraries.push.impl.store.PushDataStore
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.api.PushProvider
import kotlinx.coroutines.launch

@Inject
class PushNotificationsWarningPresenter(
    private val pushDataStore: PushDataStore,
    private val mutableBatteryOptimizationStore: MutableBatteryOptimizationStore,
    private val batteryOptimization: BatteryOptimization,
    private val pushProviders: Set<@JvmSuppressWildcards PushProvider>,
    private val getCurrentPushProvider: GetCurrentPushProvider,
) : Presenter<PushNotificationsWarningState> {
    @Composable
    override fun present(): PushNotificationsWarningState {
        val coroutineScope = rememberCoroutineScope()
        var isRequestSent by remember { mutableStateOf(false) }
        var localShouldDisplayBanner by remember { mutableStateOf(true) }
        val storeShouldDisplayBanner by pushDataStore.shouldDisplayBatteryOptimizationBannerFlow.collectAsState(initial = false)
        var isSystemIgnoringBatteryOptimizations by remember {
            mutableStateOf(batteryOptimization.isIgnoringBatteryOptimizations())
        }

        val currentUserPushConfig by produceState<AsyncData<CurrentUserPushConfig?>>(AsyncData.Loading(null)) {
            val currentPushProvider = pushProviders.first { it.name == getCurrentPushProvider.getCurrentPushProvider() }
            value = AsyncData.Success(currentPushProvider.getCurrentUserPushConfig())
        }

        LifecycleResumeEffect(Unit) {
            isSystemIgnoringBatteryOptimizations = batteryOptimization.isIgnoringBatteryOptimizations()
            if (isRequestSent) {
                localShouldDisplayBanner = false
            }
            onPauseOrDispose {}
        }

        fun handleEvent(event: PushNotificationsWarningEvents) {
            when (event) {
                PushNotificationsWarningEvents.Dismiss -> coroutineScope.launch {
                    mutableBatteryOptimizationStore.onOptimizationBannerDismissed()
                }
                PushNotificationsWarningEvents.RequestDisableOptimizations -> {
                    isRequestSent = true
                    if (batteryOptimization.requestDisablingBatteryOptimization().not()) {
                        // If not able to perform the request, ensure that we do not display the banner again
                        coroutineScope.launch {
                            mutableBatteryOptimizationStore.onOptimizationBannerDismissed()
                        }
                    }
                }
                PushNotificationsWarningEvents.OpenPushDistributorApp -> {
                    // TODO: actually open the app
                }
            }
        }

        return PushNotificationsWarningState(
            currentUserPushConfig = currentUserPushConfig,
            needsEnablingBatteryOptimization = localShouldDisplayBanner && storeShouldDisplayBanner && !isSystemIgnoringBatteryOptimizations,
            eventSink = ::handleEvent,
        )
    }
}
