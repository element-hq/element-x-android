/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.api

/**
 * Provides a list of available [Feature]s that the application supports.
 * Used by [FeatureFlagService.getAvailableFeatures] to obtain a list of features
 * that can be presented to the user.
 *
 * Multiple instances can exist within the application, and all instances are polled
 * to provide features for the list. This means that additional features can be provided
 * by compile-time extensions.
 */
fun interface FeaturesProvider {
    fun provide(): List<Feature>
}
