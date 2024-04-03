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

package io.element.android.libraries.troubleshoot.impl

import im.vector.app.features.analytics.plan.NotificationTroubleshoot
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTest
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class TroubleshootTestSuite @Inject constructor(
    private val notificationTroubleshootTests: Set<@JvmSuppressWildcards NotificationTroubleshootTest>,
    private val getCurrentPushProvider: GetCurrentPushProvider,
    private val analyticsService: AnalyticsService,
) {
    lateinit var tests: List<NotificationTroubleshootTest>

    private val _state: MutableStateFlow<TroubleshootTestSuiteState> = MutableStateFlow(
        TroubleshootTestSuiteState(
            mainState = AsyncAction.Uninitialized,
            tests = emptyList<NotificationTroubleshootTestState>().toImmutableList()
        )
    )
    val state: StateFlow<TroubleshootTestSuiteState> = _state

    suspend fun start(coroutineScope: CoroutineScope) {
        val testFilterData = TestFilterData(
            currentPushProviderName = getCurrentPushProvider.getCurrentPushProvider()
        )
        tests = notificationTroubleshootTests
            .filter { it.isRelevant(testFilterData) }
            .sortedBy { it.order }
        tests.forEach {
            // Observe the state of the tests
            it.state.onEach {
                emitState()
            }.launchIn(coroutineScope)
        }
    }

    suspend fun runTestSuite(coroutineScope: CoroutineScope) {
        tests.forEach {
            it.reset()
        }
        tests.forEach {
            it.run(coroutineScope)
        }
    }

    suspend fun retryFailedTest(coroutineScope: CoroutineScope) {
        tests
            .filter { it.state.value.status is NotificationTroubleshootTestState.Status.Failure }
            .forEach {
                it.run(coroutineScope)
            }
    }

    private suspend fun emitState() {
        val states = tests.map { it.state.value }
        val mainState = states.computeMainState()
        when (mainState) {
            is AsyncAction.Success -> {
                analyticsService.capture(NotificationTroubleshoot(hasError = false))
            }
            is AsyncAction.Failure -> {
                analyticsService.capture(NotificationTroubleshoot(hasError = true))
            }
            else -> Unit
        }
        _state.emit(
            TroubleshootTestSuiteState(
                mainState = states.computeMainState(),
                tests = states.toImmutableList()
            )
        )
    }

    suspend fun quickFix(testIndex: Int, coroutineScope: CoroutineScope) {
        tests[testIndex].quickFix(coroutineScope)
    }
}

fun List<NotificationTroubleshootTestState>.computeMainState(): AsyncAction<Unit> {
    val isIdle = all { it.status is NotificationTroubleshootTestState.Status.Idle }
    val isRunning = any { it.status is NotificationTroubleshootTestState.Status.InProgress }
    return when {
        isIdle -> AsyncAction.Uninitialized
        isRunning -> AsyncAction.Loading
        else -> {
            if (any { it.status is NotificationTroubleshootTestState.Status.WaitingForUser }) {
                AsyncAction.Confirming
            } else if (any { it.status is NotificationTroubleshootTestState.Status.Failure }) {
                AsyncAction.Failure(Exception("Some tests failed"))
            } else {
                AsyncAction.Success(Unit)
            }
        }
    }
}
