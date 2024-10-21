/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl.direct

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBackKey
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultDirectLogoutViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on confirm logout sends expected Event`() {
        val eventsRecorder = EventsRecorder<DirectLogoutEvents>()
        rule.setDefaultDirectLogoutView(
            state = aDirectLogoutState(
                logoutAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder,
            )
        )
        rule.clickOn(CommonStrings.action_signout)
        eventsRecorder.assertSingle(DirectLogoutEvents.Logout(false))
    }

    @Test
    fun `clicking on cancel logout sends expected Event`() {
        val eventsRecorder = EventsRecorder<DirectLogoutEvents>()
        rule.setDefaultDirectLogoutView(
            state = aDirectLogoutState(
                logoutAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder,
            )
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(DirectLogoutEvents.CloseDialogs)
    }

    @Ignore("Pressing back key should dismiss the dialog, and so generate the expected event, but it's not the case.")
    @Test
    fun `clicking on back invoke back callback`() {
        val eventsRecorder = EventsRecorder<DirectLogoutEvents>()
        rule.setDefaultDirectLogoutView(
            state = aDirectLogoutState(
                logoutAction = AsyncAction.ConfirmingNoParams,
                eventSink = eventsRecorder,
            )
        )
        rule.pressBackKey()
        eventsRecorder.assertSingle(DirectLogoutEvents.CloseDialogs)
    }

    @Test
    fun `clicking on confirm after error sends expected Event`() {
        val eventsRecorder = EventsRecorder<DirectLogoutEvents>()
        rule.setDefaultDirectLogoutView(
            state = aDirectLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            )
        )
        rule.clickOn(CommonStrings.action_signout_anyway)
        eventsRecorder.assertSingle(DirectLogoutEvents.Logout(true))
    }

    @Test
    fun `clicking on cancel after error sends expected Event`() {
        val eventsRecorder = EventsRecorder<DirectLogoutEvents>()
        rule.setDefaultDirectLogoutView(
            state = aDirectLogoutState(
                logoutAction = AsyncAction.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            )
        )
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(DirectLogoutEvents.CloseDialogs)
    }

    @Test
    fun `success logout invoke expected callback and sends expected Event`() {
        val eventsRecorder = EventsRecorder<DirectLogoutEvents>(expectEvents = false)
        ensureCalledOnceWithParam<String?>(null) { callback ->
            rule.setDefaultDirectLogoutView(
                state = aDirectLogoutState(
                    logoutAction = AsyncAction.Success(null),
                    eventSink = eventsRecorder,
                ),
                onSuccessLogout = callback
            )
        }
    }

    @Test
    fun `success logout invoke expected callback and sends expected Event with data`() {
        val eventsRecorder = EventsRecorder<DirectLogoutEvents>(expectEvents = false)
        val data = "data"
        ensureCalledOnceWithParam<String?>(data) { callback ->
            rule.setDefaultDirectLogoutView(
                state = aDirectLogoutState(
                    logoutAction = AsyncAction.Success(data),
                    eventSink = eventsRecorder,
                ),
                onSuccessLogout = callback
            )
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setDefaultDirectLogoutView(
    state: DirectLogoutState,
    onSuccessLogout: (String?) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        DefaultDirectLogoutView().Render(
            state,
            onSuccessLogout = onSuccessLogout,
        )
    }
}
