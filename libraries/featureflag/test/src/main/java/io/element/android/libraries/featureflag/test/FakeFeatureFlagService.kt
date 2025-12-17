/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.test

import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFeatureFlagService(
    initialState: Map<String, Boolean> = emptyMap(),
    private val buildMeta: BuildMeta = aBuildMeta(),
    private val getAvailableFeaturesResult: (Boolean, Boolean) -> List<Feature> = { _, _ -> emptyList() },
) : FeatureFlagService {
    private val enabledFeatures = initialState
        .mapValues { MutableStateFlow(it.value) }
        .toMutableMap()

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean {
        val flow = enabledFeatures.getOrPut(feature.key) { MutableStateFlow(enabled) }
        flow.emit(enabled)
        return true
    }

    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return enabledFeatures.getOrPut(feature.key) { MutableStateFlow(feature.defaultValue(buildMeta)) }
    }

    override fun getAvailableFeatures(
        includeFinishedFeatures: Boolean,
        isInLabs: Boolean,
    ): List<Feature> {
        return getAvailableFeaturesResult(includeFinishedFeatures, isInLabs)
    }
}
