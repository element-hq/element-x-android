/*
 * Copyright (c) 2023 New Vector Ltd
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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.createroom.impl.configureroom

import android.net.Uri
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigureRoomPresenterTests {

    private lateinit var presenter: ConfigureRoomPresenter

    @Before
    fun setup() {
        presenter = ConfigureRoomPresenter(ConfigureRoomPresenterArgs(emptyList()))
    }

    @Test
    fun `present - initial state`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomName).isEmpty()
            assertThat(initialState.topic).isEmpty()
            assertThat(initialState.privacy).isNull()
        }
    }

    @Test
    fun `present - create room button is enabled only if the required fields are completed`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isCreateButtonEnabled).isFalse()

            // Room name not empty
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(A_ROOM_NAME))
            var newState: ConfigureRoomState = awaitItem()
            assertThat(newState.roomName).isEqualTo(A_ROOM_NAME)
            assertThat(newState.isCreateButtonEnabled).isFalse()

            // Select privacy
            initialState.eventSink(ConfigureRoomEvents.RoomPrivacyChanged(RoomPrivacy.Private))
            newState = awaitItem()
            assertThat(newState.privacy).isEqualTo(RoomPrivacy.Private)
            assertThat(newState.isCreateButtonEnabled).isTrue()

            // Clear room name
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(""))
            newState = awaitItem()
            assertThat(newState.roomName).isEqualTo("")
            assertThat(newState.isCreateButtonEnabled).isFalse()
        }
    }

    @Test
    fun `present - state is updated when fields are changed`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Room name
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(A_ROOM_NAME))
            val stateAfterRoomNameChanged = awaitItem()
            assertThat(stateAfterRoomNameChanged.roomName).isEqualTo(A_ROOM_NAME)

            // Room topic
            stateAfterRoomNameChanged.eventSink(ConfigureRoomEvents.TopicChanged(A_MESSAGE))
            val stateAfterTopicChanged = awaitItem()
            assertThat(stateAfterTopicChanged.topic).isEqualTo(A_MESSAGE)

            // Room avatar
            val anUri = Uri.parse(AN_AVATAR_URL)
            stateAfterTopicChanged.eventSink(ConfigureRoomEvents.AvatarUriChanged(anUri))
            val stateAfterAvatarUriChanged = awaitItem()
            assertThat(stateAfterAvatarUriChanged.avatarUri).isEqualTo(anUri)

            // Room privacy
            stateAfterAvatarUriChanged.eventSink(ConfigureRoomEvents.RoomPrivacyChanged(RoomPrivacy.Public))
            val stateAfterPrivacyChanged = awaitItem()
            assertThat(stateAfterPrivacyChanged.privacy).isEqualTo(RoomPrivacy.Public)
        }
    }
}

