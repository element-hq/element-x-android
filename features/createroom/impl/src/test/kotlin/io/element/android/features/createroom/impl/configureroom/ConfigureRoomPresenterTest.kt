/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

private const val AN_URI_FROM_CAMERA = "content://uri_from_camera"
private const val AN_URI_FROM_CAMERA_2 = "content://uri_from_camera_2"
private const val AN_URI_FROM_GALLERY = "content://uri_from_gallery"

@RunWith(RobolectricTestRunner::class)
class ConfigureRoomPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private lateinit var presenter: ConfigureRoomPresenter
    private lateinit var userListDataStore: UserListDataStore
    private lateinit var createRoomDataStore: CreateRoomDataStore
    private lateinit var fakeMatrixClient: FakeMatrixClient
    private lateinit var fakePickerProvider: FakePickerProvider
    private lateinit var fakeMediaPreProcessor: FakeMediaPreProcessor
    private lateinit var fakeAnalyticsService: FakeAnalyticsService
    private lateinit var fakePermissionsPresenter: FakePermissionsPresenter

    @Before
    fun setup() {
        fakeMatrixClient = FakeMatrixClient()
        userListDataStore = UserListDataStore()
        createRoomDataStore = CreateRoomDataStore(userListDataStore)
        fakePickerProvider = FakePickerProvider()
        fakeMediaPreProcessor = FakeMediaPreProcessor()
        fakeAnalyticsService = FakeAnalyticsService()
        fakePermissionsPresenter = FakePermissionsPresenter()
        presenter = ConfigureRoomPresenter(
            dataStore = createRoomDataStore,
            matrixClient = fakeMatrixClient,
            mediaPickerProvider = fakePickerProvider,
            mediaPreProcessor = fakeMediaPreProcessor,
            analyticsService = fakeAnalyticsService,
            permissionsPresenterFactory = FakePermissionsPresenterFactory(fakePermissionsPresenter),
        )

        mockkStatic(File::readBytes)
        every { any<File>().readBytes() } returns byteArrayOf()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `present - initial state`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.config).isEqualTo(CreateRoomConfig())
            assertThat(initialState.config.roomName).isNull()
            assertThat(initialState.config.topic).isNull()
            assertThat(initialState.config.invites).isEmpty()
            assertThat(initialState.config.avatarUri).isNull()
            assertThat(initialState.config.privacy).isEqualTo(RoomPrivacy.Private)
        }
    }

    @Test
    fun `present - create room button is enabled only if the required fields are completed`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
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
        moleculeFlow(RecompositionMode.Immediate) {
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
            // Pick avatar
            fakePickerProvider.givenResult(null)
            // From gallery
            val uriFromGallery = Uri.parse(AN_URI_FROM_GALLERY)
            fakePickerProvider.givenResult(uriFromGallery)
            newState.eventSink(ConfigureRoomEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(avatarUri = uriFromGallery)
            assertThat(newState.config).isEqualTo(expectedConfig)
            // From camera
            val uriFromCamera = Uri.parse(AN_URI_FROM_CAMERA)
            fakePickerProvider.givenResult(uriFromCamera)
            assertThat(newState.cameraPermissionState.permissionGranted).isFalse()
            newState.eventSink(ConfigureRoomEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            newState = awaitItem()
            assertThat(newState.cameraPermissionState.showDialog).isTrue()
            fakePermissionsPresenter.setPermissionGranted()
            newState = awaitItem()
            assertThat(newState.cameraPermissionState.permissionGranted).isTrue()
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(avatarUri = uriFromCamera)
            assertThat(newState.config).isEqualTo(expectedConfig)
            // Do it again, no permission is requested
            val uriFromCamera2 = Uri.parse(AN_URI_FROM_CAMERA_2)
            fakePickerProvider.givenResult(uriFromCamera2)
            newState.eventSink(ConfigureRoomEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(avatarUri = uriFromCamera2)
            assertThat(newState.config).isEqualTo(expectedConfig)
            // Remove
            newState.eventSink(ConfigureRoomEvents.HandleAvatarAction(AvatarAction.Remove))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(avatarUri = null)
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val createRoomResult = Result.success(RoomId("!createRoomResult:domain"))

            fakeMatrixClient.givenCreateRoomResult(createRoomResult)

            initialState.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(stateAfterCreateRoom.createRoomAction.dataOrNull()).isEqualTo(createRoomResult.getOrNull())
        }
    }

    @Test
    fun `present - record analytics when creating room`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val createRoomResult = Result.success(RoomId("!createRoomResult:domain"))

            fakeMatrixClient.givenCreateRoomResult(createRoomResult)

            initialState.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            skipItems(2)

            val analyticsEvent = fakeAnalyticsService.capturedEvents.filterIsInstance<CreatedRoom>().firstOrNull()
            assertThat(analyticsEvent).isNotNull()
            assertThat(analyticsEvent?.isDM).isFalse()
        }
    }

    @Test
    fun `present - trigger create room with upload error and retry`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            createRoomDataStore.setAvatarUri(Uri.parse(AN_URI_FROM_GALLERY))
            fakeMediaPreProcessor.givenResult(Result.success(MediaUploadInfo.Image(mockk(), mockk(), mockk())))
            fakeMatrixClient.givenUploadMediaResult(Result.failure(A_THROWABLE))

            val initialState = awaitItem()
            initialState.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(AsyncAction.Failure::class.java)
            assertThat(fakeAnalyticsService.capturedEvents.filterIsInstance<CreatedRoom>()).isEmpty()

            fakeMatrixClient.givenUploadMediaResult(Result.success(AN_AVATAR_URL))
            stateAfterCreateRoom.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Success::class.java)
        }
    }

    @Test
    fun `present - trigger retry and cancel actions`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val createRoomResult = Result.failure<RoomId>(A_THROWABLE)

            fakeMatrixClient.givenCreateRoomResult(createRoomResult)

            // Create
            initialState.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(AsyncAction.Failure::class.java)
            assertThat((stateAfterCreateRoom.createRoomAction as? AsyncAction.Failure)?.error).isEqualTo(createRoomResult.exceptionOrNull())

            // Retry
            stateAfterCreateRoom.eventSink(ConfigureRoomEvents.CreateRoom(initialState.config))
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            val stateAfterRetry = awaitItem()
            assertThat(stateAfterRetry.createRoomAction).isInstanceOf(AsyncAction.Failure::class.java)
            assertThat((stateAfterRetry.createRoomAction as? AsyncAction.Failure)?.error).isEqualTo(createRoomResult.exceptionOrNull())

            // Cancel
            stateAfterRetry.eventSink(ConfigureRoomEvents.CancelCreateRoom)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }
}
