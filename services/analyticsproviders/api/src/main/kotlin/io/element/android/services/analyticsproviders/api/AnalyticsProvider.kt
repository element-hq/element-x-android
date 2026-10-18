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

/**
 * One analytics backend, such as PostHog or Sentry.
 *
 * Providers are driven by the analytics service rather than used directly, and only receive data once the user has consented.
 */
interface AnalyticsProvider : AnalyticsTracker, ErrorTracker {
    /**
     * User friendly name.
     */
    val name: String

    /** Starts the backend, to be called when the user consents to analytics. */
    fun init()

    /** Stops the backend and drops any pending data, to be called when the user withdraws their consent. */
    fun stop()

    /**
     * Starts a performance transaction, returning `null` when this provider does not support them.
     *
     * @param name the name the transaction is reported under.
     * @param operation the kind of operation being measured.
     * @param description a human readable detail shown alongside the measurement.
     */
    fun startTransaction(name: String, operation: String? = null, description: String? = null): AnalyticsTransaction?
}
