/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch
import javax.inject.Inject

class TroubleshootNotificationsPresenter @Inject constructor(
    private val troubleshootTestSuite: TroubleshootTestSuite,
) : Presenter<TroubleshootNotificationsState> {
    @Composable
    override fun present(): TroubleshootNotificationsState {
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            troubleshootTestSuite.start(this)
        }

        val testSuiteState by troubleshootTestSuite.state.collectAsState()
        fun handleEvents(event: TroubleshootNotificationsEvents) {
            when (event) {
                TroubleshootNotificationsEvents.StartTests -> coroutineScope.launch {
                    troubleshootTestSuite.runTestSuite(this)
                }
                is TroubleshootNotificationsEvents.QuickFix -> coroutineScope.launch {
                    troubleshootTestSuite.quickFix(event.testIndex, this)
                }
                TroubleshootNotificationsEvents.RetryFailedTests -> coroutineScope.launch {
                    troubleshootTestSuite.retryFailedTest(this)
                }
            }
        }

        return TroubleshootNotificationsState(
            testSuiteState = testSuiteState,
            eventSink = ::handleEvents
        )
    }
}
