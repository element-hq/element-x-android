/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.features.createroom.impl.CreateRoomDataStore
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
import io.element.android.libraries.matrix.api.room.alias.RoomAliasHelper
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidityEffect
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.jvm.optionals.getOrDefault

class ConfigureRoomPresenter @Inject constructor(
    private val dataStore: CreateRoomDataStore,
    private val matrixClient: MatrixClient,
    private val mediaPickerProvider: PickerProvider,
    private val mediaPreProcessor: MediaPreProcessor,
    private val analyticsService: AnalyticsService,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val featureFlagService: FeatureFlagService,
    private val roomAliasHelper: RoomAliasHelper,
) : Presenter<ConfigureRoomState> {
    private val cameraPermissionPresenter: PermissionsPresenter = permissionsPresenterFactory.create(android.Manifest.permission.CAMERA)
    private var pendingPermissionRequest = false

    @Composable
    override fun present(): ConfigureRoomState {
        val cameraPermissionState = cameraPermissionPresenter.present()
        val createRoomConfig by dataStore.createRoomConfigWithInvites.collectAsState(CreateRoomConfig())
        val homeserverName = remember { matrixClient.userIdServerName() }
        val isKnockFeatureEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.Knock)
        }.collectAsState(initial = false)
        val roomAddressValidity = remember {
            mutableStateOf<RoomAddressValidity>(RoomAddressValidity.Unknown)
        }

        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri -> if (uri != null) dataStore.setAvatarUri(uri = uri, cached = true) },
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri -> if (uri != null) dataStore.setAvatarUri(uri = uri) }
        )

        val avatarActions by remember(createRoomConfig.avatarUri) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { createRoomConfig.avatarUri != null },
                ).toImmutableList()
            }
        }

        LaunchedEffect(cameraPermissionState.permissionGranted) {
            if (cameraPermissionState.permissionGranted && pendingPermissionRequest) {
                pendingPermissionRequest = false
                cameraPhotoPicker.launch()
            }
        }

        RoomAddressValidityEffect(
            client = matrixClient,
            roomAliasHelper = roomAliasHelper,
            newRoomAddress = createRoomConfig.roomVisibility.roomAddress().getOrDefault(""),
            knownRoomAddress = null,
        ) { newRoomAddressValidity ->
            roomAddressValidity.value = newRoomAddressValidity
        }

        val localCoroutineScope = rememberCoroutineScope()
        val createRoomAction: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun createRoom(config: CreateRoomConfig) {
            createRoomAction.value = AsyncAction.Uninitialized
            localCoroutineScope.createRoom(config, createRoomAction)
        }

        fun handleEvents(event: ConfigureRoomEvents) {
            when (event) {
                is ConfigureRoomEvents.RoomNameChanged -> dataStore.setRoomName(event.name)
                is ConfigureRoomEvents.TopicChanged -> dataStore.setTopic(event.topic)
                is ConfigureRoomEvents.RoomVisibilityChanged -> dataStore.setRoomVisibility(event.visibilityItem)
                is ConfigureRoomEvents.RemoveUserFromSelection -> dataStore.selectedUserListDataStore.removeUserFromSelection(event.matrixUser)
                is ConfigureRoomEvents.RoomAccessChanged -> dataStore.setRoomAccess(event.roomAccess)
                is ConfigureRoomEvents.RoomAddressChanged -> dataStore.setRoomAddress(event.roomAddress)
                is ConfigureRoomEvents.CreateRoom -> createRoom(createRoomConfig)
                is ConfigureRoomEvents.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> if (cameraPermissionState.permissionGranted) {
                            cameraPhotoPicker.launch()
                        } else {
                            pendingPermissionRequest = true
                            cameraPermissionState.eventSink(PermissionsEvents.RequestPermissions)
                        }
                        AvatarAction.Remove -> dataStore.setAvatarUri(uri = null)
                    }
                }

                ConfigureRoomEvents.CancelCreateRoom -> createRoomAction.value = AsyncAction.Uninitialized
            }
        }

        return ConfigureRoomState(
            isKnockFeatureEnabled = isKnockFeatureEnabled,
            config = createRoomConfig,
            avatarActions = avatarActions,
            createRoomAction = createRoomAction.value,
            cameraPermissionState = cameraPermissionState,
            homeserverName = homeserverName,
            roomAddressValidity = roomAddressValidity.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.createRoom(
        config: CreateRoomConfig,
        createRoomAction: MutableState<AsyncAction<RoomId>>
    ) = launch {
        suspend {
            val avatarUrl = config.avatarUri?.let { uploadAvatar(it) }
            val params = if (config.roomVisibility is RoomVisibilityState.Public) {
                CreateRoomParameters(
                    name = config.roomName,
                    topic = config.topic,
                    isEncrypted = false,
                    isDirect = false,
                    visibility = RoomVisibility.Public,
                    joinRuleOverride = config.roomVisibility.roomAccess.toJoinRule(),
                    preset = RoomPreset.PUBLIC_CHAT,
                    invite = config.invites.map { it.userId },
                    avatar = avatarUrl,
                    roomAliasName = config.roomVisibility.roomAddress()
                )
            } else {
                CreateRoomParameters(
                    name = config.roomName,
                    topic = config.topic,
                    isEncrypted = config.roomVisibility is RoomVisibilityState.Private,
                    isDirect = false,
                    visibility = RoomVisibility.Private,
                    historyVisibilityOverride = RoomHistoryVisibility.Invited,
                    preset = RoomPreset.PRIVATE_CHAT,
                    invite = config.invites.map { it.userId },
                    avatar = avatarUrl,
                )
            }
            matrixClient.createRoom(params)
                .onFailure { failure ->
                    Timber.e(failure, "Failed to create room")
                }
                .onSuccess {
                    dataStore.clearCachedData()
                    analyticsService.capture(CreatedRoom(isDM = false))
                }
                .getOrThrow()
        }.runCatchingUpdatingState(createRoomAction)
    }

    private suspend fun uploadAvatar(avatarUri: Uri): String {
        val preprocessed = mediaPreProcessor.process(
            uri = avatarUri,
            mimeType = MimeTypes.Jpeg,
            deleteOriginal = false,
            compressIfPossible = false,
        ).getOrThrow()
        val byteArray = preprocessed.file.readBytes()
        return matrixClient.uploadMedia(MimeTypes.Jpeg, byteArray, null).getOrThrow()
    }
}
