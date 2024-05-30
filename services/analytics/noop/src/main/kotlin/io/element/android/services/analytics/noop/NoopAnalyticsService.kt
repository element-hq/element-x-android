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

package io.element.android.services.analytics.noop

import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NoopAnalyticsService @Inject constructor() : AnalyticsService {
    override fun getAvailableAnalyticsProviders(): Set<AnalyticsProvider> = emptySet()
    override fun getUserConsent(): Flow<Boolean> = flowOf(false)
    override suspend fun setUserConsent(userConsent: Boolean) = Unit
    override fun didAskUserConsent(): Flow<Boolean> = flowOf(true)
    override suspend fun setDidAskUserConsent() = Unit
    override fun getAnalyticsId(): Flow<String> = flowOf("")
    override suspend fun setAnalyticsId(analyticsId: String) = Unit
    override suspend fun reset() = Unit
    override fun capture(event: VectorAnalyticsEvent) = Unit
    override fun screen(screen: VectorAnalyticsScreen) = Unit
    override fun updateUserProperties(userProperties: UserProperties) = Unit
    override fun trackError(throwable: Throwable) = Unit
}
