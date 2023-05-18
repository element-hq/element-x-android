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

package io.element.android.features.roomdetails.impl.edition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import io.element.android.features.createroom.api.ui.AvatarAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.mediapickers.api.PickerProvider
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class RoomDetailsEditionPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val mediaPickerProvider: PickerProvider,
) : Presenter<RoomDetailsEditionState> {

    @Composable
    override fun present(): RoomDetailsEditionState {
        var roomAvatarUrl by rememberSaveable { mutableStateOf(room.avatarUrl?.toUri()) }
        var roomName by rememberSaveable { mutableStateOf((room.name ?: room.displayName).trim()) }
        var roomTopic by rememberSaveable { mutableStateOf(room.topic?.trim()) }
        val saveButtonVisible by remember(roomAvatarUrl, roomName, roomTopic) {
            derivedStateOf {
                roomAvatarUrl?.toString()?.trim() != room.avatarUrl?.toUri()?.toString()?.trim()
                    || roomName != (room.name ?: room.displayName).trim()
                    || roomTopic != room.topic?.trim()
            }
        }

        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri -> if (uri != null) roomAvatarUrl = uri }
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri -> if (uri != null) roomAvatarUrl = uri }
        )

        val avatarActions by remember(roomAvatarUrl) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { roomAvatarUrl != null },
                ).toImmutableList()
            }
        }

        fun handleEvents(event: RoomDetailsEditionEvents) {
            when (event) {
                RoomDetailsEditionEvents.Save -> Unit
                is RoomDetailsEditionEvents.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> cameraPhotoPicker.launch()
                        AvatarAction.Remove -> roomAvatarUrl = null
                    }
                }

                is RoomDetailsEditionEvents.UpdateRoomName -> roomName = event.name
                is RoomDetailsEditionEvents.UpdateRoomTopic -> roomTopic = event.topic.takeUnless { it.isEmpty() }
            }
        }

        return RoomDetailsEditionState(
            roomId = room.roomId.value,
            roomName = roomName,
            roomTopic = roomTopic.orEmpty(),
            roomAvatarUrl = roomAvatarUrl,
            avatarActions = avatarActions,
            saveButtonVisible = saveButtonVisible,
            eventSink = ::handleEvents,
        )
    }
}
