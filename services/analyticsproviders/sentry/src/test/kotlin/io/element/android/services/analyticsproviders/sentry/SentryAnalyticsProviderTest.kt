/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.sentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.core.data.megaBytes
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.analytics.GetDatabaseSizesUseCase
import io.element.android.libraries.matrix.api.analytics.SdkStoreSizes
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.services.analyticsproviders.api.AnalyticsUserData
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.sentry.Sentry
import io.sentry.SentryTracer
import io.sentry.protocol.SentryId
import io.sentry.protocol.SentryTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SentryAnalyticsProviderTest {
    @Test
    fun `init enables Sentry`() {
        createSentryAnalyticsProvider().run {
            init()
        }
        assertThat(Sentry.isEnabled()).isTrue()
    }

    @Test
    fun `stop disables Sentry`() {
        createSentryAnalyticsProvider().run {
            init()
            stop()
        }
        assertThat(Sentry.isEnabled()).isFalse()
    }

    @Test
    fun `capture adds a breadcrumb`() {
        createSentryAnalyticsProvider().run {
            init()
            capture(object : VectorAnalyticsEvent {
                override fun getName(): String = "Test"
                override fun getProperties(): Map<String, Any?>? = null
            })
        }
        assertThat(Sentry.getCurrentScopes().scope.breadcrumbs.isNotEmpty()).isTrue()
    }

    @Test
    fun `screen adds a breadcrumb`() {
        createSentryAnalyticsProvider().run {
            init()
            screen(object : VectorAnalyticsScreen {
                override fun getName(): String = "Test"
                override fun getProperties(): Map<String, Any>? = null
            })
        }
        assertThat(Sentry.getCurrentScopes().scope.breadcrumbs.isNotEmpty()).isTrue()
    }

    @Test
    fun `updateUserProperties and updateSuperProperties do nothing`() {
        createSentryAnalyticsProvider().run {
            init()
            updateUserProperties(UserProperties())
            updateSuperProperties(SuperProperties())
        }
        val scope = Sentry.getCurrentScopes().scope
        assertThat(scope.extras.isEmpty()).isTrue()
        assertThat(scope.tags.isEmpty()).isTrue()
        assertThat(scope.contexts.isEmpty()).isTrue()
    }

    @Test
    fun `addExtraData adds a global extra`() {
        createSentryAnalyticsProvider().run {
            init()
            addExtraData("foo", "bar")
        }
        val scope = Sentry.getCurrentScopes().scope
        assertThat(scope.extras.get("foo")).isEqualTo("bar")
    }

    @Test
    fun `addIndexableData adds a global tag`() {
        createSentryAnalyticsProvider().run {
            init()
            addIndexableData("foo", "bar")
        }
        val scope = Sentry.getCurrentScopes().scope
        assertThat(scope.tags.get("foo")).isEqualTo("bar")
    }

    @Test
    fun `trackError adds a throwable to the global scope`() {
        var initialLastId: SentryId? = null
        createSentryAnalyticsProvider().run {
            init()
            initialLastId = Sentry.getLastEventId()
            trackError(IllegalStateException("foo"))
        }
        assertThat(Sentry.getLastEventId()).isNotEqualTo(initialLastId)
    }

    @Test
    fun `startTransaction starts a SentryAnalyticsTransaction`() {
        val transaction = createSentryAnalyticsProvider().run {
            init()
            startTransaction("foo")
        }
        assertThat(transaction).isNotNull()
        assertThat(transaction).isInstanceOf(SentryAnalyticsTransaction::class.java)
    }

    @Test
    fun `prepareTransactionBeforeSend removes unwanted data and adds DB size extras`() {
        createSentryAnalyticsProvider(
            getDatabaseSizesUseCase = GetDatabaseSizesUseCase {
                Result.success(
                    SdkStoreSizes(stateStore = 10.megaBytes, eventCacheStore = 11.megaBytes, mediaStore = 12.megaBytes, cryptoStore = 13.megaBytes)
                )
            },
            appNavigationStateService = FakeAppNavigationStateService(
                MutableStateFlow(AppNavigationState(navigationState = NavigationState.Session("owner", A_SESSION_ID), isInForeground = true))
            )
        ).run {
            init()

            val transaction = SentryTransaction(Sentry.startTransaction("foo", "bar") as SentryTracer)
            // Add a user id value
            transaction.setExtra("user", "@some:user")

            val result = prepareTransactionBeforeSend(transaction)

            // The user id value should have been removed
            assertThat(result.getExtra("user")).isNull()

            // The DB sizes should be included
            assertThat(result.getExtra(AnalyticsUserData.STATE_STORE_SIZE)).isEqualTo(10)
            assertThat(result.getExtra(AnalyticsUserData.EVENT_CACHE_SIZE)).isEqualTo(11)
            assertThat(result.getExtra(AnalyticsUserData.MEDIA_STORE_SIZE)).isEqualTo(12)
            assertThat(result.getExtra(AnalyticsUserData.CRYPTO_STORE_SIZE)).isEqualTo(13)
        }
    }

    private fun createSentryAnalyticsProvider(
        buildMeta: BuildMeta = aBuildMeta(),
        getDatabaseSizesUseCase: GetDatabaseSizesUseCase = GetDatabaseSizesUseCase { Result.success(SdkStoreSizes(null, null, null, null)) },
        appNavigationStateService: FakeAppNavigationStateService = FakeAppNavigationStateService(),
    ) = SentryAnalyticsProvider(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        buildMeta = buildMeta,
        getDatabaseSizesUseCase = getDatabaseSizesUseCase,
        appNavigationStateService = appNavigationStateService,
    )
}
