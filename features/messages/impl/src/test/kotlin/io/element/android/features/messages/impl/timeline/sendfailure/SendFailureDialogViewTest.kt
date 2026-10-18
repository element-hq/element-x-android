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
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.assertNodeWithTextIsDisplayed
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
    fun `show state renders the error failure reason`() = runAndroidComposeUiTest {
        setSendFailureDialogView(
            sendFailureDialogState = aSendFailureDialogStateShow(
                sendFailureType = SendFailureDialogState.SendFailureType.Error("A failure reason"),
            ),
        )
        assertNodeWithTextIsDisplayed(CommonStrings.common_sending_failed)
        onNodeWithText(activity!!.getString(R.string.error_sending_failure_other, "A failure reason")).assertExists()
    }

    @Test
    fun `show state renders the invalid mime type failure reason`() = runAndroidComposeUiTest {
        setSendFailureDialogView(
            sendFailureDialogState = aSendFailureDialogStateShow(
                sendFailureType = SendFailureDialogState.SendFailureType.InvalidMimeType("invalid/mimeType"),
            ),
        )
        onNodeWithText(activity!!.getString(R.string.error_sending_failure_invalid_mime_type, "invalid/mimeType")).assertExists()
    }

    @Test
    fun `show state renders the missing media content failure reason`() = runAndroidComposeUiTest {
        setSendFailureDialogView(
            sendFailureDialogState = aSendFailureDialogStateShow(
                sendFailureType = SendFailureDialogState.SendFailureType.MissingMediaContent,
            ),
        )
        onNodeWithText(activity!!.getString(R.string.error_sending_failure_missing_media_content)).assertExists()
    }

    @Test
    fun `show state renders the sending from unverified device failure reason`() = runAndroidComposeUiTest {
        setSendFailureDialogView(
            sendFailureDialogState = aSendFailureDialogStateShow(
                sendFailureType = SendFailureDialogState.SendFailureType.SendingFromUnverifiedDevice,
            ),
        )
        onNodeWithText(activity!!.getString(R.string.error_sending_failure_sending_from_unverified_device)).assertExists()
    }

    @Test
    fun `show state renders the unknown failure reason`() = runAndroidComposeUiTest {
        setSendFailureDialogView(
            sendFailureDialogState = aSendFailureDialogStateShow(
                sendFailureType = SendFailureDialogState.SendFailureType.Unknown,
            ),
        )
        onNodeWithText(activity!!.getString(R.string.error_sending_failure_unknown)).assertExists()
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
        sendFailureType: SendFailureDialogState.SendFailureType = SendFailureDialogState.SendFailureType.Unknown,
    ) = SendFailureDialogState.Show(
        event = aTimelineItemEvent(sendState = LocalEventSendState.Failed.Unknown("")),
        sendFailureType = sendFailureType,
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
