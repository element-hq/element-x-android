/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.securityandprivacy

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdetails.impl.securityandprivacy.SecurityAndPrivacyState
import io.element.android.features.roomdetails.impl.securityandprivacy.SecurityAndPrivacyView
import io.element.android.features.roomdetails.impl.securityandprivacy.aSecurityAndPrivacyState
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityAndPrivacyPresenterViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setSecurityAndPrivacyView(
                onBackClick = callback,
            )
            rule.pressBack()
        }
    }

}


private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setSecurityAndPrivacyView(
    state: SecurityAndPrivacyState = aSecurityAndPrivacyState(
        eventSink = EventsRecorder(expectEvents = false),
    ),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        SecurityAndPrivacyView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
