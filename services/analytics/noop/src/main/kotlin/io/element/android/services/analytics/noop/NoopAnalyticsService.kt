/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.noop

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
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
import kotlinx.coroutines.flow.flowOf

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NoopAnalyticsService : AnalyticsService {
    override fun getAvailableAnalyticsProviders(): Set<AnalyticsProvider> = emptySet()
    override val userConsentFlow: Flow<Boolean> = flowOf(false)
    override suspend fun setUserConsent(userConsent: Boolean) = Unit
    override val didAskUserConsentFlow: Flow<Boolean> = flowOf(true)
    override suspend fun setDidAskUserConsent() = Unit
    override val analyticsIdFlow: Flow<String> = flowOf("")
    override suspend fun setAnalyticsId(analyticsId: String) = Unit
    override fun capture(event: VectorAnalyticsEvent) = Unit
    override fun screen(screen: VectorAnalyticsScreen) = Unit
    override fun updateUserProperties(userProperties: UserProperties) = Unit
    override fun trackError(throwable: Throwable) = Unit
    override fun updateSuperProperties(updatedProperties: SuperProperties) = Unit
    override fun startTransaction(name: String, operation: String?): AnalyticsTransaction = NoopAnalyticsTransaction
    override fun startLongRunningTransaction(
        longRunningTransaction: AnalyticsLongRunningTransaction,
        parentTransaction: AnalyticsTransaction?,
    ): AnalyticsTransaction = NoopAnalyticsTransaction
    override fun getLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction? = null
    override fun removeLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction) = NoopAnalyticsTransaction
}
