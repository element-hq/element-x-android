/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.analytics.test

import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAnalyticsService(
    isEnabled: Boolean = false,
    didAskUserConsent: Boolean = false,
    private val resetLambda: () -> Unit = {},
) : AnalyticsService {
    private val isEnabledFlow = MutableStateFlow(isEnabled)
    private val didAskUserConsentFlow = MutableStateFlow(didAskUserConsent)
    val capturedEvents = mutableListOf<VectorAnalyticsEvent>()
    val screenEvents = mutableListOf<VectorAnalyticsScreen>()
    val trackedErrors = mutableListOf<Throwable>()
    val capturedUserProperties = mutableListOf<UserProperties>()

    override fun getAvailableAnalyticsProviders(): Set<AnalyticsProvider> = emptySet()

    override fun getUserConsent(): Flow<Boolean> = isEnabledFlow

    override suspend fun setUserConsent(userConsent: Boolean) {
        isEnabledFlow.value = userConsent
    }

    override fun didAskUserConsent(): Flow<Boolean> = didAskUserConsentFlow

    override suspend fun setDidAskUserConsent() {
        didAskUserConsentFlow.value = true
    }

    override fun getAnalyticsId(): Flow<String> = MutableStateFlow("")

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

    override suspend fun reset() {
        didAskUserConsentFlow.value = false
        resetLambda()
    }
}
