/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.analytics

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.services.analytics.api.AnalyticsSdkManager
import io.element.android.services.analytics.api.AnalyticsSdkSpan
import org.matrix.rustcomponents.sdk.enableSentryLogging

@ContributesBinding(AppScope::class)
class RustAnalyticsSdkManager : AnalyticsSdkManager {
    override fun enableSdkAnalytics(enabled: Boolean) {
        enableSentryLogging(enabled)
    }

    override fun startSpan(name: String, parentTraceId: String?): AnalyticsSdkSpan {
        return RustAnalyticsSdkSpan(name = name, parentTraceId = parentTraceId)
    }

    override fun bridge(parentTraceId: String?): AnalyticsSdkSpan {
        // A bridge span has no name
        return RustAnalyticsSdkSpan(name = null, parentTraceId = parentTraceId)
    }
}
