/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.api

interface AnalyticsTransaction {
    /**
     * Start a child span from this transaction.
     */
    fun startChild(operation: String, description: String? = null): AnalyticsTransaction

    /**
     * Adds extra data to the transaction. This data is not indexed, it's just listed.
     */
    fun putExtraData(key: String, value: String)

    /**
     * Similar to [putExtraData], adds extra data that *will be indexed* and can be used for filtering in the analytics portal.
     *
     * **Do not add numerical values using this function, use [putExtraData] instead.**
     */
    fun putIndexableData(key: String, value: String)

    /**
     * Whether the transaction has finished.
     */
    fun isFinished(): Boolean

    /**
     * The optional trace id which can be used for distributed tracing.
     */
    fun traceId(): String?

    /**
     * Attach a throwable to the transaction, so we can know it failed.
     */
    fun attachError(throwable: Throwable)

    /**
     * Finish the transaction. This will schedule an upload of the data.
     */
    fun finish()
}

/**
 * Records a child span from this transaction.
 */
inline fun <T> AnalyticsTransaction.recordChildTransaction(operation: String, description: String? = null, block: (AnalyticsTransaction) -> T): T {
    val child = startChild(operation, description)
    try {
        val result = block(child)
        return result
    } finally {
        child.finish()
    }
}
