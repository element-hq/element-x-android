/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.forward.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.testtags.TestTags
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressTag
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForwardMessagesViewTest {
    @Test
    fun `cancel error emits the expected event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<ForwardMessagesEvents>()
        setForwardMessagesView(
            aForwardMessagesState(
                forwardAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder
            ),
        )
        pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(ForwardMessagesEvents.ClearError)
    }

    @Test
    fun `success invokes onForwardSuccess`() = runAndroidComposeUiTest {
        val data = listOf(A_ROOM_ID)
        val eventsRecorder = EventsRecorder<ForwardMessagesEvents>(expectEvents = false)
        ensureCalledOnceWithParam<List<RoomId>?>(data) { callback ->
            setForwardMessagesView(
                aForwardMessagesState(
                    forwardAction = AsyncAction.Success(data),
                    eventSink = eventsRecorder
                ),
                onForwardSuccess = callback,
            )
        }
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setForwardMessagesView(
    state: ForwardMessagesState,
    onForwardSuccess: (List<RoomId>) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        ForwardMessagesView(
            state = state,
            onForwardSuccess = onForwardSuccess,
        )
    }
}
