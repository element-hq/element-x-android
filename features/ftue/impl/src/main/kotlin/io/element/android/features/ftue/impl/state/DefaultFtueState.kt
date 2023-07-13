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

package io.element.android.features.ftue.impl.state

import androidx.annotation.VisibleForTesting
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.ftue.api.state.FtueStep
import io.element.android.features.ftue.impl.welcome.state.WelcomeScreenState
import io.element.android.libraries.di.AppScope
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultFtueState @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService,
    private val welcomeScreenState: WelcomeScreenState,
) : FtueState {

    private val _shouldDisplayFlow = MutableStateFlow(isAnyStepInComplete())
    override val shouldDisplayFlow: StateFlow<Boolean> = _shouldDisplayFlow

    init {
        analyticsService.didAskUserConsent()
            .onEach { updateState() }
            .launchIn(coroutineScope)
    }

    override fun getNextStep(currentStep: FtueStep?): FtueStep? =
        when (currentStep) {
            null -> if (shouldDisplayWelcomeScreen()) FtueStep.WelcomeScreen else getNextStep(
                FtueStep.WelcomeScreen
            )
            FtueStep.WelcomeScreen -> if (needsAnalyticsOptIn()) FtueStep.AnalyticsOptIn else getNextStep(
                FtueStep.AnalyticsOptIn
            )
            FtueStep.AnalyticsOptIn -> null
        }

    private fun isAnyStepInComplete(): Boolean {
        return listOf(
            shouldDisplayWelcomeScreen(),
            needsAnalyticsOptIn()
        ).any { it }
    }

    private fun needsAnalyticsOptIn(): Boolean {
        // We need this function to not be suspend, so we need to load the value through runBlocking
        return runBlocking { analyticsService.didAskUserConsent().first().not() }
    }

    private fun shouldDisplayWelcomeScreen(): Boolean {
        return welcomeScreenState.isWelcomeScreenNeeded()
    }

    override fun setWelcomeScreenShown() {
        welcomeScreenState.setWelcomeScreenShown()
        updateState()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun updateState() {
        _shouldDisplayFlow.value = isAnyStepInComplete()
    }
}
