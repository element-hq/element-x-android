/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.api

import io.element.android.services.analyticsproviders.api.trackers.AnalyticsTracker
import io.element.android.services.analyticsproviders.api.trackers.ErrorTracker

interface AnalyticsProvider : AnalyticsTracker, ErrorTracker {
    /**
     * User friendly name.
     */
    val name: String

    fun init()

    fun stop()

    fun startTransaction(name: String, operation: String? = null): AnalyticsTransaction?
}
