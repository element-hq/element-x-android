/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import io.element.android.services.analyticsproviders.api.AnalyticsTransaction

object NoopAnalyticsTransaction : AnalyticsTransaction {
    override fun startChild(operation: String, description: String?): AnalyticsTransaction = NoopAnalyticsTransaction
    override fun putExtraData(key: String, value: String) {}
    override fun putIndexableData(key: String, value: String) {}
    override fun isFinished(): Boolean = true
    override fun traceId(): String? = null
    override fun attachError(throwable: Throwable) {}
    override fun finish() {}
}
