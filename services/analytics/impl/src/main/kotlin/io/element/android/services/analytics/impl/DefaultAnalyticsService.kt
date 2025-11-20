/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.NoopAnalyticsTransaction
import io.element.android.services.analytics.impl.log.analyticsTag
import io.element.android.services.analytics.impl.store.AnalyticsStore
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<AnalyticsService>())
class DefaultAnalyticsService(
    private val analyticsProviders: Set<@JvmSuppressWildcards AnalyticsProvider>,
    private val analyticsStore: AnalyticsStore,
//    private val lateInitUserPropertiesFactory: LateInitUserPropertiesFactory,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    private val sessionObserver: SessionObserver,
) : AnalyticsService, SessionListener {
    private val pendingLongRunningTransactions = ConcurrentHashMap<AnalyticsLongRunningTransaction, AnalyticsTransaction>()

    // Cache for the store values
    private val userConsent = AtomicBoolean(false)

    // Cache for the properties to send
    private var pendingUserProperties: UserProperties? = null

    override val userConsentFlow: Flow<Boolean> = analyticsStore.userConsentFlow
    override val didAskUserConsentFlow: Flow<Boolean> = analyticsStore.didAskUserConsentFlow
    override val analyticsIdFlow: Flow<String> = analyticsStore.analyticsIdFlow

    init {
        observeUserConsent()
        observeSessions()
    }

    override fun getAvailableAnalyticsProviders(): Set<AnalyticsProvider> {
        return analyticsProviders
    }

    override suspend fun setUserConsent(userConsent: Boolean) {
        Timber.tag(analyticsTag.value).d("setUserConsent($userConsent)")
        analyticsStore.setUserConsent(userConsent)
    }

    override suspend fun setDidAskUserConsent() {
        Timber.tag(analyticsTag.value).d("setDidAskUserConsent()")
        analyticsStore.setDidAskUserConsent()
    }

    override suspend fun setAnalyticsId(analyticsId: String) {
        Timber.tag(analyticsTag.value).d("setAnalyticsId($analyticsId)")
        analyticsStore.setAnalyticsId(analyticsId)
    }

    override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
        // Delete the store when the last session is deleted
        if (wasLastSession) {
            analyticsStore.reset()
        }
    }

    private fun observeUserConsent() {
        userConsentFlow
            .onEach { consent ->
                Timber.tag(analyticsTag.value).d("User consent updated to $consent")
                userConsent.set(consent)
                initOrStop()
            }
            .launchIn(coroutineScope)
    }

    private fun observeSessions() {
        sessionObserver.addListener(this)
    }

    private fun initOrStop() {
        if (userConsent.get()) {
            analyticsProviders.onEach { it.init() }
            pendingUserProperties?.let {
                analyticsProviders.onEach { provider -> provider.updateUserProperties(it) }
                pendingUserProperties = null
            }
        } else {
            analyticsProviders.onEach { it.stop() }
        }
    }

    override fun capture(event: VectorAnalyticsEvent) {
        Timber.tag(analyticsTag.value).d("capture($event)")
        if (userConsent.get()) {
            analyticsProviders.onEach { it.capture(event) }
        }
    }

    override fun screen(screen: VectorAnalyticsScreen) {
        Timber.tag(analyticsTag.value).d("screen($screen)")
        if (userConsent.get()) {
            analyticsProviders.onEach { it.screen(screen) }
        }
    }

    override fun updateUserProperties(userProperties: UserProperties) {
        if (userConsent.get()) {
            analyticsProviders.onEach { it.updateUserProperties(userProperties) }
        } else {
            pendingUserProperties = userProperties
        }
    }

    override fun updateSuperProperties(updatedProperties: SuperProperties) {
        this.analyticsProviders.onEach { it.updateSuperProperties(updatedProperties) }
    }

    override fun trackError(throwable: Throwable) {
        if (userConsent.get()) {
            analyticsProviders.onEach { it.trackError(throwable) }
        }
    }

    override fun startTransaction(name: String, operation: String?): AnalyticsTransaction {
        return if (userConsent.get()) {
            analyticsProviders.firstNotNullOfOrNull { it.startTransaction(name, operation) }
        } else {
            null
        } ?: NoopAnalyticsTransaction
    }

    override fun startLongRunningTransaction(
        longRunningTransaction: AnalyticsLongRunningTransaction,
        parentTransaction: AnalyticsTransaction?,
    ): AnalyticsTransaction {
        val transaction = parentTransaction?.startChild(longRunningTransaction.name, longRunningTransaction.operation)
            ?: startTransaction(longRunningTransaction.name, longRunningTransaction.operation)

        pendingLongRunningTransactions[longRunningTransaction] = transaction
        return transaction
    }

    override fun getLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction? {
        return pendingLongRunningTransactions[longRunningTransaction]
    }

    override fun removeLongRunningTransaction(longRunningTransaction: AnalyticsLongRunningTransaction): AnalyticsTransaction? {
        return pendingLongRunningTransactions.remove(longRunningTransaction)
    }
}
