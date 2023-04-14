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
import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.features.userlist.api.UserListDataStore
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
        presenter = ConfigureRoomPresenter(CreateRoomDataStore(UserListDataStore()))
    }

    @Test
    fun `present - initial state`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.config).isEqualTo(CreateRoomConfig())
            assertThat(initialState.config.roomName).isNull()
            assertThat(initialState.config.topic).isNull()
            assertThat(initialState.config.invites).isEmpty()
            assertThat(initialState.config.avatarUrl).isNull()
            assertThat(initialState.config.privacy).isNull()
        }
    }

    @Test
    fun `present - create room button is enabled only if the required fields are completed`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            var config = initialState.config
            assertThat(initialState.isCreateButtonEnabled).isFalse()

            // Room name not empty
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(A_ROOM_NAME))
            var newState: ConfigureRoomState = awaitItem()
            config = config.copy(roomName = A_ROOM_NAME)
            assertThat(newState.config).isEqualTo(config)
            assertThat(newState.isCreateButtonEnabled).isFalse()

            // Select privacy
            newState.eventSink(ConfigureRoomEvents.RoomPrivacyChanged(RoomPrivacy.Private))
            newState = awaitItem()
            config = config.copy(privacy = RoomPrivacy.Private)
            assertThat(newState.config).isEqualTo(config)
            assertThat(newState.isCreateButtonEnabled).isTrue()

            // Clear room name
            newState.eventSink(ConfigureRoomEvents.RoomNameChanged(""))
            newState = awaitItem()
            config = config.copy(roomName = null)
            assertThat(newState.config).isEqualTo(config)
            assertThat(newState.isCreateButtonEnabled).isFalse()
        }
    }

    @Test
    fun `present - state is updated when fields are changed`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            var config = initialState.config

            // Room name
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(A_ROOM_NAME))
            var newState = awaitItem()
            config = config.copy(roomName = A_ROOM_NAME)
            assertThat(newState.config).isEqualTo(config)

            // Room topic
            newState.eventSink(ConfigureRoomEvents.TopicChanged(A_MESSAGE))
            newState = awaitItem()
            config = config.copy(topic = A_MESSAGE)
            assertThat(newState.config).isEqualTo(config)

            // Room avatar
            val anUri = Uri.parse(AN_AVATAR_URL)
            newState.eventSink(ConfigureRoomEvents.AvatarUriChanged(anUri))
            newState = awaitItem()
            config = config.copy(avatarUrl = anUri.toString())
            assertThat(newState.config).isEqualTo(config)

            // Room privacy
            newState.eventSink(ConfigureRoomEvents.RoomPrivacyChanged(RoomPrivacy.Public))
            newState = awaitItem()
            config = config.copy(privacy = RoomPrivacy.Public)
            assertThat(newState.config).isEqualTo(config)
        }
    }
}

