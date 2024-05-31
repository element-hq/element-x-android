/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.forward

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForwardMessagesViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `cancel error emits the expected event`() {
        val eventsRecorder = EventsRecorder<ForwardMessagesEvents>()
        rule.setForwardMessagesView(
            aForwardMessagesState(
                forwardAction = AsyncAction.Failure(AN_EXCEPTION),
                eventSink = eventsRecorder
            ),
        )
        rule.pressTag(TestTags.dialogPositive.value)
        eventsRecorder.assertSingle(ForwardMessagesEvents.ClearError)
    }

    @Test
    fun `success invokes onForwardSuccess`() {
        val data = listOf(A_ROOM_ID)
        val eventsRecorder = EventsRecorder<ForwardMessagesEvents>(expectEvents = false)
        ensureCalledOnceWithParam<List<RoomId>?>(data) { callback ->
            rule.setForwardMessagesView(
                aForwardMessagesState(
                    forwardAction = AsyncAction.Success(data),
                    eventSink = eventsRecorder
                ),
                onForwardSuccess = callback,
            )
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setForwardMessagesView(
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
