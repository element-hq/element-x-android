/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.analytics

import io.element.android.services.analytics.api.AnalyticsSdkManager
import io.element.android.services.analytics.api.AnalyticsSdkSpan
import io.element.android.services.analytics.api.NoopAnalyticsSdkSpan
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAnalyticsSdkManager(
    private val enableSdkAnalyticsLambda: ((Boolean) -> Unit) = { lambdaError() },
) : AnalyticsSdkManager {
    override fun enableSdkAnalytics(enabled: Boolean) {
        enableSdkAnalyticsLambda(enabled)
    }

    override fun startSpan(name: String, parentTraceId: String?): AnalyticsSdkSpan = NoopAnalyticsSdkSpan
    override fun bridge(parentTraceId: String?): AnalyticsSdkSpan = NoopAnalyticsSdkSpan
}
