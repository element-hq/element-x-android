/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.api

interface AnalyticsTransaction {
    fun startChild(operation: String, description: String? = null): AnalyticsTransaction
    fun setData(key: String, value: Any)
    fun isFinished(): Boolean
    fun finish()
}

inline fun <T> AnalyticsTransaction.recordChildTransaction(operation: String, description: String? = null, block: (AnalyticsTransaction) -> T): T {
    val child = startChild(operation, description)
    try {
        val result = block(child)
        return result
    } finally {
        child.finish()
    }
}
