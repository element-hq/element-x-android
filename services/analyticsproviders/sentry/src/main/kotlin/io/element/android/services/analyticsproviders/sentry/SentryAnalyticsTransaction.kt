/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.sentry

import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.sentry.ISpan
import io.sentry.ITransaction
import io.sentry.Sentry
import timber.log.Timber

class SentryAnalyticsTransaction private constructor(span: ISpan) : AnalyticsTransaction {
    constructor(name: String, operation: String?, description: String? = null) : this(
        Sentry.startTransaction(name, operation.orEmpty()).also { it.description = description }
    )
    private val inner = span

    override fun startChild(operation: String, description: String?): AnalyticsTransaction = SentryAnalyticsTransaction(
        inner.startChild(operation, description)
    )

    override fun putIndexableData(key: String, value: String) = inner.setTag(key, value)
    override fun putExtraData(key: String, value: String) = inner.setData(key, value)
    override fun traceId(): String? = inner.toSentryTrace().value
    override fun isFinished(): Boolean = inner.isFinished
    override fun attachError(throwable: Throwable) {
        inner.throwable = throwable
    }
    override fun finish() {
        val name = if (inner is ITransaction) inner.name else inner.operation
        Timber.d("Finishing transaction: '$name'")
        inner.finish()
    }
}
