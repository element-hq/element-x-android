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
import io.element.android.features.ftue.impl.migration.MigrationScreenStore
import io.element.android.features.ftue.impl.welcome.state.WelcomeScreenState
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultFtueState @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService,
    private val welcomeScreenState: WelcomeScreenState,
    private val migrationScreenStore: MigrationScreenStore,
    private val matrixClient: MatrixClient,
) : FtueState {

    override val shouldDisplayFlow = MutableStateFlow(isAnyStepIncomplete())

    override suspend fun reset() {
        welcomeScreenState.reset()
        analyticsService.reset()
        migrationScreenStore.reset()
    }

    init {
        analyticsService.didAskUserConsent()
            .onEach { updateState() }
            .launchIn(coroutineScope)
    }

    fun getNextStep(currentStep: FtueStep? = null): FtueStep? =
        when (currentStep) {
            null -> if (shouldDisplayMigrationScreen()) FtueStep.MigrationScreen else getNextStep(
                FtueStep.MigrationScreen
            )
            FtueStep.MigrationScreen -> if (shouldDisplayWelcomeScreen()) FtueStep.WelcomeScreen else getNextStep(
                FtueStep.WelcomeScreen
            )
            FtueStep.WelcomeScreen -> if (needsAnalyticsOptIn()) FtueStep.AnalyticsOptIn else getNextStep(
                FtueStep.AnalyticsOptIn
            )
            FtueStep.AnalyticsOptIn -> null
        }

    private fun isAnyStepIncomplete(): Boolean {
        return listOf(
            shouldDisplayMigrationScreen(),
            shouldDisplayWelcomeScreen(),
            needsAnalyticsOptIn()
        ).any { it }
    }

    private fun shouldDisplayMigrationScreen(): Boolean {
        return migrationScreenStore.isMigrationScreenNeeded(matrixClient.sessionId)
    }

    private fun needsAnalyticsOptIn(): Boolean {
        // We need this function to not be suspend, so we need to load the value through runBlocking
        return runBlocking { analyticsService.didAskUserConsent().first().not() }
    }

    private fun shouldDisplayWelcomeScreen(): Boolean {
        return welcomeScreenState.isWelcomeScreenNeeded()
    }

    fun setWelcomeScreenShown() {
        welcomeScreenState.setWelcomeScreenShown()
        updateState()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun updateState() {
        shouldDisplayFlow.value = isAnyStepIncomplete()
    }
}

sealed interface FtueStep {
    data object MigrationScreen : FtueStep
    data object WelcomeScreen : FtueStep
    data object AnalyticsOptIn : FtueStep
}
