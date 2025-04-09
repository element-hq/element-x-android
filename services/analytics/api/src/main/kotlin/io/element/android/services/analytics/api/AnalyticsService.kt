/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.trackers.AnalyticsTracker
import io.element.android.services.analyticsproviders.api.trackers.ErrorTracker
import kotlinx.coroutines.flow.Flow

interface AnalyticsService : AnalyticsTracker, ErrorTracker {
    /**
     * Get the available analytics providers.
     */
    fun getAvailableAnalyticsProviders(): Set<AnalyticsProvider>

    /**
     * A Flow of Boolean, true if the user has given their consent.
     */
    val userConsentFlow: Flow<Boolean>

    /**
     * Update the user consent value.
     */
    suspend fun setUserConsent(userConsent: Boolean)

    /**
     * A Flow of Boolean, true if the user has been asked for their consent.
     */
    val didAskUserConsentFlow: Flow<Boolean>

    /**
     * Store the fact that the user has been asked for their consent.
     */
    suspend fun setDidAskUserConsent()

    /**
     * A Flow of String, used for analytics Id.
     */
    val analyticsIdFlow: Flow<String>

    /**
     * Update analyticsId from the AccountData.
     */
    suspend fun setAnalyticsId(analyticsId: String)

    /**
     * Reset the analytics service (will ask for user consent again).
     */
    suspend fun reset()
}
