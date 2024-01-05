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

package io.element.android.features.logout.impl

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.tests.testutils.EnsureCalledOnce
import io.element.android.tests.testutils.EnsureCalledOnceWithParam
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutViewTest {

    @get:Rule val rule = createComposeRule()

    @Test
    fun `clicking on logout sends a LogoutEvents`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        rule.setContent {
            LogoutView(
                aLogoutState(
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClicked = EnsureNeverCalled(),
                onBackClicked = EnsureNeverCalled(),
                onSuccessLogout = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn("Sign out")
        eventsRecorder.assertSingle(LogoutEvents.Logout(false))
    }

    @Test
    fun `clicking on back invoke back callback`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>(expectEvents = false)
        val callback = EnsureCalledOnce()
        rule.setContent {
            LogoutView(
                aLogoutState(
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClicked = EnsureNeverCalled(),
                onBackClicked = callback,
                onSuccessLogout = EnsureNeverCalledWithParam(),
            )
        }
        rule.onNode(hasContentDescription("Back")).performClick()
        callback.assertSuccess()
    }

    @Test
    fun `clicking on confirm after error sends a LogoutEvents`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        rule.setContent {
            LogoutView(
                aLogoutState(
                    logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClicked = EnsureNeverCalled(),
                onBackClicked = EnsureNeverCalled(),
                onSuccessLogout = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn("Sign out anyway")
        eventsRecorder.assertSingle(LogoutEvents.Logout(true))
    }

    @Test
    fun `clicking on cancel after error sends a LogoutEvents`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>()
        rule.setContent {
            LogoutView(
                aLogoutState(
                    logoutAction = AsyncAction.Failure(Exception("Failed to logout")),
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClicked = EnsureNeverCalled(),
                onBackClicked = EnsureNeverCalled(),
                onSuccessLogout = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn("Cancel")
        eventsRecorder.assertSingle(LogoutEvents.CloseDialogs)
    }

    @Test
    fun `success logout invoke onSuccessLogout`() {
        val data = "data"
        val eventsRecorder = EventsRecorder<LogoutEvents>(expectEvents = false)
        val callback = EnsureCalledOnceWithParam<String?>(data)
        rule.setContent {
            LogoutView(
                aLogoutState(
                    logoutAction = AsyncAction.Success(data),
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClicked = EnsureNeverCalled(),
                onBackClicked = EnsureNeverCalled(),
                onSuccessLogout = callback,
            )
        }
        callback.assertSuccess()
    }

    @Test
    fun `last session setting button invoke onChangeRecoveryKeyClicked`() {
        val eventsRecorder = EventsRecorder<LogoutEvents>(expectEvents = false)
        val callback = EnsureCalledOnce()
        rule.setContent {
            LogoutView(
                aLogoutState(
                    isLastSession = true,
                    eventSink = eventsRecorder
                ),
                onChangeRecoveryKeyClicked = callback,
                onBackClicked = EnsureNeverCalled(),
                onSuccessLogout = EnsureNeverCalledWithParam(),
            )
        }
        rule.clickOn("Settings")
        callback.assertSuccess()
    }
}
