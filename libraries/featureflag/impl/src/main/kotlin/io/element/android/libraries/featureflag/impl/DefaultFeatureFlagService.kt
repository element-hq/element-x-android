/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultFeatureFlagService(
    private val providers: Set<@JvmSuppressWildcards FeatureFlagProvider>,
    private val buildMeta: BuildMeta,
    private val featuresProvider: FeaturesProvider,
) : FeatureFlagService {
    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return providers.filter { it.hasFeature(feature) }
            .maxByOrNull(FeatureFlagProvider::priority)
            ?.isFeatureEnabledFlow(feature)
            ?: flowOf(feature.defaultValue(buildMeta))
    }

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean {
        return providers.filterIsInstance<MutableFeatureFlagProvider>()
            .maxByOrNull(FeatureFlagProvider::priority)
            ?.setFeatureEnabled(feature, enabled)
            ?.let { true }
            ?: false
    }

    override fun getAvailableFeatures(
        includeFinishedFeatures: Boolean,
        isInLabs: Boolean,
    ): List<Feature> {
        return featuresProvider.provide().filter { flag ->
            (includeFinishedFeatures || !flag.isFinished) &&
                flag.isInLabs == isInLabs
        }
    }
}
