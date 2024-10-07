/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.featureflag.impl

import io.element.android.libraries.featureflag.api.Feature

interface MutableFeatureFlagProvider : FeatureFlagProvider {
    suspend fun setFeatureEnabled(feature: Feature, enabled: Boolean)
}
