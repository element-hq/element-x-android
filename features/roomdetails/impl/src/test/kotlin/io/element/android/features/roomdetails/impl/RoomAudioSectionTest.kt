/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.roomdetails.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class RoomAudioSectionTest : RobolectricTest() {
    @Test
    fun `video mode shows selection and allows selecting PTT`() = runAndroidComposeUiTest<ComponentActivity> {
        ensureCalledOnceWithParam(true) { callback ->
            setContent {
                ElementPreview {
                    RoomAudioSection(isPttEnabled = false, onPttEnabledChange = callback)
                }
            }
            onNodeWithText("Video Calls").assertIsSelected()
            onNodeWithText("Push to Talk").assertIsNotSelected().performClick()
        }
    }

    @Test
    fun `PTT mode shows selection and allows selecting video`() = runAndroidComposeUiTest<ComponentActivity> {
        ensureCalledOnceWithParam(false) { callback ->
            setContent {
                ElementPreview {
                    RoomAudioSection(isPttEnabled = true, onPttEnabledChange = callback)
                }
            }
            onNodeWithText("Push to Talk").assertIsSelected()
            onNodeWithText("Video Calls").assertIsNotSelected().performClick()
        }
    }
}
