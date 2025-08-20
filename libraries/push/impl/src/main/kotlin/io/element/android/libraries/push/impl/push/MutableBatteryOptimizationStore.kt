/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.push.impl.store.DefaultPushDataStore
import dev.zacsweers.metro.Inject

interface MutableBatteryOptimizationStore {
    suspend fun showBatteryOptimizationBanner()
    suspend fun onOptimizationBannerDismissed()
    suspend fun reset()
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultMutableBatteryOptimizationStore(
    private val defaultPushDataStore: DefaultPushDataStore,
) : MutableBatteryOptimizationStore {
    override suspend fun showBatteryOptimizationBanner() {
        defaultPushDataStore.setBatteryOptimizationBannerState(DefaultPushDataStore.BATTERY_OPTIMIZATION_BANNER_STATE_SHOW)
    }

    override suspend fun onOptimizationBannerDismissed() {
        defaultPushDataStore.setBatteryOptimizationBannerState(DefaultPushDataStore.BATTERY_OPTIMIZATION_BANNER_STATE_DISMISSED)
    }

    override suspend fun reset() {
        defaultPushDataStore.setBatteryOptimizationBannerState(DefaultPushDataStore.BATTERY_OPTIMIZATION_BANNER_STATE_INIT)
    }
}
