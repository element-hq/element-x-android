/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

interface AnalyticsSdkSpanFactory {
    /** Create an SDK span with the provided [name] and optional [parentTraceId]. */
    fun create(name: String, parentTraceId: String?): AnalyticsSdkSpan

    /** Create a bridge span which will join our tracing spans to the SDK ones while it's active. */
    fun bridge(parentTraceId: String?): AnalyticsSdkSpan
}
