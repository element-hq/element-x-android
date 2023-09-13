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

package io.element.android.features.preferences.impl.user.screen

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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.getCurrentUser
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserPreferencesPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val mediaPickerProvider: PickerProvider,
    private val mediaPreProcessor: MediaPreProcessor,
) : Presenter<UserPreferencesState> {

    @Composable
    override fun present(): UserPreferencesState {
        var currentUser by remember { mutableStateOf<MatrixUser?>(null) }
        var userAvatarUri by rememberSaveable(currentUser) { mutableStateOf(currentUser?.avatarUrl?.let { Uri.parse(it) }) }
        var userDisplayName by rememberSaveable(currentUser) { mutableStateOf(currentUser?.displayName) }
        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker(
            onResult = { uri -> if (uri != null) userAvatarUri = uri }
        )
        val galleryImagePicker = mediaPickerProvider.registerGalleryImagePicker(
            onResult = { uri -> if (uri != null) userAvatarUri = uri }
        )

        LaunchedEffect(Unit) {
            currentUser = matrixClient.getCurrentUser()
        }

        val avatarActions by remember(userAvatarUri) {
            derivedStateOf {
                listOfNotNull(
                    AvatarAction.TakePhoto,
                    AvatarAction.ChoosePhoto,
                    AvatarAction.Remove.takeIf { userAvatarUri != null },
                ).toImmutableList()
            }
        }

        val saveAction: MutableState<Async<Unit>> = remember { mutableStateOf(Async.Uninitialized) }
        val localCoroutineScope = rememberCoroutineScope()
        fun handleEvents(event: UserPreferencesEvents) {
            when (event) {
                is UserPreferencesEvents.Save -> currentUser?.let {
                    localCoroutineScope.saveChanges(userDisplayName, userAvatarUri, it, saveAction)
                }
                is UserPreferencesEvents.HandleAvatarAction -> {
                    when (event.action) {
                        AvatarAction.ChoosePhoto -> galleryImagePicker.launch()
                        AvatarAction.TakePhoto -> cameraPhotoPicker.launch()
                        AvatarAction.Remove -> userAvatarUri = null
                    }
                }

                is UserPreferencesEvents.UpdateDisplayName -> userDisplayName = event.name
                UserPreferencesEvents.CancelSaveChanges -> saveAction.value = Async.Uninitialized
            }
        }

        val canSave = remember(userDisplayName, userAvatarUri, currentUser) {
            val hasProfileChanged = hasDisplayNameChanged(userDisplayName, currentUser)
                    || hasAvatarUrlChanged(userAvatarUri, currentUser)
            !userDisplayName.isNullOrBlank() && hasProfileChanged
        }

        return UserPreferencesState(
            userId = currentUser?.userId,
            displayName = userDisplayName.orEmpty(),
            userAvatarUrl = userAvatarUri,
            avatarActions = avatarActions,
            saveButtonEnabled = canSave && saveAction.value !is Async.Loading,
            saveAction = saveAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun hasDisplayNameChanged(name: String?, currentUser: MatrixUser?) = name?.trim() != currentUser?.displayName?.trim()
    private fun hasAvatarUrlChanged(avatarUri: Uri?, currentUser: MatrixUser?) = avatarUri?.toString()?.trim() != currentUser?.avatarUrl?.trim()

    private fun CoroutineScope.saveChanges(name: String?, avatarUri: Uri?, currentUser: MatrixUser, action: MutableState<Async<Unit>>) = launch {
        matrixClient.getCurrentUser()
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
        }
    }
}
