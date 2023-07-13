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

package io.element.android.features.ftue.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.analytics.test.FakeAnalyticsService
import io.element.android.features.ftue.api.state.FtueStep
import io.element.android.features.ftue.impl.state.DefaultFtueState
import io.element.android.features.ftue.impl.welcome.state.FakeWelcomeState
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFtueStateTests {

    @Test
    fun `given any check being false, should display flow is true`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val state = createState(coroutineScope)

        assertThat(state.shouldDisplayFlow.value).isTrue()

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `given all checks being true, should display flow is false`() = runTest {
        val welcomeState = FakeWelcomeState()
        val analyticsService = FakeAnalyticsService()
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

        val state = createState(coroutineScope, welcomeState, analyticsService)

        welcomeState.setWelcomeScreenShown()
        analyticsService.setDidAskUserConsent()
        state.updateState()

        assertThat(state.shouldDisplayFlow.value).isFalse()

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `traverse flow`() = runTest {
        val welcomeState = FakeWelcomeState()
        val analyticsService = FakeAnalyticsService()
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

        val state = createState(coroutineScope, welcomeState, analyticsService)
        val steps = mutableListOf<FtueStep?>()

        // First step, welcome screen
        steps.add(state.getNextStep(steps.lastOrNull()))
        welcomeState.setWelcomeScreenShown()

        // Second step, analytics opt in
        steps.add(state.getNextStep(steps.lastOrNull()))
        analyticsService.setDidAskUserConsent()

        // Final step (null)
        steps.add(state.getNextStep(steps.lastOrNull()))

        assertThat(steps).containsExactly(
            FtueStep.WelcomeScreen,
            FtueStep.AnalyticsOptIn,
            null, // Final state
        )

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `if a check for a step is true, start from the next one`() = runTest {
        val welcomeState = FakeWelcomeState().apply { setWelcomeScreenShown() }
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

        val state = createState(coroutineScope, welcomeState)

        assertThat(state.getNextStep()).isEqualTo(FtueStep.AnalyticsOptIn)

        // Cleanup
        coroutineScope.cancel()
    }

    private fun TestScope.createState(
        coroutineScope: CoroutineScope,
        welcomeState: FakeWelcomeState = FakeWelcomeState(),
        analyticsService: AnalyticsService = FakeAnalyticsService()
    ) = DefaultFtueState(coroutineScope, analyticsService, welcomeState)

}
