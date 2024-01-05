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

package io.element.android.features.roomdetails.edit

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.edit.RoomDetailsEditEvents
import io.element.android.features.roomdetails.impl.edit.RoomDetailsEditPresenter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.tests.testutils.WarmUpRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class RoomDetailsEditPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

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

    private fun createRoomDetailsEditPresenter(
        room: MatrixRoom,
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
    ): RoomDetailsEditPresenter {
        return RoomDetailsEditPresenter(
            room = room,
            mediaPickerProvider = fakePickerProvider,
            mediaPreProcessor = fakeMediaPreProcessor,
            permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
        )
    }

    @Test
    fun `present - initial state is created from room info`() = runTest {
        val room = aMatrixRoom(avatarUrl = AN_AVATAR_URL)
        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
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
            assertThat(initialState.saveButtonEnabled).isFalse()
            assertThat(initialState.saveAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    @Test
    fun `present - sets canChangeName if user has permission`() = runTest {
        val room = aMatrixRoom(avatarUrl = AN_AVATAR_URL).apply {
            givenCanSendStateResult(StateEventType.ROOM_NAME, Result.success(true))
            givenCanSendStateResult(StateEventType.ROOM_AVATAR, Result.success(false))
            givenCanSendStateResult(StateEventType.ROOM_TOPIC, Result.failure(Throwable("Oops")))
        }
        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
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
        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
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
        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
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
        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomTopic).isEqualTo("My topic")
            assertThat(initialState.roomName).isEqualTo("Name")
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("Name II"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("My topic")
                assertThat(roomName).isEqualTo("Name II")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("Name III"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("My topic")
                assertThat(roomName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("Another topic"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("Another topic")
                assertThat(roomName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }

            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove))
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

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)

            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(roomAvatarUrl).isEqualTo(anotherAvatarUri)
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from camera`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        fakePickerProvider.givenResult(anotherAvatarUri)
        val fakePermissionsPresenter = FakePermissionsPresenter()
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            permissionsPresenter = fakePermissionsPresenter,
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)
            assertThat(initialState.cameraPermissionState.permissionGranted).isFalse()
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            val stateWithAskingPermission = awaitItem()
            assertThat(stateWithAskingPermission.cameraPermissionState.showDialog).isTrue()
            fakePermissionsPresenter.setPermissionGranted()
            val stateWithPermission = awaitItem()
            assertThat(stateWithPermission.cameraPermissionState.permissionGranted).isTrue()
            val stateWithNewAvatar = awaitItem()
            assertThat(stateWithNewAvatar.roomAvatarUrl).isEqualTo(anotherAvatarUri)
            // Do it again, no permission is requested
            fakePickerProvider.givenResult(roomAvatarUri)
            stateWithNewAvatar.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            val stateWithNewAvatar2 = awaitItem()
            assertThat(stateWithNewAvatar2.roomAvatarUrl).isEqualTo(roomAvatarUri)
        }
    }

    @Test
    fun `present - updates save button state`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        fakePickerProvider.givenResult(roomAvatarUri)

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.saveButtonEnabled).isFalse()

            // Once a change is made, the save button is enabled
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("Name II"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }

            // If it's reverted then the save disables again
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("Name"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }

            // Make a change...
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("Another topic"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }

            // Revert it...
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("My topic"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }

            // Make a change...
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }

            // Revert it...
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - updates save button state when initial values are null`() = runTest {
        val room = aMatrixRoom(topic = null, name = null, displayName = "fallback", avatarUrl = null)

        fakePickerProvider.givenResult(roomAvatarUri)

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.saveButtonEnabled).isFalse()

            // Once a change is made, the save button is enabled
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("Name II"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }

            // If it's reverted then the save disables again
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("fallback"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }

            // Make a change...
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("Another topic"))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }

            // Revert it...
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic(""))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }

            // Make a change...
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isTrue()
            }

            // Revert it...
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(saveButtonEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - save changes room details if different`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL)

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("New name"))
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("New topic"))
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(5)
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

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("   Name   "))
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("  My topic  "))
            initialState.eventSink(RoomDetailsEditEvents.Save)

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

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic(""))
            initialState.eventSink(RoomDetailsEditEvents.Save)

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

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName(""))
            initialState.eventSink(RoomDetailsEditEvents.Save)

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

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(3)

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

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(2)

            assertThat(room.newName).isNull()
            assertThat(room.newTopic).isNull()
            assertThat(room.newAvatarData).isNull()
            assertThat(room.removedAvatar).isFalse()

            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - sets save action to failure if name update fails`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenSetNameResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditEvents.UpdateRoomName("New name"))
    }

    @Test
    fun `present - sets save action to failure if topic update fails`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenSetTopicResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditEvents.UpdateRoomTopic("New topic"))
    }

    @Test
    fun `present - sets save action to failure if removing avatar fails`() = runTest {
        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenRemoveAvatarResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove))
    }

    @Test
    fun `present - sets save action to failure if setting avatar fails`() = runTest {
        givenPickerReturnsFile()

        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenUpdateAvatarResult(Result.failure(Throwable("!")))
        }

        saveAndAssertFailure(room, RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
    }

    @Test
    fun `present - CancelSaveChanges resets save action state`() = runTest {
        givenPickerReturnsFile()

        val room = aMatrixRoom(topic = "My topic", name = "Name", avatarUrl = AN_AVATAR_URL).apply {
            givenSetTopicResult(Result.failure(Throwable("!")))
        }

        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("foo"))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(2)

            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)

            initialState.eventSink(RoomDetailsEditEvents.CancelSaveChanges)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    private suspend fun saveAndAssertFailure(room: MatrixRoom, event: RoomDetailsEditEvents) {
        val presenter = createRoomDetailsEditPresenter(room)

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(event)
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(1)

            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    private fun givenPickerReturnsFile() {
        mockkStatic(File::readBytes)
        val processedFile: File = mockk {
            every { readBytes() } returns fakeFileContents
        }

        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(
            Result.success(
                MediaUploadInfo.AnyFile(
                    file = processedFile,
                    fileInfo = mockk(),
                )
            )
        )
    }

    companion object {
        private const val ANOTHER_AVATAR_URL = "example://camera/foo.jpg"
    }
}
