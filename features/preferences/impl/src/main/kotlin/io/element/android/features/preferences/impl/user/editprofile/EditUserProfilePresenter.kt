/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class EditUserProfilePresenter @AssistedInject constructor(
    @Assisted private val matrixUser: MatrixUser,
    private val matrixClient: MatrixClient,
    private val buildMeta: BuildMeta,
    private val mediaPickerProvider: PickerProvider,
    private val mediaPreProcessor: MediaPreProcessor,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) : Presenter<EditUserProfileState> {
    private val cameraPermissionPresenter: PermissionsPresenter = permissionsPresenterFactory.create(android.Manifest.permission.CAMERA)
    private var pendingPermissionRequest = false

    @AssistedFactory
    interface Factory {
        fun create(matrixUser: MatrixUser): EditUserProfilePresenter
    }

    @Composable
    override fun present(): EditUserProfileState {
        val cameraPermissionState = cameraPermissionPresenter.present()
        var userAvatarUri by rememberSaveable { mutableStateOf(matrixUser.avatarUrl?.let { Uri.parse(it) }) }
        var userDisplayName by rememberSaveable { mutableStateOf(matrixUser.displayName) }
        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri -> if (uri != null) userAvatarUri = uri }
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri -> if (uri != null) userAvatarUri = uri }
        )

        val avatarActions by remember(userAvatarUri) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { userAvatarUri != null },
                ).toImmutableList()
            }
        }

        LaunchedEffect(cameraPermissionState.permissionGranted) {
            if (cameraPermissionState.permissionGranted && pendingPermissionRequest) {
                pendingPermissionRequest = false
                cameraPhotoPicker.launch()
            }
        }

        val saveAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val localCoroutineScope = rememberCoroutineScope()
        fun handleEvents(event: EditUserProfileEvents) {
            when (event) {
                is EditUserProfileEvents.Save -> localCoroutineScope.saveChanges(userDisplayName, userAvatarUri, matrixUser, saveAction)
                is EditUserProfileEvents.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> if (cameraPermissionState.permissionGranted) {
                            cameraPhotoPicker.launch()
                        } else {
                            pendingPermissionRequest = true
                            cameraPermissionState.eventSink(PermissionsEvents.RequestPermissions)
                        }
                        AvatarAction.Remove -> userAvatarUri = null
                    }
                }

                is EditUserProfileEvents.UpdateDisplayName -> userDisplayName = event.name
                EditUserProfileEvents.CancelSaveChanges -> saveAction.value = AsyncAction.Uninitialized
            }
        }

        val canSave = remember(userDisplayName, userAvatarUri) {
            val hasProfileChanged = hasDisplayNameChanged(userDisplayName, matrixUser) ||
                hasAvatarUrlChanged(userAvatarUri, matrixUser)
            !userDisplayName.isNullOrBlank() && hasProfileChanged
        }

        return EditUserProfileState(
            isDebugBuild = buildMeta.isDebuggable,
            userId = matrixUser.userId,
            displayName = userDisplayName.orEmpty(),
            userAvatarUrl = userAvatarUri,
            avatarActions = avatarActions,
            saveButtonEnabled = canSave && saveAction.value !is AsyncAction.Loading,
            saveAction = saveAction.value,
            cameraPermissionState = cameraPermissionState,
            eventSink = { handleEvents(it) },
        )
    }

    private fun hasDisplayNameChanged(name: String?, currentUser: MatrixUser) =
        name?.trim() != currentUser.displayName?.trim()

    private fun hasAvatarUrlChanged(avatarUri: Uri?, currentUser: MatrixUser) =
        // Need to call `toUri()?.toString()` to make the test pass (we mockk Uri)
        avatarUri?.toString()?.trim() != currentUser.avatarUrl?.toUri()?.toString()?.trim()

    private fun CoroutineScope.saveChanges(
        name: String?,
        avatarUri: Uri?,
        currentUser: MatrixUser,
        action: MutableState<AsyncAction<Unit>>,
    ) = launch {
        val results = mutableListOf<Result<Unit>>()
        suspend {
            if (!name.isNullOrEmpty() && name.trim() != currentUser.displayName.orEmpty().trim()) {
                results.add(matrixClient.setDisplayName(name).onFailure {
                    Timber.e(it, "Failed to set user's display name")
                })
            }
            if (avatarUri?.toString()?.trim() != currentUser.avatarUrl?.trim()) {
                results.add(updateAvatar(avatarUri).onFailure {
                    Timber.e(it, "Failed to update user's avatar")
                })
            }
            if (results.all { it.isSuccess }) Unit else results.first { it.isFailure }.getOrThrow()
        }.runCatchingUpdatingState(action)
    }

    private suspend fun updateAvatar(avatarUri: Uri?): Result<Unit> {
        return runCatching {
            if (avatarUri != null) {
                val preprocessed = mediaPreProcessor.process(avatarUri, MimeTypes.Jpeg, compressIfPossible = false).getOrThrow()
                matrixClient.uploadAvatar(MimeTypes.Jpeg, preprocessed.file.readBytes()).getOrThrow()
            } else {
                matrixClient.removeAvatar().getOrThrow()
            }
        }.onFailure { Timber.e(it, "Unable to update avatar") }
    }
}
