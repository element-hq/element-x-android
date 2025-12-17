/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

/**
 * Manager to handle SDK analytics (e.g., Sentry).
 */
interface AnalyticsSdkManager {
    /**
     * Enable or disable SDK analytics.
     */
    fun enableSdkAnalytics(enabled: Boolean)

    /**
     * Start a new span with the given [name], using [parentTraceId] to optionally attach it to a parent transaction.
     */
    fun startSpan(name: String, parentTraceId: String? = null): AnalyticsSdkSpan

    /**
     * Create a 'bridge' span optionally linking it to a parent trace via [parentTraceId].
     */
    fun bridge(parentTraceId: String? = null): AnalyticsSdkSpan
}
