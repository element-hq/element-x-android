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

package io.element.android.features.roomaliasresolver.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomAliasResolverViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setRoomAliasResolverView(
                aRoomAliasResolverState(
                    eventSink = eventsRecorder,
                ),
                onBackPressed = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on Retry emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvents>()
        rule.setRoomAliasResolverView(
            aRoomAliasResolverState(
                resolveState = AsyncData.Failure(Exception("Error")),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_retry)
        eventsRecorder.assertSingle(RoomAliasResolverEvents.Retry)
    }

    @Test
    fun `success state invokes the expected Callback`() {
        val eventsRecorder = EventsRecorder<RoomAliasResolverEvents>(expectEvents = false)
        ensureCalledOnceWithParam(A_ROOM_ID) {
            rule.setRoomAliasResolverView(
                aRoomAliasResolverState(
                    resolveState = AsyncData.Success(A_ROOM_ID),
                    eventSink = eventsRecorder,
                ),
                onAliasResolved = it,
            )
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomAliasResolverView(
    state: RoomAliasResolverState,
    onBackPressed: () -> Unit = EnsureNeverCalled(),
    onAliasResolved: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomAliasResolverView(
            state = state,
            onBackPressed = onBackPressed,
            onAliasResolved = onAliasResolved,
        )
    }
}
