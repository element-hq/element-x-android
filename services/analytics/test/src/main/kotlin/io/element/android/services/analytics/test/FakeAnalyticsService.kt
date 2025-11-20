/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.test

import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.NoopAnalyticsTransaction
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAnalyticsService(
    isEnabled: Boolean = false,
    didAskUserConsent: Boolean = false,
) : AnalyticsService {
    private val isEnabledFlow = MutableStateFlow(isEnabled)
    override val didAskUserConsentFlow = MutableStateFlow(didAskUserConsent)
    val capturedEvents = mutableListOf<VectorAnalyticsEvent>()
    val screenEvents = mutableListOf<VectorAnalyticsScreen>()
    val trackedErrors = mutableListOf<Throwable>()
    val capturedUserProperties = mutableListOf<UserProperties>()
    val longRunningTransactions = mutableMapOf<AnalyticsLongRunningTransaction, AnalyticsTransaction>()

    override fun getAvailableAnalyticsProviders(): Set<AnalyticsProvider> = emptySet()

    override val userConsentFlow: Flow<Boolean> = isEnabledFlow.asStateFlow()

    override suspend fun setUserConsent(userConsent: Boolean) {
        isEnabledFlow.value = userConsent
    }

    override suspend fun setDidAskUserConsent() {
        didAskUserConsentFlow.value = true
    }

    override val analyticsIdFlow: Flow<String> = MutableStateFlow("")

    override suspend fun setAnalyticsId(analyticsId: String) {
    }

    override fun capture(event: VectorAnalyticsEvent) {
        capturedEvents += event
    }

    override fun screen(screen: VectorAnalyticsScreen) {
        screenEvents += screen
    }

    override fun updateUserProperties(userProperties: UserProperties) {
        capturedUserProperties += userProperties
    }

    override fun trackError(throwable: Throwable) {
        trackedErrors += throwable
    }

    override fun updateSuperProperties(updatedProperties: SuperProperties) {
        // No op
    }

    override fun startTransaction(name: String, operation: String?): AnalyticsTransaction = NoopAnalyticsTransaction
    override fun startLongRunningTransaction(
        longRunningTransaction: AnalyticsLongRunningTransaction,
        parentTransaction: AnalyticsTransaction?
    ): AnalyticsTransaction {
        longRunningTransactions[longRunningTransaction] = NoopAnalyticsTransaction
        return NoopAnalyticsTransaction
    }

    override fun getLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction? {
        return longRunningTransactions[longRunningTransaction]
    }

    override fun removeLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction? {
        return longRunningTransactions.remove(longRunningTransaction)
    }
}
