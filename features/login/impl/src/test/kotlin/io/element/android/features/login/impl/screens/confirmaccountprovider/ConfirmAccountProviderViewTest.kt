/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.login.impl.screens.confirmaccountprovider

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.matrix.api.auth.OAuthDetails
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class ConfirmAccountProviderViewTest : RobolectricTest() {
    @Test
    fun `clicking on back invokes the expected callback`() = runAndroidComposeUiTest {
        val eventSink = EventsRecorder<ConfirmAccountProviderEvent>(expectEvents = false)
        ensureCalledOnce {
            setConfirmAccountProviderView(
                state = aConfirmAccountProviderState(
                    eventSink = eventSink,
                ),
                onBackClick = it,
            )
            pressBack()
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setConfirmAccountProviderView(
        state: ConfirmAccountProviderState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onOAuthDetails: (OAuthDetails) -> Unit = EnsureNeverCalledWithParam(),
        onNeedLoginPassword: () -> Unit = EnsureNeverCalled(),
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            ConfirmAccountProviderView(
                state = state,
                onBackClick = onBackClick,
                onOAuthDetails = onOAuthDetails,
                onNeedLoginPassword = onNeedLoginPassword,
                onLearnMoreClick = onLearnMoreClick,
            )
        }
    }
}
