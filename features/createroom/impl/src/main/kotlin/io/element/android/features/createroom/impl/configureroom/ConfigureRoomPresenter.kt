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

package io.element.android.features.createroom.impl.configureroom

import android.net.Uri
import androidx.compose.runtime.Composable
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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
import io.element.android.libraries.matrix.api.createroom.RoomVisibility
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfigureRoomPresenter @Inject constructor(
    private val dataStore: CreateRoomDataStore,
    private val matrixClient: MatrixClient,
    private val mediaPickerProvider: PickerProvider,
    private val mediaPreProcessor: MediaPreProcessor,
    private val analyticsService: AnalyticsService,
) : Presenter<ConfigureRoomState> {

    @Composable
    override fun present(): ConfigureRoomState {
        val createRoomConfig = dataStore.getCreateRoomConfig().collectAsState(CreateRoomConfig())

        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri -> if (uri != null) dataStore.setAvatarUri(uri = uri, cached = true) },
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri -> if (uri != null) dataStore.setAvatarUri(uri = uri) }
        )

        val avatarActions by remember(createRoomConfig.value.avatarUri) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { createRoomConfig.value.avatarUri != null },
                ).toImmutableList()
            }
        }

        val localCoroutineScope = rememberCoroutineScope()
        val createRoomAction: MutableState<Async<RoomId>> = remember { mutableStateOf(Async.Uninitialized) }

        fun createRoom(config: CreateRoomConfig) {
            createRoomAction.value = Async.Uninitialized
            localCoroutineScope.createRoom(config, createRoomAction)
        }

        fun handleEvents(event: ConfigureRoomEvents) {
            when (event) {
                is ConfigureRoomEvents.RoomNameChanged -> dataStore.setRoomName(event.name)
                is ConfigureRoomEvents.TopicChanged -> dataStore.setTopic(event.topic)
                is ConfigureRoomEvents.RoomPrivacyChanged -> dataStore.setPrivacy(event.privacy)
                is ConfigureRoomEvents.RemoveFromSelection -> dataStore.selectedUserListDataStore.removeUserFromSelection(event.matrixUser)
                is ConfigureRoomEvents.CreateRoom -> createRoom(event.config)
                is ConfigureRoomEvents.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> cameraPhotoPicker.launch()
                        AvatarAction.Remove -> dataStore.setAvatarUri(uri = null)
                    }
                }

                ConfigureRoomEvents.CancelCreateRoom -> createRoomAction.value = Async.Uninitialized
            }
        }

        return ConfigureRoomState(
            config = createRoomConfig.value,
            avatarActions = avatarActions,
            createRoomAction = createRoomAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.createRoom(
        config: CreateRoomConfig,
        createRoomAction: MutableState<Async<RoomId>>
    ) = launch {
        suspend {
            val avatarUrl = config.avatarUri?.let { uploadAvatar(it) }
            val params = CreateRoomParameters(
                name = config.roomName,
                topic = config.topic,
                isEncrypted = config.privacy == RoomPrivacy.Private,
                isDirect = false,
                visibility = if (config.privacy == RoomPrivacy.Public) RoomVisibility.PUBLIC else RoomVisibility.PRIVATE,
                preset = if (config.privacy == RoomPrivacy.Public) RoomPreset.PUBLIC_CHAT else RoomPreset.PRIVATE_CHAT,
                invite = config.invites.map { it.userId },
                avatar = avatarUrl,
            )
            matrixClient.createRoom(params).getOrThrow()
                .also {
                    dataStore.clearCachedData()
                    analyticsService.capture(CreatedRoom(isDM = false))
                }
        }.runCatchingUpdatingState(createRoomAction)
    }

    private suspend fun uploadAvatar(avatarUri: Uri): String {
        val preprocessed = mediaPreProcessor.process(avatarUri, MimeTypes.Jpeg, compressIfPossible = false).getOrThrow()
        val byteArray = preprocessed.file.readBytes()
        return matrixClient.uploadMedia(MimeTypes.Jpeg, byteArray, null).getOrThrow()
    }
}
