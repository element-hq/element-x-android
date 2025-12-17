/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.posthog

import com.google.common.truth.Truth.assertThat
import com.posthog.PostHogInterface
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.plan.MobileScreen
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.tests.testutils.WarmUpRule
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PosthogAnalyticsProviderTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `Posthog - Test user properties`() = runTest {
        val mockPosthog = mockk<PostHogInterface>().also {
            every { it.optIn() } just runs
            every { it.capture(any(), any(), any(), any(), any(), any()) } just runs
        }
        val mockPosthogFactory = mockk<PostHogFactory>()
        every { mockPosthogFactory.createPosthog() } returns mockPosthog

        val analyticsProvider = PosthogAnalyticsProvider(mockPosthogFactory)
        analyticsProvider.init()

        val testUserProperties = UserProperties(
            verificationState = UserProperties.VerificationState.Verified,
            recoveryState = UserProperties.RecoveryState.Incomplete,
        )
        analyticsProvider.updateUserProperties(testUserProperties)

        // report mock event
        val mockEvent = mockk<VectorAnalyticsEvent>().also {
            every {
                it.getProperties()
            } returns emptyMap()
            every { it.getName() } returns "MockEventName"
        }
        analyticsProvider.capture(mockEvent)
        val userPropertiesSlot = slot<Map<String, Any>>()

        verify { mockPosthog.capture(event = "MockEventName", any(), any(), userProperties = capture(userPropertiesSlot)) }

        assertThat(userPropertiesSlot.captured).isNotNull()
        assertThat(userPropertiesSlot.captured["verificationState"] as String).isEqualTo(testUserProperties.verificationState?.name)
        assertThat(userPropertiesSlot.captured["recoveryState"] as String).isEqualTo(testUserProperties.recoveryState?.name)

        // Should only be reported once when the next event is sent
        // Try another capture to check
        analyticsProvider.capture(mockEvent)
        verify { mockPosthog.capture(any(), any(), any(), userProperties = null) }
    }

    @Test
    fun `Posthog - Test accumulate user properties until next capture call`() = runTest {
        val mockPosthog = mockk<PostHogInterface>().also {
            every { it.optIn() } just runs
            every { it.capture(any(), any(), any(), any(), any(), any()) } just runs
        }
        val mockPosthogFactory = mockk<PostHogFactory>()
        every { mockPosthogFactory.createPosthog() } returns mockPosthog

        val analyticsProvider = PosthogAnalyticsProvider(mockPosthogFactory)
        analyticsProvider.init()

        val testUserProperties = UserProperties(
            verificationState = UserProperties.VerificationState.NotVerified,
        )
        analyticsProvider.updateUserProperties(testUserProperties)

        // Update again
        val testUserPropertiesUpdate = UserProperties(
            verificationState = UserProperties.VerificationState.Verified,
        )
        analyticsProvider.updateUserProperties(testUserPropertiesUpdate)

        // report mock event
        val mockEvent = mockk<VectorAnalyticsEvent>().also {
            every {
                it.getProperties()
            } returns emptyMap()
            every { it.getName() } returns "MockEventName"
        }
        analyticsProvider.capture(mockEvent)
        val userPropertiesSlot = slot<Map<String, Any>>()

        verify { mockPosthog.capture(event = "MockEventName", any(), any(), userProperties = capture(userPropertiesSlot)) }

        assertThat(userPropertiesSlot.captured).isNotNull()
        assertThat(userPropertiesSlot.captured["verificationState"] as String).isEqualTo(testUserPropertiesUpdate.verificationState?.name)
    }

    @Test
    fun `Posthog - Test super properties added to all captured events`() = runTest {
        val mockPosthog = mockk<PostHogInterface>().also {
            every { it.optIn() } just runs
            every { it.capture(any(), any(), any(), any(), any(), any()) } just runs
            every { it.screen(any(), any()) } just runs
        }
        val mockPosthogFactory = mockk<PostHogFactory>()
        every { mockPosthogFactory.createPosthog() } returns mockPosthog

        val analyticsProvider = PosthogAnalyticsProvider(mockPosthogFactory)
        analyticsProvider.init()

        val testSuperProperties = SuperProperties(
            appPlatform = SuperProperties.AppPlatform.EXA,
        )
        analyticsProvider.updateSuperProperties(testSuperProperties)

        // Test with events having different sort of properties
        listOf(
            mapOf("foo" to "bar"),
            mapOf("a" to "aValue1", "b" to "aValue2"),
            null
        ).forEach { eventProperties ->
            // report an event with properties
            val mockEvent = mockk<VectorAnalyticsEvent>().also {
                every {
                    it.getProperties()
                } returns eventProperties
                every { it.getName() } returns "MockEventName"
            }

            analyticsProvider.capture(mockEvent)

            val expectedProperties = eventProperties.orEmpty() + testSuperProperties.getProperties().orEmpty()
            verify { mockPosthog.capture(event = "MockEventName", any(), properties = expectedProperties, any()) }
        }

        // / Test it is also added to screens
        val screenEvent = MobileScreen(null, MobileScreen.ScreenName.Home)
        analyticsProvider.screen(screenEvent)
        verify { mockPosthog.screen(MobileScreen.ScreenName.Home.rawValue, testSuperProperties.getProperties()) }
    }

    @Test
    fun `Posthog - Test super properties can be updated`() = runTest {
        val mockPosthog = mockk<PostHogInterface>().also {
            every { it.optIn() } just runs
            every { it.capture(any(), any(), any(), any(), any(), any()) } just runs
        }
        val mockPosthogFactory = mockk<PostHogFactory>()
        every { mockPosthogFactory.createPosthog() } returns mockPosthog

        val analyticsProvider = PosthogAnalyticsProvider(mockPosthogFactory)
        analyticsProvider.init()

        // Test with events having different sort of aggregation
        // left is the updated properties, right is the expected aggregated state
        mapOf(
            SuperProperties(appPlatform = SuperProperties.AppPlatform.EXA) to SuperProperties(appPlatform = SuperProperties.AppPlatform.EXA),
            SuperProperties(cryptoSDKVersion = "0.0") to SuperProperties(appPlatform = SuperProperties.AppPlatform.EXA, cryptoSDKVersion = "0.0"),
            SuperProperties(cryptoSDKVersion = "1.0") to SuperProperties(appPlatform = SuperProperties.AppPlatform.EXA, cryptoSDKVersion = "1.0"),
            SuperProperties(cryptoSDK = SuperProperties.CryptoSDK.Rust) to SuperProperties(
                appPlatform = SuperProperties.AppPlatform.EXA,
                cryptoSDKVersion = "1.0",
                cryptoSDK = SuperProperties.CryptoSDK.Rust
            ),
        ).entries.forEach { (updated, expected) ->
            // report an event with properties
            val mockEvent = mockk<VectorAnalyticsEvent>().also {
                every {
                    it.getProperties()
                } returns null
                every { it.getName() } returns "MockEventName"
            }

            analyticsProvider.updateSuperProperties(updated)
            analyticsProvider.capture(mockEvent)

            verify { mockPosthog.capture(event = "MockEventName", any(), properties = expected.getProperties(), any()) }
        }
    }

    @Test
    fun `Posthog - Test super properties do not override property with same name on the event`() = runTest {
        val mockPosthog = mockk<PostHogInterface>().also {
            every { it.optIn() } just runs
            every { it.capture(any(), any(), any(), any(), any(), any()) } just runs
        }
        val mockPosthogFactory = mockk<PostHogFactory>()
        every { mockPosthogFactory.createPosthog() } returns mockPosthog

        val analyticsProvider = PosthogAnalyticsProvider(mockPosthogFactory)
        analyticsProvider.init()

        // report an event with properties
        val mockEvent = mockk<VectorAnalyticsEvent>().also {
            every {
                it.getProperties()
            } returns mapOf("appPlatform" to SuperProperties.AppPlatform.Other)
            every { it.getName() } returns "MockEventName"
        }

        analyticsProvider.updateSuperProperties(SuperProperties(appPlatform = SuperProperties.AppPlatform.EXA))
        analyticsProvider.capture(mockEvent)

        verify { mockPosthog.capture(event = "MockEventName", any(), properties = mapOf("appPlatform" to SuperProperties.AppPlatform.Other), any()) }
    }
}
