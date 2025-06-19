/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.impl.store.DefaultPushDataStore
import javax.inject.Inject

interface MutableBatteryOptimizationStore {
    suspend fun showBatteryOptimizationBanner()
    suspend fun onOptimizationBannerDismissed()
    suspend fun reset()
}

@ContributesBinding(AppScope::class)
class DefaultMutableBatteryOptimizationStore @Inject constructor(
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
