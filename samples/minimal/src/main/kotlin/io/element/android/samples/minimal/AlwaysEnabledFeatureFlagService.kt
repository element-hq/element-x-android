/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.samples.minimal

import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AlwaysEnabledFeatureFlagService : FeatureFlagService {
    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return flowOf(true)
    }

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean {
        return true
    }
}
