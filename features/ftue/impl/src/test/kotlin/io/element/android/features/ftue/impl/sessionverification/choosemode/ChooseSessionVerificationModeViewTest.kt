/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.ftue.impl.sessionverification.choosemode

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.ftue.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.annotation.Config

class ChooseSessionVerificationModeViewTest : RobolectricTest() {
    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on learn more invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(),
                onLearnMoreClick = callback,
            )
            clickOn(CommonStrings.action_learn_more)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on use another device calls the callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(AsyncData.Success(aButtonsState(canUseAnotherDevice = true))),
                onUseAnotherDevice = callback,
            )
            clickOn(R.string.screen_identity_use_another_device)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on enter recovery key calls the callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(AsyncData.Success(aButtonsState(canUseRecoveryKey = true))),
                onEnterRecoveryKey = callback,
            )
            clickOn(R.string.screen_identity_confirmation_use_recovery_key)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on cannot confirm calls the reset keys callback`() = runAndroidComposeUiTest {
        ensureCalledOnceWithParam(true) { callback ->
            setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(),
                onResetKey = callback,
            )
            clickOn(R.string.screen_identity_confirmation_cannot_confirm)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on cannot confirm without any other option calls the reset keys callback with no options`() = runAndroidComposeUiTest {
        ensureCalledOnceWithParam(false) { callback ->
            setChooseSelfVerificationModeView(
                aChooseSelfVerificationModeState(AsyncData.Success(aButtonsState(canUseAnotherDevice = false, canUseRecoveryKey = false))),
                onResetKey = callback,
            )
            clickOn(R.string.screen_identity_confirmation_cannot_confirm)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `unavailable options are displayed but disabled`() = runAndroidComposeUiTest {
        setChooseSelfVerificationModeView(
            aChooseSelfVerificationModeState(AsyncData.Success(aButtonsState(canUseAnotherDevice = false, canUseRecoveryKey = false))),
        )
        val context = activity!!
        val useAnotherDevice = context.getString(R.string.screen_identity_use_another_device)
        val useRecoveryKey = context.getString(R.string.screen_identity_confirmation_use_recovery_key)
        val cannotConfirm = context.getString(R.string.screen_identity_confirmation_cannot_confirm)
        onNodeWithText(useAnotherDevice).assertIsNotEnabled()
        onNodeWithText(useRecoveryKey).assertIsNotEnabled()
        onNodeWithText(cannotConfirm).assertIsEnabled()
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setChooseSelfVerificationModeView(
        state: ChooseSelfVerificationModeState,
        onLearnMoreClick: () -> Unit = EnsureNeverCalled(),
        onUseAnotherDevice: () -> Unit = EnsureNeverCalled(),
        onResetKey: (Boolean) -> Unit = EnsureNeverCalledWithParam(),
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
