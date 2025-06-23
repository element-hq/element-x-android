/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.canSendState
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.matrix.ui.room.avatarUrl
import io.element.android.libraries.matrix.ui.room.rawName
import io.element.android.libraries.matrix.ui.room.topic
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RoomDetailsEditPresenter @Inject constructor(
    private val room: JoinedRoom,
    private val mediaPickerProvider: PickerProvider,
    private val mediaPreProcessor: MediaPreProcessor,
    private val temporaryUriDeleter: TemporaryUriDeleter,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) : Presenter<RoomDetailsEditState> {
    private val cameraPermissionPresenter = permissionsPresenterFactory.create(android.Manifest.permission.CAMERA)
    private var pendingPermissionRequest = false

    @Composable
    override fun present(): RoomDetailsEditState {
        val cameraPermissionState = cameraPermissionPresenter.present()
        val roomSyncUpdateFlow = room.syncUpdateFlow.collectAsState()

        val roomAvatarUri = room.avatarUrl()?.toUri()
        var roomAvatarUriEdited by rememberSaveable { mutableStateOf<Uri?>(null) }
        LaunchedEffect(roomAvatarUri) {
            // Every time the roomAvatar change (from sync), we can set the new avatar.
            temporaryUriDeleter.delete(roomAvatarUriEdited)
            roomAvatarUriEdited = roomAvatarUri
        }

        val roomRawNameTrimmed = room.rawName().orEmpty().trim()
        var roomRawNameEdited by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(roomRawNameTrimmed) {
            // Every time the rawName change (from sync), we can set the new name.
            roomRawNameEdited = roomRawNameTrimmed
        }
        val roomTopicTrimmed = room.topic().orEmpty().trim()
        var roomTopicEdited by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(roomTopicTrimmed) {
            // Every time the topic change (from sync), we can set the new topic.
            roomTopicEdited = roomTopicTrimmed
        }

        val saveButtonEnabled by remember(
            roomRawNameTrimmed,
            roomTopicTrimmed,
            roomAvatarUri,
        ) {
            derivedStateOf {
                roomRawNameTrimmed != roomRawNameEdited.trim() ||
                    roomTopicTrimmed != roomTopicEdited.trim() ||
                    roomAvatarUri != roomAvatarUriEdited
            }
        }

        var canChangeName by remember { mutableStateOf(false) }
        var canChangeTopic by remember { mutableStateOf(false) }
        var canChangeAvatar by remember { mutableStateOf(false) }

        LaunchedEffect(roomSyncUpdateFlow.value) {
            canChangeName = room.canSendState(StateEventType.ROOM_NAME).getOrElse { false }
            canChangeTopic = room.canSendState(StateEventType.ROOM_TOPIC).getOrElse { false }
            canChangeAvatar = room.canSendState(StateEventType.ROOM_AVATAR).getOrElse { false }
        }

        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri ->
                if (uri != null) {
                    temporaryUriDeleter.delete(roomAvatarUriEdited)
                    roomAvatarUriEdited = uri
                }
            }
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri ->
                if (uri != null) {
                    temporaryUriDeleter.delete(roomAvatarUriEdited)
                    roomAvatarUriEdited = uri
                }
            }
        )

        LaunchedEffect(cameraPermissionState.permissionGranted) {
            if (cameraPermissionState.permissionGranted && pendingPermissionRequest) {
                pendingPermissionRequest = false
                cameraPhotoPicker.launch()
            }
        }

        val avatarActions by remember(roomAvatarUriEdited) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { roomAvatarUriEdited != null },
                ).toImmutableList()
            }
        }

        val saveAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val localCoroutineScope = rememberCoroutineScope()
        fun handleEvents(event: RoomDetailsEditEvents) {
            when (event) {
                is RoomDetailsEditEvents.Save -> localCoroutineScope.saveChanges(
                    currentNameTrimmed = roomRawNameTrimmed,
                    newNameTrimmed = roomRawNameEdited.trim(),
                    currentTopicTrimmed = roomTopicTrimmed,
                    newTopicTrimmed = roomTopicEdited.trim(),
                    currentAvatar = roomAvatarUri,
                    newAvatarUri = roomAvatarUriEdited,
                    action = saveAction,
                )
                is RoomDetailsEditEvents.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> if (cameraPermissionState.permissionGranted) {
                            cameraPhotoPicker.launch()
                        } else {
                            pendingPermissionRequest = true
                            cameraPermissionState.eventSink(PermissionsEvents.RequestPermissions)
                        }
                        AvatarAction.Remove -> {
                            temporaryUriDeleter.delete(roomAvatarUriEdited)
                            roomAvatarUriEdited = null
                        }
                    }
                }

                is RoomDetailsEditEvents.UpdateRoomName -> roomRawNameEdited = event.name
                is RoomDetailsEditEvents.UpdateRoomTopic -> roomTopicEdited = event.topic
                RoomDetailsEditEvents.CancelSaveChanges -> saveAction.value = AsyncAction.Uninitialized
            }
        }

        return RoomDetailsEditState(
            roomId = room.roomId,
            roomRawName = roomRawNameEdited,
            canChangeName = canChangeName,
            roomTopic = roomTopicEdited,
            canChangeTopic = canChangeTopic,
            roomAvatarUrl = roomAvatarUriEdited,
            canChangeAvatar = canChangeAvatar,
            avatarActions = avatarActions,
            saveButtonEnabled = saveButtonEnabled,
            saveAction = saveAction.value,
            cameraPermissionState = cameraPermissionState,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.saveChanges(
        currentNameTrimmed: String,
        newNameTrimmed: String,
        currentTopicTrimmed: String,
        newTopicTrimmed: String,
        currentAvatar: Uri?,
        newAvatarUri: Uri?,
        action: MutableState<AsyncAction<Unit>>,
    ) = launch {
        val results = mutableListOf<Result<Unit>>()
        suspend {
            if (newTopicTrimmed != currentTopicTrimmed) {
                results.add(room.setTopic(newTopicTrimmed).onFailure {
                    Timber.e(it, "Failed to set room topic")
                })
            }
            if (newNameTrimmed.isNotEmpty() && newNameTrimmed != currentNameTrimmed) {
                results.add(room.setName(newNameTrimmed).onFailure {
                    Timber.e(it, "Failed to set room name")
                })
            }
            if (newAvatarUri != currentAvatar) {
                results.add(updateAvatar(newAvatarUri).onFailure {
                    Timber.e(it, "Failed to update avatar")
                })
            }
            if (results.all { it.isSuccess }) Unit else results.first { it.isFailure }.getOrThrow()
        }.runCatchingUpdatingState(action)
    }

    private suspend fun updateAvatar(avatarUri: Uri?): Result<Unit> {
        return runCatchingExceptions {
            if (avatarUri != null) {
                val preprocessed = mediaPreProcessor.process(
                    uri = avatarUri,
                    mimeType = MimeTypes.Jpeg,
                    deleteOriginal = false,
                    compressIfPossible = false,
                ).getOrThrow()
                room.updateAvatar(MimeTypes.Jpeg, preprocessed.file.readBytes()).getOrThrow()
            } else {
                room.removeAvatar().getOrThrow()
            }
        }
    }
}
