/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import kotlinx.coroutines.flow.Flow

/**
 * Local storage for:
 * - user consent (Boolean);
 * - did ask user consent (Boolean);
 * - analytics Id (String).
 */
interface AnalyticsStore {
    val userConsentFlow: Flow<Boolean>
    val didAskUserConsentFlow: Flow<Boolean>
    val analyticsIdFlow: Flow<String>
    suspend fun setUserConsent(newUserConsent: Boolean)
    suspend fun setDidAskUserConsent(newValue: Boolean = true)
    suspend fun setAnalyticsId(newAnalyticsId: String)
    suspend fun reset()
}
