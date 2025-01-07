/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.Feature
import io.element.android.libraries.featureflag.api.FeatureFlagService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultFeatureFlagService @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards FeatureFlagProvider>,
    private val buildMeta: BuildMeta,
) : FeatureFlagService {
    override fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean> {
        return providers.filter { it.hasFeature(feature) }
            .sortedByDescending(FeatureFlagProvider::priority)
            .firstOrNull()
            ?.isFeatureEnabledFlow(feature)
            ?: flowOf(feature.defaultValue(buildMeta))
    }

    override suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean {
        return providers.filterIsInstance<MutableFeatureFlagProvider>()
            .sortedBy(FeatureFlagProvider::priority)
            .firstOrNull()
            ?.setFeatureEnabled(feature, enabled)
            ?.let { true }
            ?: false
    }
}
