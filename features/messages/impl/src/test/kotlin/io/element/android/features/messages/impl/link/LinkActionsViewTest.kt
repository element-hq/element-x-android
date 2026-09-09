/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.link

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class LinkActionsViewTest : RobolectricTest() {
    @Test
    fun `clicking on open with invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setContent {
                LinkActionsViewContent(
                    onOpenWithClick = callback,
                    onShareClick = EnsureNeverCalled(),
                    onCopyClick = EnsureNeverCalled(),
                )
            }
            clickOn(CommonStrings.action_open_with)
        }
    }

    @Test
    fun `clicking on share invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setContent {
                LinkActionsViewContent(
                    onOpenWithClick = EnsureNeverCalled(),
                    onShareClick = callback,
                    onCopyClick = EnsureNeverCalled(),
                )
            }
            clickOn(CommonStrings.action_share_link)
        }
    }

    @Test
    fun `clicking on copy invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setContent {
                LinkActionsViewContent(
                    onOpenWithClick = EnsureNeverCalled(),
                    onShareClick = EnsureNeverCalled(),
                    onCopyClick = callback,
                )
            }
            clickOn(CommonStrings.action_copy_link)
        }
    }
}
