/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.services.analyticsproviders.posthog

import com.google.common.truth.Truth.assertThat
import com.posthog.PostHogInterface
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
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
}
