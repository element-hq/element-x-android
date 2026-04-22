/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import io.element.android.features.call.impl.pip.aPictureInPictureState
import io.element.android.features.call.impl.ui.CallScreenEvents
import io.element.android.features.call.impl.ui.CallScreenView
import io.element.android.features.call.impl.ui.aCallScreenState
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CallScreenViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `pressing back key triggers hangup when no web view is available and pip is unsupported`() {
        val callEvents = EventsRecorder<CallScreenEvents>()

        rule.setCallScreenView(
            state = aCallScreenState(eventSink = callEvents),
        )

        rule.pressBackKey()

        callEvents.assertSingle(CallScreenEvents.Hangup)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setCallScreenView(
    state: io.element.android.features.call.impl.ui.CallScreenState,
) {
    setContent {
        // Force inspection mode so AndroidView is not created and BackHandler goes through native fallback.
        CompositionLocalProvider(LocalInspectionMode provides true) {
            CallScreenView(
                state = state,
                pipState = aPictureInPictureState(supportPip = false),
                onConsoleMessage = {},
                requestPermissions = { _, _ -> },
            )
        }
    }
}

