/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.test

import io.element.android.services.analytics.api.AnalyticsSdkSpan
import io.element.android.services.analytics.api.AnalyticsSdkSpanFactory
import io.element.android.services.analytics.api.NoopAnalyticsSdkSpan

class FakeAnalyticsSdkSpanFactory : AnalyticsSdkSpanFactory {
    override fun create(name: String, parentTraceId: String?): AnalyticsSdkSpan = NoopAnalyticsSdkSpan

    override fun bridge(parentTraceId: String?): AnalyticsSdkSpan = NoopAnalyticsSdkSpan
}
