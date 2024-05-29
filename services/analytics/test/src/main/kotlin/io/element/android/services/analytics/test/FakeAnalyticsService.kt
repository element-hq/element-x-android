/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.services.analytics.test

import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAnalyticsService(
    isEnabled: Boolean = false,
    didAskUserConsent: Boolean = false
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

    override suspend fun reset() {
        didAskUserConsentFlow.value = false
    }
}
