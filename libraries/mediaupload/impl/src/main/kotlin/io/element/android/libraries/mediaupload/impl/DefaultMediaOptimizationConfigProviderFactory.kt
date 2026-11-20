/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProviderFactory
import io.element.android.libraries.preferences.api.store.SessionPreferencesStoreFactory

@ContributesBinding(AppScope::class)
class DefaultMediaOptimizationConfigProviderFactory(
    private val sessionPreferencesStoreFactory: SessionPreferencesStoreFactory,
    private val featureFlagService: FeatureFlagService,
) : MediaOptimizationConfigProviderFactory {
    override fun create(client: MatrixClient): MediaOptimizationConfigProvider {
        return DefaultMediaOptimizationConfigProvider(
            sessionPreferencesStore = sessionPreferencesStoreFactory.get(client.sessionId, client.sessionCoroutineScope),
            featureFlagsService = featureFlagService,
        )
    }
}
