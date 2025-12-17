/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock.keypad

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PinKeypadTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on a number emits the expected event`() {
        val eventsRecorder = EventsRecorder<PinKeypadModel>()
        rule.setPinKeyPad(onClick = eventsRecorder)
        rule.onNode(hasText("1")).performClick()
        eventsRecorder.assertSingle(PinKeypadModel.Number('1'))
    }

    @Test
    fun `clicking on the delete previous character button emits the expected event`() {
        val eventsRecorder = EventsRecorder<PinKeypadModel>()
        rule.setPinKeyPad(onClick = eventsRecorder)
        rule.onNode(hasContentDescription(rule.activity.getString(CommonStrings.a11y_delete))).performClick()
        eventsRecorder.assertSingle(PinKeypadModel.Back)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `typing using the hardware keyboard emits the expected events`() {
        val eventsRecorder = EventsRecorder<PinKeypadModel>()
        rule.setPinKeyPad(onClick = eventsRecorder)
        rule.onNodeWithText("1").requestFocus()
        rule.onAllNodes(isRoot())[0].performKeyInput {
            val keys = listOf(
                Key.A,
                Key.NumPad1,
                Key.NumPad2,
                Key.NumPad3,
                Key.NumPad4,
                Key.NumPad5,
                Key.NumPad6,
                Key.NumPad7,
                Key.NumPad8,
                Key.NumPad9,
                Key.NumPad0,
                Key(KeyEvent.KEYCODE_1),
                Key(KeyEvent.KEYCODE_2),
                Key(KeyEvent.KEYCODE_3),
                Key(KeyEvent.KEYCODE_4),
                Key(KeyEvent.KEYCODE_5),
                Key(KeyEvent.KEYCODE_6),
                Key(KeyEvent.KEYCODE_7),
                Key(KeyEvent.KEYCODE_8),
                Key(KeyEvent.KEYCODE_9),
                Key(KeyEvent.KEYCODE_0),
                Key.Backspace,
            )
            for (key in keys) {
                pressKey(key)
            }
        }
        eventsRecorder.assertList(
            listOf(
                // Note that the first key is not a number, but a letter so it's ignored as input
                // Then we have the numpad keys
                PinKeypadModel.Number('1'),
                PinKeypadModel.Number('2'),
                PinKeypadModel.Number('3'),
                PinKeypadModel.Number('4'),
                PinKeypadModel.Number('5'),
                PinKeypadModel.Number('6'),
                PinKeypadModel.Number('7'),
                PinKeypadModel.Number('8'),
                PinKeypadModel.Number('9'),
                PinKeypadModel.Number('0'),
                // And the normal keys from the number row in the keyboard
                PinKeypadModel.Number('1'),
                PinKeypadModel.Number('2'),
                PinKeypadModel.Number('3'),
                PinKeypadModel.Number('4'),
                PinKeypadModel.Number('5'),
                PinKeypadModel.Number('6'),
                PinKeypadModel.Number('7'),
                PinKeypadModel.Number('8'),
                PinKeypadModel.Number('9'),
                PinKeypadModel.Number('0'),
                PinKeypadModel.Back,
            )
        )
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setPinKeyPad(
        onClick: (PinKeypadModel) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            PinKeypad(
                onClick = onClick,
                maxWidth = 1000.dp,
                maxHeight = 1000.dp,
            )
        }
    }
}
