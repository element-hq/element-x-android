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

package io.element.android.services.analytics.impl

import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.impl.log.analyticsTag
import io.element.android.services.analytics.providers.api.AnalyticsProvider
import io.element.android.services.analytics.providers.posthog.store.AnalyticsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAnalyticsService @Inject constructor(
    private val analyticsProviders: Set<@JvmSuppressWildcards AnalyticsProvider>,
    private val analyticsStore: AnalyticsStore,
//    private val lateInitUserPropertiesFactory: LateInitUserPropertiesFactory,
    private val coroutineScope: CoroutineScope
) : AnalyticsService {
    // Cache for the store values
    private var userConsent: Boolean? = null
    // Cache for the properties to send
    private var pendingUserProperties: UserProperties? = null


    override fun init() {
        observeUserConsent()
        observeAnalyticsId()
    }

    override fun getAvailableAnalyticsProviders(): List<AnalyticsProvider> {
        return analyticsProviders.sortedBy { it.index }
    }

    override fun getUserConsent(): Flow<Boolean> {
        return analyticsStore.userConsentFlow
    }

    override suspend fun setUserConsent(userConsent: Boolean) {
        Timber.tag(analyticsTag.value).d("setUserConsent($userConsent)")
        analyticsStore.setUserConsent(userConsent)
    }

    override fun didAskUserConsent(): Flow<Boolean> {
        return analyticsStore.didAskUserConsentFlow
    }

    override suspend fun setDidAskUserConsent() {
        Timber.tag(analyticsTag.value).d("setDidAskUserConsent()")
        analyticsStore.setDidAskUserConsent()
    }

    override fun getAnalyticsId(): Flow<String> {
        return analyticsStore.analyticsIdFlow
    }

    override suspend fun setAnalyticsId(analyticsId: String) {
        Timber.tag(analyticsTag.value).d("setAnalyticsId($analyticsId)")
        analyticsStore.setAnalyticsId(analyticsId)
    }

    override suspend fun onSignOut() {
        // stop all providers
        analyticsProviders.onEach { it.stop() }
    }

    private fun observeAnalyticsId() {
        getAnalyticsId()
                .onEach { id ->
                    Timber.tag(analyticsTag.value).d("Analytics Id updated to '$id'")
//                    analyticsId = id
//                    identifyPostHog()
                }
                .launchIn(coroutineScope)
    }



    private fun observeUserConsent() {
        getUserConsent()
                .onEach { consent ->
                    Timber.tag(analyticsTag.value).d("User consent updated to $consent")
                    userConsent = consent
                    initOrStop()
                }
                .launchIn(coroutineScope)
    }

    private fun initOrStop() {
        userConsent?.let { _userConsent ->
            when (_userConsent) {
                true -> {
                    pendingUserProperties?.let {
                        analyticsProviders.onEach { provider -> provider.updateUserProperties(it) }
                        pendingUserProperties = null
                    }
                }
                false -> {}
            }
        }
    }

    override fun capture(event: VectorAnalyticsEvent) {
        Timber.tag(analyticsTag.value).d("capture($event)")
        if (userConsent == true) {
            analyticsProviders.onEach { it.capture(event) }
        }

    }

    override fun screen(screen: VectorAnalyticsScreen) {
        Timber.tag(analyticsTag.value).d("screen($screen)")
        if (userConsent == true) {
            analyticsProviders.onEach { it.screen(screen) }
        }
    }

    override fun updateUserProperties(userProperties: UserProperties) {
        if (userConsent == true) {
            analyticsProviders.onEach { it.updateUserProperties(userProperties) }
        } else {
            pendingUserProperties = userProperties
        }
    }

    override fun trackError(throwable: Throwable) {
        if (userConsent == true) {
            analyticsProviders.onEach { it.trackError(throwable) }
        }
    }
}
