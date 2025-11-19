/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.sentry

import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.sentry.ISpan
import io.sentry.Sentry

class SentryAnalyticsTransaction private constructor(span: ISpan) : AnalyticsTransaction {
    constructor(name: String, operation: String?) : this(Sentry.startTransaction(name, operation.orEmpty()))
    private val inner = span

    override fun startChild(operation: String, description: String?): AnalyticsTransaction = SentryAnalyticsTransaction(
        inner.startChild(operation, description)
    )
    override fun setData(key: String, value: Any) = inner.setData(key, value)
    override fun isFinished(): Boolean = inner.isFinished
    override fun finish() = inner.finish()
}
