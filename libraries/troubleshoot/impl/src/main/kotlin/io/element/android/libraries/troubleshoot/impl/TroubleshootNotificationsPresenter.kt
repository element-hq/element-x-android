/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootNavigator
import kotlinx.coroutines.launch

@AssistedInject
class TroubleshootNotificationsPresenter(
    @Assisted private val navigator: NotificationTroubleshootNavigator,
    private val troubleshootTestSuite: TroubleshootTestSuite,
) : Presenter<TroubleshootNotificationsState> {
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: NotificationTroubleshootNavigator): TroubleshootNotificationsPresenter
    }

    @Composable
    override fun present(): TroubleshootNotificationsState {
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            troubleshootTestSuite.start(this)
        }

        val testSuiteState by troubleshootTestSuite.state.collectAsState()
        fun handleEvent(event: TroubleshootNotificationsEvents) {
            when (event) {
                TroubleshootNotificationsEvents.StartTests -> coroutineScope.launch {
                    troubleshootTestSuite.runTestSuite(this)
                }
                is TroubleshootNotificationsEvents.QuickFix -> coroutineScope.launch {
                    troubleshootTestSuite.quickFix(
                        testIndex = event.testIndex,
                        coroutineScope = this,
                        navigator = navigator,
                    )
                }
                TroubleshootNotificationsEvents.RetryFailedTests -> coroutineScope.launch {
                    troubleshootTestSuite.retryFailedTest(this)
                }
            }
        }

        return TroubleshootNotificationsState(
            testSuiteState = testSuiteState,
            eventSink = ::handleEvent,
        )
    }
}
