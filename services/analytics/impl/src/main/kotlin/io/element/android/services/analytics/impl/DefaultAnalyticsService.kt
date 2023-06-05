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

import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.impl.log.analyticsTag
import io.element.android.services.analytics.impl.store.AnalyticsStore
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, boundType = AnalyticsService::class)
class DefaultAnalyticsService @Inject constructor(
    private val analyticsProviders: Set<@JvmSuppressWildcards AnalyticsProvider>,
    private val analyticsStore: AnalyticsStore,
//    private val lateInitUserPropertiesFactory: LateInitUserPropertiesFactory,
    private val coroutineScope: CoroutineScope,
    private val sessionObserver: SessionObserver,
) : AnalyticsService, SessionListener {
    // Cache for the store values
    private var userConsent: Boolean? = null

    // Cache for the properties to send
    private var pendingUserProperties: UserProperties? = null

    init {
        observeUserConsent()
        observeSessions()
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

    override suspend fun onSessionCreated(userId: String) {
        // Nothing to do
    }

    override suspend fun onSessionDeleted(userId: String) {
        // Delete the store
        analyticsStore.reset()
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

    private fun observeSessions() {
        sessionObserver.addListener(this)
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
