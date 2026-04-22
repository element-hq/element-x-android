/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import androidx.annotation.Discouraged

/**
 * Represents an analytics span in the Rust SDK.
 */
@Discouraged("This component can cause crashes of the app when using debug builds of the Rust SDK.")
interface AnalyticsSdkSpan {
    /** Enters the span and starts collecting metrics. */
    fun enter()

    /** Exit the span and stop collecting the metrics. A request should be sent shortly after. */
    fun exit()
}
