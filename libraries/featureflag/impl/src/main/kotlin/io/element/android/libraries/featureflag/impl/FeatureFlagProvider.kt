/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import io.element.android.libraries.featureflag.api.Feature
import kotlinx.coroutines.flow.Flow

interface FeatureFlagProvider {
    val priority: Int
    fun isFeatureEnabledFlow(feature: Feature): Flow<Boolean>
    fun hasFeature(feature: Feature): Boolean
}

const val LOW_PRIORITY = 0
const val MEDIUM_PRIORITY = 1
const val HIGH_PRIORITY = 2
