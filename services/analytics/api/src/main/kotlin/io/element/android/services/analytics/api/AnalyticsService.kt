/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.element.android.services.analyticsproviders.api.trackers.AnalyticsTracker
import io.element.android.services.analyticsproviders.api.trackers.ErrorTracker
import kotlinx.coroutines.flow.Flow

interface AnalyticsService : AnalyticsTracker, ErrorTracker {
    /**
     * Get the available analytics providers.
     */
    fun getAvailableAnalyticsProviders(): Set<AnalyticsProvider>

    /**
     * A Flow of Boolean, true if the user has given their consent.
     */
    val userConsentFlow: Flow<Boolean>

    /**
     * Update the user consent value.
     */
    suspend fun setUserConsent(userConsent: Boolean)

    /**
     * A Flow of Boolean, true if the user has been asked for their consent.
     */
    val didAskUserConsentFlow: Flow<Boolean>

    /**
     * Store the fact that the user has been asked for their consent.
     */
    suspend fun setDidAskUserConsent()

    /**
     * A Flow of String, used for analytics Id.
     */
    val analyticsIdFlow: Flow<String>

    /**
     * Update analyticsId from the AccountData.
     */
    suspend fun setAnalyticsId(analyticsId: String)

    /**
     * Starts a transaction to measure the performance of an operation.
     */
    fun startTransaction(name: String, operation: String? = null): AnalyticsTransaction

    /**
     * Starts an [AnalyticsLongRunningTransaction], that can be shared with other components.
     */
    fun startLongRunningTransaction(
        longRunningTransaction: AnalyticsLongRunningTransaction,
        parentTransaction: AnalyticsTransaction? = null
    ): AnalyticsTransaction

    /**
     * Gets an ongoing [AnalyticsLongRunningTransaction], if it exists.
     */
    fun getLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction?

    /**
     * Removes an ongoing [AnalyticsLongRunningTransaction] so it's no longer shared.
     */
    fun removeLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction?
}

inline fun <T> AnalyticsService.recordTransaction(
    name: String,
    operation: String,
    parentTransaction: AnalyticsTransaction? = null,
    block: (AnalyticsTransaction) -> T
): T {
    val transaction = parentTransaction?.startChild(name, operation)
        ?: startTransaction(name, operation)
    try {
        val result = block(transaction)
        return result
    } finally {
        transaction.finish()
    }
}

/**
 * Cancels a long running transaction. It behaves the same as [AnalyticsService.removeLongRunningTransaction],
 * but it doesn't return the transaction so we can't finish it later.
 */
fun AnalyticsService.cancelLongRunningTransaction(
    longRunningTransaction: AnalyticsLongRunningTransaction
) = removeLongRunningTransaction(longRunningTransaction)

/**
 * Finishes a long running transaction if it exists. Optionally performs an [action] with the transaction before finishing it.
 */
fun AnalyticsService.finishLongRunningTransaction(
    longRunningTransaction: AnalyticsLongRunningTransaction,
    action: (AnalyticsTransaction) -> Unit = {},
) {
    removeLongRunningTransaction(longRunningTransaction)?.let {
        action(it)
        it.finish()
    }
}
