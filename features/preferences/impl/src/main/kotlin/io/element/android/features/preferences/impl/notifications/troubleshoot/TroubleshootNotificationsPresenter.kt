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

package io.element.android.features.preferences.impl.notifications.troubleshoot

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
