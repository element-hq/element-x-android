/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import androidx.annotation.Discouraged
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.element.android.services.analyticsproviders.api.trackers.AnalyticsTracker
import io.element.android.services.analyticsproviders.api.trackers.ErrorTracker
import kotlinx.coroutines.flow.Flow

/**
 * Central analytics entry point: user consent, the analytics identity, and performance transactions.
 *
 * Nothing is sent to the providers until the user has given their consent through [setUserConsent].
 */
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
     *
     * @param userConsent true when the user agrees to analytics being collected.
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
     *
     * @param analyticsId the identifier shared across the user's clients, so their events are attributed to one person.
     */
    suspend fun setAnalyticsId(analyticsId: String)

    /**
     * Starts a transaction to measure the performance of an operation.
     * The caller is responsible for finishing it; see `recordTransaction` for a scoped alternative.
     *
     * @param name the name the transaction is reported under.
     * @param operation the kind of operation being measured.
     * @param description a human readable detail shown alongside the measurement.
     */
    fun startTransaction(name: String, operation: String? = null, description: String? = null): AnalyticsTransaction

    /**
     * Starts an [AnalyticsLongRunningTransaction], that can be shared with other components.
     *
     * @param longRunningTransaction which of the known long running operations is starting.
     * @param parentTransaction the transaction to attach this one to, or `null` to start a root transaction.
     */
    fun startLongRunningTransaction(
        longRunningTransaction: AnalyticsLongRunningTransaction,
        parentTransaction: AnalyticsTransaction? = null
    ): AnalyticsTransaction

    /**
     * Gets an ongoing [AnalyticsLongRunningTransaction], if it exists.
     *
     * @param longRunningTransaction the long running operation to look up.
     */
    fun getLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction?

    /**
     * Removes an ongoing [AnalyticsLongRunningTransaction] so it's no longer shared.
     *
     * @param longRunningTransaction the long running operation to stop sharing.
     * @return the transaction that was removed, or `null` when none was ongoing.
     */
    fun removeLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction?

    /**
     * Enter a span inside the Rust SDK tracing system. If a [parentTraceId] is provided, the SDK trace will be added as a child of that trace.
     *
     * @param name the name of the span.
     * @param parentTraceId the trace to attach the span to, or `null` to start a root span.
     */
    @Discouraged("This method can cause crashes of the app when using debug builds of the Rust SDK.")
    fun enterSdkSpan(name: String?, parentTraceId: String?): AnalyticsSdkSpan
}

inline fun <T> AnalyticsService.recordTransaction(
    name: String,
    operation: String,
    description: String? = null,
    parentTransaction: AnalyticsTransaction? = null,
    block: (AnalyticsTransaction) -> T
): T {
    val transaction = parentTransaction?.startChild(operation, description)
        ?: startTransaction(name, operation, description)
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
): Boolean {
    return removeLongRunningTransaction(longRunningTransaction)?.let {
        action(it)
        it.finish()
        true
    } ?: false
}

@Discouraged("This method can cause crashes of the app when using debug builds of the Rust SDK.")
inline fun <T> AnalyticsService.inBridgeSdkSpan(parentTraceId: String?, block: (AnalyticsSdkSpan) -> T): T {
    val span = enterSdkSpan(name = null, parentTraceId = parentTraceId)
    return try {
        block(span)
    } finally {
        span.exit()
    }
}
