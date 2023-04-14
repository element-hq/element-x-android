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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConfigureRoomPresenterTests {

    private lateinit var presenter: ConfigureRoomPresenter
    private lateinit var userListDataStore: UserListDataStore
    private lateinit var fakeMatrixClient: FakeMatrixClient

    @Before
    fun setup() {
        fakeMatrixClient = FakeMatrixClient()
        userListDataStore = UserListDataStore()
        presenter = ConfigureRoomPresenter(
            dataStore = CreateRoomDataStore(userListDataStore),
            matrixClient = fakeMatrixClient
        )
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
            var expectedConfig = CreateRoomConfig()
            assertThat(initialState.config).isEqualTo(expectedConfig)

            // Select User
            val selectedUser1 = aMatrixUser()
            val selectedUser2 = aMatrixUser("@id_of_bob:server.org", "Bob")
            userListDataStore.selectUser(selectedUser1)
            skipItems(1)
            userListDataStore.selectUser(selectedUser2)
            var newState = awaitItem()
            expectedConfig = expectedConfig.copy(invites = persistentListOf(selectedUser1, selectedUser2))
            assertThat(newState.config).isEqualTo(expectedConfig)

            // Room name
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(A_ROOM_NAME))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(roomName = A_ROOM_NAME)
            assertThat(newState.config).isEqualTo(expectedConfig)

            // Room topic
            newState.eventSink(ConfigureRoomEvents.TopicChanged(A_MESSAGE))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(topic = A_MESSAGE)
            assertThat(newState.config).isEqualTo(expectedConfig)

            // Room avatar
            val anUri = Uri.parse(AN_AVATAR_URL)
            newState.eventSink(ConfigureRoomEvents.AvatarUriChanged(anUri))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(avatarUrl = anUri.toString())
            assertThat(newState.config).isEqualTo(expectedConfig)

            // Room privacy
            newState.eventSink(ConfigureRoomEvents.RoomPrivacyChanged(RoomPrivacy.Public))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(privacy = RoomPrivacy.Public)
            assertThat(newState.config).isEqualTo(expectedConfig)

            // Remove user
            newState.eventSink(ConfigureRoomEvents.RemoveFromSelection(selectedUser1))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(invites = expectedConfig.invites.minus(selectedUser1).toImmutableList())
            assertThat(newState.config).isEqualTo(expectedConfig)
        }
    }

    @Test
    fun `present - trigger create room action`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val createRoomResult = Result.success(RoomId("!createRoomResult"))

            fakeMatrixClient.givenCreateRoomResult(createRoomResult)

            initialState.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(Async.Success::class.java)
            assertThat(stateAfterCreateRoom.createRoomAction.dataOrNull()).isEqualTo(createRoomResult.getOrNull())
        }
    }

    @Test
    fun `present - trigger retry and cancel actions`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val createRoomResult = Result.failure<RoomId>(A_THROWABLE)

            fakeMatrixClient.givenCreateRoomResult(createRoomResult)

            // Create
            initialState.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(Async.Failure::class.java)
            assertThat((stateAfterCreateRoom.createRoomAction as? Async.Failure)?.error).isEqualTo(createRoomResult.exceptionOrNull())

            // Retry
            stateAfterCreateRoom.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(Async.Uninitialized::class.java)
            assertThat(awaitItem().createRoomAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterRetry = awaitItem()
            assertThat(stateAfterRetry.createRoomAction).isInstanceOf(Async.Failure::class.java)
            assertThat((stateAfterRetry.createRoomAction as? Async.Failure)?.error).isEqualTo(createRoomResult.exceptionOrNull())

            // Cancel
            stateAfterRetry.eventSink(ConfigureRoomEvents.CancelCreateRoom)
            assertThat(awaitItem().createRoomAction).isInstanceOf(Async.Uninitialized::class.java)
        }
    }
}

