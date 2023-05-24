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

package io.element.android.features.roomdetails.edition

import android.net.Uri
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.ui.AvatarAction
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.edition.RoomDetailsEditionEvents
import io.element.android.features.roomdetails.impl.edition.RoomDetailsEditionPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.api.ThumbnailProcessingInfo
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class RoomDetailsEditionPresenterTest {

    private lateinit var fakePickerProvider: FakePickerProvider
    private lateinit var fakeMediaPreProcessor: FakeMediaPreProcessor

    private val roomAvatarUri: Uri = mockk()
    private val anotherAvatarUri: Uri = mockk()

    private val fakeFileContents = ByteArray(2)

    @Before
    fun setup() {
        fakePickerProvider = FakePickerProvider()
        fakeMediaPreProcessor = FakeMediaPreProcessor()
        mockkStatic(Uri::class)

        every { Uri.parse(AN_AVATAR_URL) } returns roomAvatarUri
        every { Uri.parse(ANOTHER_AVATAR_URL) } returns anotherAvatarUri
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun aRoomDetailsEditionPresenter(room: MatrixRoom): RoomDetailsEditionPresenter {
        return RoomDetailsEditionPresenter(
            room = room,
            mediaPickerProvider = fakePickerProvider,
            mediaPreProcessor = fakeMediaPreProcessor,
        )
    }

    @Test
    fun `present - initial state is created from room info`() = runTest {
        val room = aMatrixRoom(avatarUrl = AN_AVATAR_URL)
        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomId).isEqualTo(room.roomId.value)
            assertThat(initialState.roomName).isEqualTo(room.name)
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)
            assertThat(initialState.roomTopic).isEqualTo(room.topic.orEmpty())
            assertThat(initialState.avatarActions).containsExactly(
                AvatarAction.ChoosePhoto,
                AvatarAction.TakePhoto,
                AvatarAction.Remove
            )
            assertThat(initialState.saveButtonEnabled).isEqualTo(false)
            assertThat(initialState.saveAction).isInstanceOf(Async.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - sets canChangeName if user has permission`() = runTest {
        val room = aMatrixRoom(avatarUrl = AN_AVATAR_URL).apply {
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.success(false))
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.failure(Throwable("Oops")))        }
        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            // Initially false
            val initialState = awaitItem()
            assertThat(initialState.canChangeName).isFalse()
            assertThat(initialState.canChangeAvatar).isFalse()
            assertThat(initialState.canChangeTopic).isFalse()

            // When the asynchronous check completes, the single field we can edit is true
            val settledState = awaitItem()
            assertThat(settledState.canChangeName).isTrue()
            assertThat(settledState.canChangeAvatar).isFalse()
            assertThat(settledState.canChangeTopic).isFalse()
        }
    }

    @Test
    fun `present - sets canChangeAvatar if user has permission`() = runTest {
        val room = aMatrixRoom(avatarUrl = AN_AVATAR_URL).apply {
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(false))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.failure(Throwable("Oops")))
        }
        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            // Initially false
            val initialState = awaitItem()
            assertThat(initialState.canChangeName).isFalse()
            assertThat(initialState.canChangeAvatar).isFalse()
            assertThat(initialState.canChangeTopic).isFalse()

            // When the asynchronous check completes, the single field we can edit is true
            val settledState = awaitItem()
            assertThat(settledState.canChangeName).isFalse()
            assertThat(settledState.canChangeAvatar).isTrue()
            assertThat(settledState.canChangeTopic).isFalse()
        }
    }

    @Test
    fun `present - sets canChangeTopic if user has permission`() = runTest {
        val room = aMatrixRoom(avatarUrl = AN_AVATAR_URL).apply {
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(false))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.failure(Throwable("Oops")))
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.success(true))
        }
        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            // Initially false
            val initialState = awaitItem()
            assertThat(initialState.canChangeName).isFalse()
            assertThat(initialState.canChangeAvatar).isFalse()
            assertThat(initialState.canChangeTopic).isFalse()

            // When the asynchronous check completes, the single field we can edit is true
            val settledState = awaitItem()
            assertThat(settledState.canChangeName).isFalse()
            assertThat(settledState.canChangeAvatar).isFalse()
            assertThat(settledState.canChangeTopic).isTrue()
        }
    }

    @Test
    fun `present - updates state in response to changes`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)
        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomTopic).isEqualTo("My topic")
            assertThat(initialState.roomName).isEqualTo("Name")
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)

            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomName("Name II"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("My topic")
                assertThat(roomName).isEqualTo("Name II")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }

            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomName("Name III"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("My topic")
                assertThat(roomName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }

            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomTopic("Another topic"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("Another topic")
                assertThat(roomName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }

            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("Another topic")
                assertThat(roomName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isNull()
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from gallery`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        fakePickerProvider.givenResult(anotherAvatarUri)

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)

            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(roomAvatarUrl).isEqualTo(anotherAvatarUri)
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from camera`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        fakePickerProvider.givenResult(anotherAvatarUri)

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)

            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            awaitItem().apply {
                assertThat(roomAvatarUrl).isEqualTo(anotherAvatarUri)
            }
        }
    }

    @Test
    fun `present - updates save button state`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        fakePickerProvider.givenResult(roomAvatarUri)

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.saveButtonEnabled).isEqualTo(false)

            // Once a change is made, the save button is enabled
            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomName("Name II"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isEqualTo(true)
            }

            // If it's reverted then the save disables again
            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomName("Name"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isEqualTo(false)
            }

            // Make a change...
            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomTopic("Another topic"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isEqualTo(true)
            }

            // Revert it...
            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomTopic("My topic"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isEqualTo(false)
            }

            // Make a change...
            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isEqualTo(true)
            }

            // Revert it...
            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isEqualTo(false)
            }
        }
    }

    @Test
    fun `present - save changes room details if different`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomName("New name"))
            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomTopic("New topic"))
            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.Remove))
            initialState.eventSink(RoomDetailsEditionEvents.Save)

            assertThat(room.newName).isEqualTo("New name")
            assertThat(room.newTopic).isEqualTo("New topic")
            assertThat(room.newAvatarData).isNull()
            assertThat(room.removedAvatar).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save doesn't change room details if they're the same trimmed`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomName("   Name   "))
            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomTopic("  My topic  "))
            initialState.eventSink(RoomDetailsEditionEvents.Save)

            assertThat(room.newName).isNull()
            assertThat(room.newTopic).isNull()
            assertThat(room.newAvatarData).isNull()
            assertThat(room.removedAvatar).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save doesn't change topic if it was unset and is now blank`() = runTest {
        val room = aMatrixRoom(topic = null, name = "Name", avatarUrl = AN_AVATAR_URL)

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomTopic(""))
            initialState.eventSink(RoomDetailsEditionEvents.Save)

            assertThat(room.newName).isNull()
            assertThat(room.newTopic).isNull()
            assertThat(room.newAvatarData).isNull()
            assertThat(room.removedAvatar).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save doesn't change name if it's now empty`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditionEvents.UpdateRoomName(""))
            initialState.eventSink(RoomDetailsEditionEvents.Save)

            assertThat(room.newName).isNull()
            assertThat(room.newTopic).isNull()
            assertThat(room.newAvatarData).isNull()
            assertThat(room.removedAvatar).isFalse()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save processes and sets avatar when processor returns successfully`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        givenPickerReturnsFile()

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(RoomDetailsEditionEvents.Save)
            skipItems(2)

            assertThat(room.newName).isNull()
            assertThat(room.newTopic).isNull()
            assertThat(room.newAvatarData).isSameInstanceAs(fakeFileContents)
            assertThat(room.removedAvatar).isFalse()
        }
    }

    @Test
    fun `present - save does not set avatar data if processor fails`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(Result.failure(Throwable("Oh no")))

        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(RoomDetailsEditionEvents.Save)
            skipItems(1)

            assertThat(room.newName).isNull()
            assertThat(room.newTopic).isNull()
            assertThat(room.newAvatarData).isNull()
            assertThat(room.removedAvatar).isFalse()

            assertThat(awaitItem().saveAction).isInstanceOf(Async.Failure::class.java)
        }
    }

    @Test
    fun `present - sets save action to failure if name update fails`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenSetNameResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditionEvents.UpdateRoomName("New name"))
    }

    @Test
    fun `present - sets save action to failure if topic update fails`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenSetTopicResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditionEvents.UpdateRoomTopic("New topic"))
    }

    @Test
    fun `present - sets save action to failure if removing avatar fails`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenRemoveAvatarResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.Remove))
    }

    @Test
    fun `present - sets save action to failure if setting avatar fails`() = runTest {
        givenPickerReturnsFile()

        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenUpdateAvatarResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditionEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
    }

    private suspend fun saveAndAssertFailure(room: MatrixRoom, event: RoomDetailsEditionEvents) {
        val presenter = aRoomDetailsEditionPresenter(room)

        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(event)
            initialState.eventSink(RoomDetailsEditionEvents.Save)
            skipItems(1)

            assertThat(awaitItem().saveAction).isInstanceOf(Async.Failure::class.java)
        }
    }

    private fun givenPickerReturnsFile() {
        mockkStatic(File::readBytes)
        val processedFile: File = mockk {
            every { readBytes() } returns fakeFileContents
        }

        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(Result.success(MediaUploadInfo.Image(
            file = processedFile,
            info = mockk(),
            thumbnailInfo = ThumbnailProcessingInfo(
                file = processedFile,
                info = mockk(),
                blurhash = "",
            )
        )))
    }

    companion object {
        private const val ANOTHER_AVATAR_URL = "example://camera/foo.jpg"
    }

}
