/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface FeatureFlagService {
    /**
     * @param feature the feature to check for
     *
     * @return true if the feature is enabled
     */
    suspend fun isFeatureEnabled(feature: Feature): Boolean = isFeatureEnabledFlow(feature).first()

    /**
     * @param feature the feature to check for
     *
     * @return a flow of booleans, true if the feature is enabled, false if it is disabled.
     */
    fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean>

    /**
     * @param feature the feature to enable or disable
     * @param enabled true to enable the feature
     *
     * @return true if the method succeeds, ie if a [io.element.android.libraries.featureflag.impl.MutableFeatureFlagProvider]
     * is registered
     */
    suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean): Boolean

    /**
     * @return the list of available features that can be toggled.
     * @param includeFinishedFeatures whether to include finished features, default is false
     * @param isInLabs whether the user is in labs (to include lab features), default is false
     */
    fun getAvailableFeatures(
        includeFinishedFeatures: Boolean = false,
        isInLabs: Boolean = false,
    ): List<Feature>
}
