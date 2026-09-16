/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetailsedit.impl

import android.Manifest
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
import dev.zacsweers.metro.Inject
import io.element.android.features.roomdetailsedit.api.RoomDetailsEditPermissions
import io.element.android.features.roomdetailsedit.api.roomDetailsEditPermissions
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsEvent
import io.element.android.libraries.permissions.api.PermissionsPresenter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@Inject
class RoomDetailsEditPresenter(
    private val room: JoinedRoom,
    private val mediaPickerProvider: PickerProvider,
    private val mediaPreProcessor: MediaPreProcessor,
    private val temporaryUriDeleter: TemporaryUriDeleter,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val mediaOptimizationConfigProvider: MediaOptimizationConfigProvider,
) : Presenter<RoomDetailsEditState> {
    private val cameraPermissionPresenter = permissionsPresenterFactory.create(Manifest.permission.CAMERA)
    private var pendingPermissionRequest = false

    @Composable
    override fun present(): RoomDetailsEditState {
        val cameraPermissionState = cameraPermissionPresenter.present()
        val roomInfo by room.roomInfoFlow.collectAsState()
        val roomAvatarUri = roomInfo.avatarUrl
        var roomAvatarUriEdited by rememberSaveable { mutableStateOf<String?>(null) }
        LaunchedEffect(roomAvatarUri) {
            // Every time the roomAvatar change (from sync), we can set the new avatar.
            temporaryUriDeleter.delete(roomAvatarUriEdited?.toUri())
            roomAvatarUriEdited = roomAvatarUri
        }

        val roomRawNameTrimmed = roomInfo.rawName.orEmpty().trim()
        var roomRawNameEdited by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(roomRawNameTrimmed) {
            // Every time the rawName change (from sync), we can set the new name.
            roomRawNameEdited = roomRawNameTrimmed
        }
        val roomTopicTrimmed = roomInfo.topic.orEmpty().trim()
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

        val permissions by room.permissionsAsState(RoomDetailsEditPermissions.DEFAULT) { perms ->
            perms.roomDetailsEditPermissions()
        }

        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri ->
                if (uri != null) {
                    temporaryUriDeleter.delete(roomAvatarUriEdited?.toUri())
                    roomAvatarUriEdited = uri.toString()
                }
            }
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri ->
                if (uri != null) {
                    temporaryUriDeleter.delete(roomAvatarUriEdited?.toUri())
                    roomAvatarUriEdited = uri.toString()
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
        fun handleEvent(event: RoomDetailsEditEvent) {
            when (event) {
                is RoomDetailsEditEvent.Save -> localCoroutineScope.saveChanges(
                    currentNameTrimmed = roomRawNameTrimmed,
                    newNameTrimmed = roomRawNameEdited.trim(),
                    currentTopicTrimmed = roomTopicTrimmed,
                    newTopicTrimmed = roomTopicEdited.trim(),
                    currentAvatar = roomAvatarUri?.toUri(),
                    newAvatarUri = roomAvatarUriEdited?.toUri(),
                    action = saveAction,
                )
                is RoomDetailsEditEvent.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> if (cameraPermissionState.permissionGranted) {
                            cameraPhotoPicker.launch()
                        } else {
                            pendingPermissionRequest = true
                            cameraPermissionState.eventSink(PermissionsEvent.RequestPermissions)
                        }
                        AvatarAction.Remove -> {
                            temporaryUriDeleter.delete(roomAvatarUriEdited?.toUri())
                            roomAvatarUriEdited = null
                        }
                    }
                }

                is RoomDetailsEditEvent.UpdateRoomName -> roomRawNameEdited = event.name
                is RoomDetailsEditEvent.UpdateRoomTopic -> roomTopicEdited = event.topic
                RoomDetailsEditEvent.CloseDialog -> saveAction.value = AsyncAction.Uninitialized
                RoomDetailsEditEvent.OnBackPress -> if (saveButtonEnabled.not() || saveAction.value == AsyncAction.ConfirmingCancellation) {
                    // No changes to save or already confirming exit without saving
                    saveAction.value = AsyncAction.Success(Unit)
                } else {
                    saveAction.value = AsyncAction.ConfirmingCancellation
                }
            }
        }

        return RoomDetailsEditState(
            roomId = room.roomId,
            roomRawName = roomRawNameEdited,
            canChangeName = permissions.canEditName,
            roomTopic = roomTopicEdited,
            canChangeTopic = permissions.canEditTopic,
            roomAvatarUrl = roomAvatarUriEdited,
            canChangeAvatar = permissions.canEditAvatar,
            avatarActions = avatarActions,
            saveButtonEnabled = saveButtonEnabled,
            saveAction = saveAction.value,
            cameraPermissionState = cameraPermissionState,
            isSpace = roomInfo.isSpace,
            eventSink = ::handleEvent,
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
                    mediaOptimizationConfig = mediaOptimizationConfigProvider.get(),
                ).getOrThrow()
                room.updateAvatar(MimeTypes.Jpeg, preprocessed.file.readBytes()).getOrThrow()
            } else {
                room.removeAvatar().getOrThrow()
            }
        }
    }
}
