/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.sessionverification.choosemode

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.ftue.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class ChooseSessionVerificationModeViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on learn more invokes the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(),
                onLearnMoreClick = callback,
            )
            rule.clickOn(CommonStrings.action_learn_more)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on use another device calls the callback`() {
        ensureCalledOnce { callback ->
            rule.setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(AsyncData.Success(aButtonsState(canUseAnotherDevice = true))),
                onUseAnotherDevice = callback,
            )
            rule.clickOn(R.string.screen_identity_use_another_device)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on enter recovery key calls the callback`() {
        ensureCalledOnce { callback ->
            rule.setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(AsyncData.Success(aButtonsState(canEnterRecoveryKey = true))),
                onEnterRecoveryKey = callback,
            )
            rule.clickOn(R.string.screen_session_verification_enter_recovery_key)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on cannot confirm calls the reset keys callback`() {
        ensureCalledOnce { callback ->
            rule.setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(),
                onResetKey = callback,
            )
            rule.clickOn(R.string.screen_identity_confirmation_cannot_confirm)
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChooseSelfVerificationModeView(
        state: ChooseSelfVerificationModeState,
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onUseAnotherDevice: () -> Unit = EnsureNeverCalled(),
        onResetKey: () -> Unit = EnsureNeverCalled(),
        onEnterRecoveryKey: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            ChooseSelfVerificationModeView(
                state = state,
                onLearnMore = onLearnMoreClick,
                onUseAnotherDevice = onUseAnotherDevice,
                onResetKey = onResetKey,
                onUseRecoveryKey = onEnterRecoveryKey,
            )
        }
    }
}
