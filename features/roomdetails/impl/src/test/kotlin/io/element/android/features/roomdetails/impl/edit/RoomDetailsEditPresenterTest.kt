/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.edit

import android.net.Uri
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdetails.impl.aJoinedRoom
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_ROOM_RAW_NAME
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.fake.FakeTemporaryUriDeleter
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
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

@Suppress("LargeClass")
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
        room: JoinedRoom,
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        temporaryUriDeleter: TemporaryUriDeleter = FakeTemporaryUriDeleter(),
    ): RoomDetailsEditPresenter {
        return RoomDetailsEditPresenter(
            room = room,
            mediaPickerProvider = fakePickerProvider,
            mediaPreProcessor = fakeMediaPreProcessor,
            permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
            temporaryUriDeleter = temporaryUriDeleter,
        )
    }

    @Test
    fun `present - initial state is created from room info`() = runTest {
        val room = aJoinedRoom(
            avatarUrl = AN_AVATAR_URL,
            displayName = A_ROOM_NAME,
            rawName = A_ROOM_RAW_NAME,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.roomId).isEqualTo(room.roomId)
            assertThat(initialState.roomRawName).isEqualTo(A_ROOM_RAW_NAME)
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)
            assertThat(initialState.roomTopic).isEqualTo(room.info().topic.orEmpty())
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
        val room = aJoinedRoom(
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_NAME -> Result.success(true)
                    StateEventType.ROOM_AVATAR -> Result.success(false)
                    StateEventType.ROOM_TOPIC -> Result.failure(RuntimeException("Oops"))
                    else -> lambdaError()
                }
            },
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
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
            deleteCallback.assertions().isCalledOnce().with(value(null))
        }
    }

    @Test
    fun `present - sets canChangeAvatar if user has permission`() = runTest {
        val room = aJoinedRoom(
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_NAME -> Result.success(false)
                    StateEventType.ROOM_AVATAR -> Result.success(true)
                    StateEventType.ROOM_TOPIC -> Result.failure(RuntimeException("Oops"))
                    else -> lambdaError()
                }
            }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
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
        val room = aJoinedRoom(
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, stateEventType ->
                when (stateEventType) {
                    StateEventType.ROOM_NAME -> Result.success(false)
                    StateEventType.ROOM_AVATAR -> Result.failure(RuntimeException("Oops"))
                    StateEventType.ROOM_TOPIC -> Result.success(true)
                    else -> lambdaError()
                }
            }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
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
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.roomTopic).isEqualTo("My topic")
            assertThat(initialState.roomRawName).isEqualTo("Name")
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("Name II"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("My topic")
                assertThat(roomRawName).isEqualTo("Name II")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("Name III"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("My topic")
                assertThat(roomRawName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("Another topic"))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("Another topic")
                assertThat(roomRawName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isEqualTo(roomAvatarUri)
            }
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove))
            awaitItem().apply {
                assertThat(roomTopic).isEqualTo("Another topic")
                assertThat(roomRawName).isEqualTo("Name III")
                assertThat(roomAvatarUrl).isNull()
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from gallery`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        fakePickerProvider.givenResult(anotherAvatarUri)
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.roomAvatarUrl).isEqualTo(roomAvatarUri)
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            awaitItem().apply {
                assertThat(roomAvatarUrl).isEqualTo(anotherAvatarUri)
            }
        }
    }

    @Test
    fun `present - obtains avatar uris from camera`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        fakePickerProvider.givenResult(anotherAvatarUri)
        val fakePermissionsPresenter = FakePermissionsPresenter()
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            permissionsPresenter = fakePermissionsPresenter,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
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
            deleteCallback.assertions().isCalledExactly(3).withSequence(
                listOf(value(null)),
                listOf(value(roomAvatarUri)),
                listOf(value(anotherAvatarUri)),
            )
        }
    }

    @Test
    fun `present - updates save button state`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        fakePickerProvider.givenResult(roomAvatarUri)
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
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
        val room = aJoinedRoom(
            topic = null,
            displayName = "fallback",
            avatarUrl = null,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        fakePickerProvider.givenResult(roomAvatarUri)
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
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
        val setNameResult = lambdaRecorder { _: String -> Result.success(Unit) }
        val setTopicResult = lambdaRecorder { _: String -> Result.success(Unit) }
        val removeAvatarResult = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            setNameResult = setNameResult,
            setTopicResult = setTopicResult,
            removeAvatarResult = removeAvatarResult,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("New name"))
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("New topic"))
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(5)
            setNameResult.assertions().isCalledOnce().with(value("New name"))
            setTopicResult.assertions().isCalledOnce().with(value("New topic"))
            removeAvatarResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - save doesn't change room details if they're the same trimmed`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName("   Name   "))
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("  My topic  "))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - save doesn't change topic if it was unset and is now blank`() = runTest {
        val room = aJoinedRoom(
            topic = null,
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic(""))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            cancelAndIgnoreRemainingEvents()
            deleteCallback.assertions().isCalledOnce().with(value(null))
        }
    }

    @Test
    fun `present - save doesn't change name if it's now empty`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomName(""))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            cancelAndIgnoreRemainingEvents()
            deleteCallback.assertions().isCalledOnce().with(value(null))
        }
    }

    @Test
    fun `present - save processes and sets avatar when processor returns successfully`() = runTest {
        val updateAvatarResult = lambdaRecorder { _: String, _: ByteArray -> Result.success(Unit) }
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            updateAvatarResult = updateAvatarResult,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        givenPickerReturnsFile()
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(4)
            updateAvatarResult.assertions().isCalledOnce().with(value(MimeTypes.Jpeg), value(fakeFileContents))
            deleteCallback.assertions().isCalledExactly(2).withSequence(
                listOf(value(null)),
                listOf(value(roomAvatarUri)),
            )
        }
    }

    @Test
    fun `present - save does not set avatar data if processor fails`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        fakePickerProvider.givenResult(anotherAvatarUri)
        fakeMediaPreProcessor.givenResult(Result.failure(RuntimeException("Oh no")))
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(3)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - sets save action to failure if name update fails`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            setNameResult = { Result.failure(RuntimeException("!")) },
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        saveAndAssertFailure(room, RoomDetailsEditEvents.UpdateRoomName("New name"), deleteCallbackNumberOfInvocation = 1)
    }

    @Test
    fun `present - sets save action to failure if topic update fails`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            setTopicResult = { Result.failure(RuntimeException("!")) },
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        saveAndAssertFailure(room, RoomDetailsEditEvents.UpdateRoomTopic("New topic"), deleteCallbackNumberOfInvocation = 1)
    }

    @Test
    fun `present - sets save action to failure if removing avatar fails`() = runTest {
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            removeAvatarResult = { Result.failure(RuntimeException("!")) },
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        saveAndAssertFailure(room, RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.Remove), deleteCallbackNumberOfInvocation = 2)
    }

    @Test
    fun `present - sets save action to failure if setting avatar fails`() = runTest {
        givenPickerReturnsFile()
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            updateAvatarResult = { _, _ -> Result.failure(RuntimeException("!")) },
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        saveAndAssertFailure(room, RoomDetailsEditEvents.HandleAvatarAction(AvatarAction.ChoosePhoto), deleteCallbackNumberOfInvocation = 2)
    }

    @Test
    fun `present - CancelSaveChanges resets save action state`() = runTest {
        givenPickerReturnsFile()
        val room = aJoinedRoom(
            topic = "My topic",
            displayName = "Name",
            avatarUrl = AN_AVATAR_URL,
            setTopicResult = { Result.failure(RuntimeException("!")) },
            canSendStateResult = { _, _ -> Result.success(true) }
        )
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEditEvents.UpdateRoomTopic("foo"))
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(3)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
            initialState.eventSink(RoomDetailsEditEvents.CancelSaveChanges)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    private suspend fun saveAndAssertFailure(
        room: JoinedRoom,
        event: RoomDetailsEditEvents,
        deleteCallbackNumberOfInvocation: Int = 2,
    ) {
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val presenter = createRoomDetailsEditPresenter(
            room = room,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(event)
            initialState.eventSink(RoomDetailsEditEvents.Save)
            skipItems(1)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
            deleteCallback.assertions().isCalledExactly(deleteCallbackNumberOfInvocation)
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

private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
    skipItems(1)
    return awaitItem()
}
