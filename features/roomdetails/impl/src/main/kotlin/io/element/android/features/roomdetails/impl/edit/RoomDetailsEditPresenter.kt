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

package io.element.android.features.roomdetails.impl.edit

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RoomDetailsEditPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val mediaPickerProvider: PickerProvider,
    private val mediaPreProcessor: MediaPreProcessor,
) : Presenter<RoomDetailsEditState> {

    @Composable
    override fun present(): RoomDetailsEditState {
        val roomSyncUpdateFlow = room.syncUpdateFlow().collectAsState(0L)

        // Since there is no way to obtain the new avatar uri after uploading a new avatar,
        // just erase the local value when the room field has changed
        var roomAvatarUri by rememberSaveable(room.avatarUrl) { mutableStateOf(room.avatarUrl?.toUri()) }

        var roomName by rememberSaveable { mutableStateOf((room.name ?: room.displayName).trim()) }
        var roomTopic by rememberSaveable { mutableStateOf(room.topic?.trim()) }

        val saveButtonEnabled by remember(
            roomSyncUpdateFlow.value,
            roomName,
            roomTopic,
            roomAvatarUri,
        ) {
            derivedStateOf {
                roomAvatarUri?.toString()?.trim() != room.avatarUrl?.toUri()?.toString()?.trim()
                    || roomName.trim() != (room.name ?: room.displayName).trim()
                    || roomTopic.orEmpty().trim() != room.topic.orEmpty().trim()
            }
        }

        var canChangeName by remember { mutableStateOf(false) }
        var canChangeTopic by remember { mutableStateOf(false) }
        var canChangeAvatar by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            canChangeName = room.canSendStateEvent(StateEventType.ROOM_NAME).getOrElse { false }
            canChangeTopic = room.canSendStateEvent(StateEventType.ROOM_TOPIC).getOrElse { false }
            canChangeAvatar = room.canSendStateEvent(StateEventType.ROOM_AVATAR).getOrElse { false }
        }

        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri -> if (uri != null) roomAvatarUri = uri }
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri -> if (uri != null) roomAvatarUri = uri }
        )

        val avatarActions by remember(roomAvatarUri) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { roomAvatarUri != null },
                ).toImmutableList()
            }
        }

        val saveAction: MutableState<Async<Unit>> = remember { mutableStateOf(Async.Uninitialized) }
        val localCoroutineScope = rememberCoroutineScope()
        fun handleEvents(event: RoomDetailsEditEvents) {
            when (event) {
                is RoomDetailsEditEvents.Save -> localCoroutineScope.saveChanges(roomName, roomTopic, roomAvatarUri, saveAction)
                is RoomDetailsEditEvents.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> cameraPhotoPicker.launch()
                        AvatarAction.Remove -> roomAvatarUri = null
                    }
                }

                is RoomDetailsEditEvents.UpdateRoomName -> roomName = event.name
                is RoomDetailsEditEvents.UpdateRoomTopic -> roomTopic = event.topic.takeUnless { it.isEmpty() }
                RoomDetailsEditEvents.CancelSaveChanges -> saveAction.value = Async.Uninitialized
            }
        }

        return RoomDetailsEditState(
            roomId = room.roomId.value,
            roomName = roomName,
            canChangeName = canChangeName,
            roomTopic = roomTopic.orEmpty(),
            canChangeTopic = canChangeTopic,
            roomAvatarUrl = roomAvatarUri,
            canChangeAvatar = canChangeAvatar,
            avatarActions = avatarActions,
            saveButtonEnabled = saveButtonEnabled,
            saveAction = saveAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.saveChanges(name: String, topic: String?, avatarUri: Uri?, action: MutableState<Async<Unit>>) = launch {
        val results = mutableListOf<Result<Unit>>()
        suspend {
            if (topic.orEmpty().trim() != room.topic.orEmpty().trim()) {
                results.add(room.setTopic(topic.orEmpty()).onFailure {
                    Timber.e(it, "Failed to set room topic")
                })
            }
            if (name.isNotEmpty() && name.trim() != room.name.orEmpty().trim()) {
                results.add(room.setName(name).onFailure {
                    Timber.e(it, "Failed to set room name")
                })
            }
            if (avatarUri?.toString()?.trim() != room.avatarUrl?.trim()) {
                results.add(updateAvatar(avatarUri).onFailure {
                    Timber.e(it, "Failed to update avatar")
                })
            }
            if (results.all { it.isSuccess }) Unit else results.first { it.isFailure }.getOrThrow()
        }.runCatchingUpdatingState(action)
    }

    private suspend fun updateAvatar(avatarUri: Uri?): Result<Unit> {
        return runCatching {
            if (avatarUri != null) {
                val preprocessed = mediaPreProcessor.process(avatarUri, MimeTypes.Jpeg, compressIfPossible = false).getOrThrow()
                room.updateAvatar(MimeTypes.Jpeg, preprocessed.file.readBytes()).getOrThrow()
            } else {
                room.removeAvatar().getOrThrow()
            }
        }
    }
}
