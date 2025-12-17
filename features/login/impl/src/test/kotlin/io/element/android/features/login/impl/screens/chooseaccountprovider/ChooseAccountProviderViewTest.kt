/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.OidcDetails
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ChooseAccountProviderViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventSink = EventsRecorder<ChooseAccountProviderEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setChooseAccountProviderView(
                state = aChooseAccountProviderState(
                    eventSink = eventSink,
                ),
                onBackClick = it,
            )
            rule.pressBack()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `selecting an account provider emits the the expected event`() {
        val eventSink = EventsRecorder<ChooseAccountProviderEvents>()
        rule.setChooseAccountProviderView(
            state = aChooseAccountProviderState(
                accountProviders = listOf(
                    ChooseAccountProviderPresenterTest.accountProvider1,
                    ChooseAccountProviderPresenterTest.accountProvider2,
                ),
                selectedAccountProvider = anAccountProvider(),
                eventSink = eventSink,
            ),
        )
        rule.onNodeWithText(ChooseAccountProviderPresenterTest.accountProvider1.title).performClick()
        eventSink.assertSingle(ChooseAccountProviderEvents.SelectAccountProvider(ChooseAccountProviderPresenterTest.accountProvider1))
    }

    @Test
    fun `when error is displayed - closing the dialog emits the expected event`() {
        val eventSink = EventsRecorder<ChooseAccountProviderEvents>()
        rule.setChooseAccountProviderView(
            state = aChooseAccountProviderState(
                loginMode = AsyncData.Failure(AN_EXCEPTION),
                eventSink = eventSink,
            ),
        )
        rule.clickOn(CommonStrings.action_ok)
        eventSink.assertSingle(ChooseAccountProviderEvents.ClearError)
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChooseAccountProviderView(
        state: ChooseAccountProviderState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onOidcDetails: (OidcDetails) -> Unit = EnsureNeverCalledWithParam(),
        onNeedLoginPassword: () -> Unit = EnsureNeverCalled(),
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onCreateAccountContinue: (url: String) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            ChooseAccountProviderView(
                state = state,
                onBackClick = onBackClick,
                onOidcDetails = onOidcDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onLearnMoreClick = onLearnMoreClick,
                onCreateAccountContinue = onCreateAccountContinue,
            )
        }
    }
}
