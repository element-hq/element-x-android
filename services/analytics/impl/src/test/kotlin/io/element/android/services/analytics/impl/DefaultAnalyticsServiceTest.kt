/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.services.analytics.impl

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.MobileScreen
import im.vector.app.features.analytics.plan.PollEnd
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.sessionstorage.test.observer.NoOpSessionObserver
import io.element.android.services.analytics.impl.store.AnalyticsStore
import io.element.android.services.analytics.impl.store.FakeAnalyticsStore
import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.services.analyticsproviders.test.FakeAnalyticsProvider
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAnalyticsServiceTest {
    @Test
    fun `getAvailableAnalyticsProviders return the set of provider`() = runTest {
        val providers = setOf(
            FakeAnalyticsProvider(name = "provider1", stopLambda = { }),
            FakeAnalyticsProvider(name = "provider2", stopLambda = { }),
        )
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsProviders = providers
        )
        val result = sut.getAvailableAnalyticsProviders()
        assertThat(result).isEqualTo(providers)
    }

    @Test
    fun `when consent is not provided, capture is no op`() = runTest {
        val sut = createDefaultAnalyticsService(backgroundScope)
        sut.capture(anEvent)
    }

    @Test
    fun `when consent is provided, capture is sent to the AnalyticsProvider`() = runTest {
        val initLambda = lambdaRecorder<Unit> { }
        val captureLambda = lambdaRecorder<VectorAnalyticsEvent, Unit> { _ -> }
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = FakeAnalyticsStore(defaultUserConsent = true),
            analyticsProviders = setOf(
                FakeAnalyticsProvider(
                    initLambda = initLambda,
                    captureLambda = captureLambda,
                )
            )
        )
        initLambda.assertions().isCalledOnce()
        sut.capture(anEvent)
        captureLambda.assertions().isCalledOnce().with(value(anEvent))
    }

    @Test
    fun `when consent is not provided, screen is no op`() = runTest {
        val sut = createDefaultAnalyticsService(backgroundScope)
        sut.screen(aScreen)
    }

    @Test
    fun `when consent is provided, screen is sent to the AnalyticsProvider`() = runTest {
        val initLambda = lambdaRecorder<Unit> { }
        val screenLambda = lambdaRecorder<VectorAnalyticsScreen, Unit> { _ -> }
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = FakeAnalyticsStore(defaultUserConsent = true),
            analyticsProviders = setOf(
                FakeAnalyticsProvider(
                    initLambda = initLambda,
                    screenLambda = screenLambda,
                )
            )
        )
        initLambda.assertions().isCalledOnce()
        sut.screen(aScreen)
        screenLambda.assertions().isCalledOnce().with(value(aScreen))
    }

    @Test
    fun `when consent is not provided, trackError is no op`() = runTest {
        val sut = createDefaultAnalyticsService(backgroundScope)
        sut.trackError(anError)
    }

    @Test
    fun `when consent is provided, trackError is sent to the AnalyticsProvider`() = runTest {
        val initLambda = lambdaRecorder<Unit> { }
        val trackErrorLambda = lambdaRecorder<Throwable, Unit> { _ -> }
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = FakeAnalyticsStore(defaultUserConsent = true),
            analyticsProviders = setOf(
                FakeAnalyticsProvider(
                    initLambda = initLambda,
                    trackErrorLambda = trackErrorLambda,
                )
            )
        )
        initLambda.assertions().isCalledOnce()
        sut.trackError(anError)
        trackErrorLambda.assertions().isCalledOnce().with(value(anError))
    }

    @Test
    fun `setUserConsent is sent to the store`() = runTest {
        val store = FakeAnalyticsStore()
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = store,
        )
        assertThat(store.userConsentFlow.first()).isFalse()
        assertThat(sut.userConsentFlow.first()).isFalse()
        sut.setUserConsent(true)
        assertThat(store.userConsentFlow.first()).isTrue()
        assertThat(sut.userConsentFlow.first()).isTrue()
    }

    @Test
    fun `setAnalyticsId is sent to the store`() = runTest {
        val store = FakeAnalyticsStore()
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = store,
        )
        assertThat(store.analyticsIdFlow.first()).isEqualTo("")
        assertThat(sut.analyticsIdFlow.first()).isEqualTo("")
        sut.setAnalyticsId(AN_ID)
        assertThat(store.analyticsIdFlow.first()).isEqualTo(AN_ID)
        assertThat(sut.analyticsIdFlow.first()).isEqualTo(AN_ID)
    }

    @Test
    fun `setDidAskUserConsent is sent to the store`() = runTest {
        val store = FakeAnalyticsStore()
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = store,
        )
        assertThat(store.didAskUserConsentFlow.first()).isFalse()
        assertThat(sut.didAskUserConsentFlow.first()).isFalse()
        sut.setDidAskUserConsent()
        assertThat(store.didAskUserConsentFlow.first()).isTrue()
        assertThat(sut.didAskUserConsentFlow.first()).isTrue()
    }

    @Test
    fun `when a session is deleted, the store is reset`() = runTest {
        val resetLambda = lambdaRecorder<Unit> { }
        val store = FakeAnalyticsStore(
            resetLambda = resetLambda,
        )
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = store,
        )
        sut.onSessionDeleted("userId")
        resetLambda.assertions().isCalledOnce()
    }

    @Test
    fun `when reset is invoked, the user consent is reset`() = runTest {
        val store = FakeAnalyticsStore(
            defaultDidAskUserConsent = true,
        )
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsStore = store,
        )
        assertThat(store.didAskUserConsentFlow.first()).isTrue()
        sut.reset()
        assertThat(store.didAskUserConsentFlow.first()).isFalse()
    }

    @Test
    fun `when a session is added, nothing happen`() = runTest {
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
        )
        sut.onSessionCreated("userId")
    }

    @Test
    fun `when consent is not provided, updateUserProperties is stored for future use`() = runTest {
        val completable = CompletableDeferred<Unit>()
        val updateUserPropertiesLambda = lambdaRecorder<UserProperties, Unit> { _ ->
            completable.complete(Unit)
        }
        launch {
            val sut = createDefaultAnalyticsService(
                coroutineScope = this,
                analyticsProviders = setOf(
                    FakeAnalyticsProvider(
                        initLambda = { },
                        stopLambda = { },
                        updateUserPropertiesLambda = updateUserPropertiesLambda,
                    )
                )
            )
            sut.updateUserProperties(aUserProperty)
            updateUserPropertiesLambda.assertions().isNeverCalled()
            // Give user consent
            sut.setUserConsent(true)
            completable.await()
            updateUserPropertiesLambda.assertions().isCalledOnce().with(value(aUserProperty))
            cancel()
        }
    }

    @Test
    fun `when consent is provided, updateUserProperties is sent to the provider`() = runTest {
        val updateUserPropertiesLambda = lambdaRecorder<UserProperties, Unit> { _ -> }
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsProviders = setOf(
                FakeAnalyticsProvider(
                    initLambda = { },
                    updateUserPropertiesLambda = updateUserPropertiesLambda,
                )
            ),
            analyticsStore = FakeAnalyticsStore(defaultUserConsent = true),
        )
        sut.updateUserProperties(aUserProperty)
        updateUserPropertiesLambda.assertions().isCalledOnce().with(value(aUserProperty))
    }

    @Test
    fun `when super properties are updated, updateSuperProperties is sent to the provider`() = runTest {
        val updateSuperPropertiesLambda = lambdaRecorder<SuperProperties, Unit> { _ -> }
        val sut = createDefaultAnalyticsService(
            coroutineScope = backgroundScope,
            analyticsProviders = setOf(
                FakeAnalyticsProvider(
                    initLambda = { },
                    updateSuperPropertiesLambda = updateSuperPropertiesLambda,
                )
            ),
            analyticsStore = FakeAnalyticsStore(defaultUserConsent = true),
        )
        sut.updateSuperProperties(aSuperProperty)
        updateSuperPropertiesLambda.assertions().isCalledOnce().with(value(aSuperProperty))
    }

    private suspend fun createDefaultAnalyticsService(
        coroutineScope: CoroutineScope,
        analyticsProviders: Set<@JvmSuppressWildcards AnalyticsProvider> = setOf(
            FakeAnalyticsProvider(
                stopLambda = { },
            )
        ),
        analyticsStore: AnalyticsStore = FakeAnalyticsStore(),
        sessionObserver: SessionObserver = NoOpSessionObserver(),
    ) = DefaultAnalyticsService(
        analyticsProviders = analyticsProviders,
        analyticsStore = analyticsStore,
        coroutineScope = coroutineScope,
        sessionObserver = sessionObserver,
    ).also {
        // Wait for the service to be ready
        delay(1)
    }

    private companion object {
        private val anEvent = PollEnd()
        private val aScreen = MobileScreen(screenName = MobileScreen.ScreenName.User)
        private val aUserProperty = UserProperties(
            ftueUseCaseSelection = UserProperties.FtueUseCaseSelection.WorkMessaging,
        )
        private val aSuperProperty = SuperProperties(
            appPlatform = SuperProperties.AppPlatform.EXA,
            cryptoSDK = SuperProperties.CryptoSDK.Rust,
            cryptoSDKVersion = "0.0"
        )
        private val anError = Exception("a reason")
        private const val AN_ID = "anId"
    }
}
