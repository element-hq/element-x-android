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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
        var avatarUrl by rememberSaveable { mutableStateOf(room.avatarUrl) }
        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri -> if (uri?.path != null) avatarUrl = uri.path }
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri -> if (uri?.path != null) avatarUrl = uri.path }
        )

        val avatarActions by remember(avatarUrl) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { avatarUrl != null },
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
                        AvatarAction.Remove -> avatarUrl = null
                    }
                }
            }
        }

        return RoomDetailsEditionState(
            roomId = room.roomId.value,
            roomName = room.name ?: room.displayName,
            roomAvatarUrl = avatarUrl,
            avatarActions = avatarActions,
            saveButtonVisible = true,
            eventSink = ::handleEvents,
        )
    }
}
