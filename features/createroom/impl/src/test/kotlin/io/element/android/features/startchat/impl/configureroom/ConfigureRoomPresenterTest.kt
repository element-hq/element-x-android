/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.configureroom

import android.net.Uri
import androidx.core.net.toUri
import app.cash.turbine.TurbineTestContext
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.features.createroom.impl.configureroom.ConfigureRoomEvents
import io.element.android.features.createroom.impl.configureroom.ConfigureRoomPresenter
import io.element.android.features.createroom.impl.configureroom.ConfigureRoomState
import io.element.android.features.createroom.impl.configureroom.CreateRoomConfig
import io.element.android.features.createroom.impl.configureroom.CreateRoomConfigStore
import io.element.android.features.createroom.impl.configureroom.RoomAccess
import io.element.android.features.createroom.impl.configureroom.RoomAddress
import io.element.android.features.createroom.impl.configureroom.RoomVisibilityItem
import io.element.android.features.createroom.impl.configureroom.RoomVisibilityState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.alias.FakeRoomAliasHelper
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediapickers.test.FakePickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.Optional

private const val AN_URI_FROM_CAMERA = "content://uri_from_camera"
private const val AN_URI_FROM_CAMERA_2 = "content://uri_from_camera_2"
private const val AN_URI_FROM_GALLERY = "content://uri_from_gallery"

@RunWith(RobolectricTestRunner::class)
class ConfigureRoomPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Before
    fun setup() {
        mockkStatic(File::readBytes)
        every { any<File>().readBytes() } returns byteArrayOf()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createConfigureRoomPresenter()
        presenter.test {
            val initialState = initialState()
            assertThat(initialState.config).isEqualTo(CreateRoomConfig())
            assertThat(initialState.config.roomName).isNull()
            assertThat(initialState.config.topic).isNull()
            assertThat(initialState.config.invites).isEmpty()
            assertThat(initialState.config.avatarUri).isNull()
            assertThat(initialState.config.roomVisibility).isEqualTo(RoomVisibilityState.Private)
            assertThat(initialState.createRoomAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            assertThat(initialState.homeserverName).isEqualTo("matrix.org")
        }
    }

    @Test
    fun `present - create room button is enabled only if the required fields are completed`() = runTest {
        val presenter = createConfigureRoomPresenter()
        presenter.test {
            val initialState = initialState()
            var config = initialState.config
            assertThat(initialState.isValid).isFalse()

            // Room name not empty
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(A_ROOM_NAME))
            var newState: ConfigureRoomState = awaitItem()
            config = config.copy(roomName = A_ROOM_NAME)
            assertThat(newState.config).isEqualTo(config)
            assertThat(newState.isValid).isTrue()

            // Clear room name
            newState.eventSink(ConfigureRoomEvents.RoomNameChanged(""))
            newState = awaitItem()
            config = config.copy(roomName = null)
            assertThat(newState.config).isEqualTo(config)
            assertThat(newState.isValid).isFalse()
        }
    }

    @Test
    fun `present - state is updated when fields are changed`() = runTest {
        val pickerProvider = FakePickerProvider()
        val permissionsPresenter = FakePermissionsPresenter()
        val roomAliasHelper = FakeRoomAliasHelper()
        val presenter = createConfigureRoomPresenter(
            dataStore = CreateRoomConfigStore(roomAliasHelper),
            pickerProvider = pickerProvider,
            permissionsPresenter = permissionsPresenter,
        )
        presenter.test {
            val initialState = initialState()
            var expectedConfig = CreateRoomConfig()
            assertThat(initialState.config).isEqualTo(expectedConfig)
            // Room name
            initialState.eventSink(ConfigureRoomEvents.RoomNameChanged(A_ROOM_NAME))
            var newState = awaitItem()
            expectedConfig = expectedConfig.copy(roomName = A_ROOM_NAME)
            assertThat(newState.config).isEqualTo(expectedConfig)

            // Room topic
            newState.eventSink(ConfigureRoomEvents.TopicChanged(A_MESSAGE))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(topic = A_MESSAGE)
            assertThat(newState.config).isEqualTo(expectedConfig)

            // Room avatar
            // Pick avatar
            pickerProvider.givenResult(null)
            // From gallery
            val uriFromGallery = AN_URI_FROM_GALLERY
            pickerProvider.givenResult(uriFromGallery.toUri())
            newState.eventSink(ConfigureRoomEvents.HandleAvatarAction(AvatarAction.ChoosePhoto))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(avatarUri = uriFromGallery)
            assertThat(newState.config).isEqualTo(expectedConfig)
            // From camera
            val uriFromCamera = AN_URI_FROM_CAMERA
            pickerProvider.givenResult(uriFromCamera.toUri())
            assertThat(newState.cameraPermissionState.permissionGranted).isFalse()
            newState.eventSink(ConfigureRoomEvents.HandleAvatarAction(AvatarAction.TakePhoto))
            newState = awaitItem()
            assertThat(newState.cameraPermissionState.showDialog).isTrue()
            permissionsPresenter.setPermissionGranted()
            newState = awaitItem()
            assertThat(newState.cameraPermissionState.permissionGranted).isTrue()
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(avatarUri = uriFromCamera)
            assertThat(newState.config).isEqualTo(expectedConfig)
            // Do it again, no permission is requested
            val uriFromCamera2 = AN_URI_FROM_CAMERA_2
            pickerProvider.givenResult(uriFromCamera2.toUri())
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
            newState.eventSink(ConfigureRoomEvents.RoomVisibilityChanged(RoomVisibilityItem.Public))
            newState = awaitItem()
            expectedConfig = expectedConfig.copy(
                roomVisibility = RoomVisibilityState.Public(
                    roomAddress = RoomAddress.AutoFilled(roomAliasHelper.roomAliasNameFromRoomDisplayName(expectedConfig.roomName ?: "")),
                    roomAccess = RoomAccess.Anyone,
                )
            )
            assertThat(newState.config).isEqualTo(expectedConfig)
        }
    }

    @Test
    fun `present - trigger create room action`() = runTest {
        val matrixClient = createMatrixClient()
        val presenter = createConfigureRoomPresenter(
            matrixClient = matrixClient
        )
        presenter.test {
            val initialState = initialState()
            val createRoomResult = Result.success(RoomId("!createRoomResult:domain"))

            matrixClient.givenCreateRoomResult(createRoomResult)

            initialState.eventSink(ConfigureRoomEvents.CreateRoom)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(AsyncAction.Success::class.java)
            assertThat(stateAfterCreateRoom.createRoomAction.dataOrNull()).isEqualTo(createRoomResult.getOrNull())
        }
    }

    @Test
    fun `present - record analytics when creating room`() = runTest {
        val matrixClient = createMatrixClient()
        val analyticsService = FakeAnalyticsService()
        val presenter = createConfigureRoomPresenter(
            matrixClient = matrixClient,
            analyticsService = analyticsService
        )
        presenter.test {
            val initialState = initialState()
            val createRoomResult = Result.success(RoomId("!createRoomResult:domain"))

            matrixClient.givenCreateRoomResult(createRoomResult)

            initialState.eventSink(ConfigureRoomEvents.CreateRoom)
            skipItems(2)

            val analyticsEvent = analyticsService.capturedEvents.filterIsInstance<CreatedRoom>().firstOrNull()
            assertThat(analyticsEvent).isNotNull()
            assertThat(analyticsEvent?.isDM).isFalse()
        }
    }

    @Test
    fun `present - trigger create room with upload error and retry`() = runTest {
        val matrixClient = createMatrixClient()
        val analyticsService = FakeAnalyticsService()
        val mediaPreProcessor = FakeMediaPreProcessor()
        val dataStore = CreateRoomConfigStore(FakeRoomAliasHelper())
        val presenter = createConfigureRoomPresenter(
            dataStore = dataStore,
            mediaPreProcessor = mediaPreProcessor,
            matrixClient = matrixClient,
            analyticsService = analyticsService
        )
        presenter.test {
            val initialState = initialState()
            dataStore.setAvatarUri(Uri.parse(AN_URI_FROM_GALLERY))
            skipItems(1)
            mediaPreProcessor.givenResult(Result.success(MediaUploadInfo.Image(mockk(), mockk(), mockk())))
            matrixClient.givenUploadMediaResult(Result.failure(AN_EXCEPTION))

            initialState.eventSink(ConfigureRoomEvents.CreateRoom)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(AsyncAction.Failure::class.java)
            assertThat(analyticsService.capturedEvents.filterIsInstance<CreatedRoom>()).isEmpty()

            matrixClient.givenUploadMediaResult(Result.success(AN_AVATAR_URL))
            stateAfterCreateRoom.eventSink(ConfigureRoomEvents.CreateRoom)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Success::class.java)
        }
    }

    @Test
    fun `present - trigger retry and cancel actions`() = runTest {
        val fakeMatrixClient = createMatrixClient()
        val presenter = createConfigureRoomPresenter(
            matrixClient = fakeMatrixClient
        )
        presenter.test {
            val initialState = initialState()
            val createRoomResult = Result.failure<RoomId>(AN_EXCEPTION)

            fakeMatrixClient.givenCreateRoomResult(createRoomResult)

            // Create
            initialState.eventSink(ConfigureRoomEvents.CreateRoom)
            assertThat(awaitItem().createRoomAction).isInstanceOf(AsyncAction.Loading::class.java)
            val stateAfterCreateRoom = awaitItem()
            assertThat(stateAfterCreateRoom.createRoomAction).isInstanceOf(AsyncAction.Failure::class.java)
            assertThat((stateAfterCreateRoom.createRoomAction as? AsyncAction.Failure)?.error).isEqualTo(createRoomResult.exceptionOrNull())

            // Retry
            stateAfterCreateRoom.eventSink(ConfigureRoomEvents.CreateRoom)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - address is invalid when format is invalid`() = runTest {
        val aliasHelper = FakeRoomAliasHelper(
            isRoomAliasValidLambda = { false }
        )
        val presenter = createConfigureRoomPresenter(
            roomAliasHelper = aliasHelper
        )
        presenter.test {
            val initialState = initialState()
            initialState.eventSink(ConfigureRoomEvents.RoomVisibilityChanged(RoomVisibilityItem.Public))
            skipItems(1)
            initialState.eventSink(ConfigureRoomEvents.RoomAddressChanged("invalid address"))
            skipItems(1)
            advanceUntilIdle()
            awaitItem().also { state ->
                assertThat(state.roomAddressValidity).isEqualTo(RoomAddressValidity.InvalidSymbols)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - address is not available when alias is not available`() = runTest {
        val fakeMatrixClient = createMatrixClient(isAliasAvailable = false)
        val presenter = createConfigureRoomPresenter(
            matrixClient = fakeMatrixClient,
        )
        presenter.test {
            val initialState = initialState()
            initialState.eventSink(ConfigureRoomEvents.RoomVisibilityChanged(RoomVisibilityItem.Public))
            skipItems(1)
            initialState.eventSink(ConfigureRoomEvents.RoomAddressChanged("address"))
            skipItems(1)
            advanceUntilIdle()
            awaitItem().also { state ->
                assertThat(state.roomAddressValidity).isEqualTo(RoomAddressValidity.NotAvailable)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `present - address is valid when alias is available and format is valid`() = runTest {
        val fakeMatrixClient = createMatrixClient(isAliasAvailable = true)
        val presenter = createConfigureRoomPresenter(
            matrixClient = fakeMatrixClient,
        )
        presenter.test {
            val initialState = initialState()
            initialState.eventSink(ConfigureRoomEvents.RoomVisibilityChanged(RoomVisibilityItem.Public))
            skipItems(1)
            initialState.eventSink(ConfigureRoomEvents.RoomAddressChanged("address"))
            skipItems(1)
            advanceUntilIdle()
            awaitItem().also { state ->
                assertThat(state.roomAddressValidity).isEqualTo(RoomAddressValidity.Valid)
            }
        }
    }

    private suspend fun TurbineTestContext<ConfigureRoomState>.initialState(): ConfigureRoomState {
        skipItems(1)
        return awaitItem()
    }

    private fun createMatrixClient(isAliasAvailable: Boolean = true) = FakeMatrixClient(
        userIdServerNameLambda = { "matrix.org" },
        resolveRoomAliasResult = {
            val resolvedRoomAlias = if (isAliasAvailable) {
                Optional.empty()
            } else {
                Optional.of(ResolvedRoomAlias(A_ROOM_ID, emptyList()))
            }
            Result.success(resolvedRoomAlias)
        }
    )

    private fun createConfigureRoomPresenter(
        roomAliasHelper: RoomAliasHelper = FakeRoomAliasHelper(),
        dataStore: CreateRoomConfigStore = CreateRoomConfigStore(roomAliasHelper),
        matrixClient: MatrixClient = createMatrixClient(),
        pickerProvider: PickerProvider = FakePickerProvider(),
        mediaPreProcessor: MediaPreProcessor = FakeMediaPreProcessor(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        permissionsPresenter: PermissionsPresenter = FakePermissionsPresenter(),
        isKnockFeatureEnabled: Boolean = true,
        mediaOptimizationConfigProvider: FakeMediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
    ) = ConfigureRoomPresenter(
        dataStore = dataStore,
        matrixClient = matrixClient,
        mediaPickerProvider = pickerProvider,
        mediaPreProcessor = mediaPreProcessor,
        analyticsService = analyticsService,
        permissionsPresenterFactory = FakePermissionsPresenterFactory(permissionsPresenter),
        roomAliasHelper = roomAliasHelper,
        featureFlagService = FakeFeatureFlagService(
            mapOf(FeatureFlags.Knock.key to isKnockFeatureEnabled)
        ),
        mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
    )
}
