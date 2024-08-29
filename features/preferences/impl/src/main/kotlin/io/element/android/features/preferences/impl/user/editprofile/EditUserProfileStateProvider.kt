/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.permissions.api.aPermissionsState
import kotlinx.collections.immutable.persistentListOf

open class EditUserProfileStateProvider : PreviewParameterProvider<EditUserProfileState> {
    override val values: Sequence<EditUserProfileState>
        get() = sequenceOf(
            aEditUserProfileState(),
            // Add other states here
        )
}

fun aEditUserProfileState() = EditUserProfileState(
    isDebugBuild = false,
    userId = UserId("@john.doe:matrix.org"),
    displayName = "John Doe",
    userAvatarUrl = null,
    avatarActions = persistentListOf(),
    saveAction = AsyncAction.Uninitialized,
    saveButtonEnabled = true,
    cameraPermissionState = aPermissionsState(showDialog = false),
    eventSink = {}
)
