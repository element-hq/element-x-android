/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.messages.impl.timeline.sendfailure

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.robolectric.RobolectricTest
import io.element.android.tests.testutils.setSafeContent
import org.junit.Test

class SendFailureDialogViewTest : RobolectricTest() {
    @Test
    fun `hidden state renders nothing`() = runAndroidComposeUiTest {
        setSendFailureDialogView(
            sendFailureDialogState = SendFailureDialogState.Hidden,
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_sending_failed)).assertDoesNotExist()
    }

    @Test
    fun `show state renders the failure reason`() = runAndroidComposeUiTest {
        setSendFailureDialogView(
            sendFailureDialogState = aSendFailureDialogStateShow(message = "A failure reason"),
        )
        onNodeWithText(activity!!.getString(CommonStrings.common_sending_failed)).assertExists()
        onNodeWithText("A failure reason").assertExists()
    }

    @Test
    fun `clicking on retry invokes the expected callback`() = runAndroidComposeUiTest {
        val state = aSendFailureDialogStateShow()
        ensureCalledOnceWithParam(state.event) { callback ->
            setSendFailureDialogView(
                sendFailureDialogState = state,
                onRetry = callback,
            )
            clickOn(CommonStrings.action_retry)
        }
    }

    @Test
    fun `clicking on remove message invokes the expected callback`() = runAndroidComposeUiTest {
        val state = aSendFailureDialogStateShow()
        ensureCalledOnceWithParam(state.event) { callback ->
            setSendFailureDialogView(
                sendFailureDialogState = state,
                onRemoveMessage = callback,
            )
            clickOn(CommonStrings.action_remove_message)
        }
    }

    @Test
    fun `clicking on cancel invokes the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setSendFailureDialogView(
                sendFailureDialogState = aSendFailureDialogStateShow(),
                onDismiss = callback,
            )
            clickOn(CommonStrings.action_cancel)
        }
    }

    private fun aSendFailureDialogStateShow(
        message: String = "A failure reason",
    ) = SendFailureDialogState.Show(
        event = aTimelineItemEvent(sendState = LocalEventSendState.Failed.Unknown(message)),
        message = message,
    )

    private fun AndroidComposeUiTest<ComponentActivity>.setSendFailureDialogView(
        sendFailureDialogState: SendFailureDialogState,
        onDismiss: () -> Unit = EnsureNeverCalled(),
        onRetry: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
        onRemoveMessage: (TimelineItem.Event) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setSafeContent {
            SendFailureDialogView(
                sendFailureDialogState = sendFailureDialogState,
                onDismiss = onDismiss,
                onRetry = onRetry,
                onRemoveMessage = onRemoveMessage,
            )
        }
    }
}
