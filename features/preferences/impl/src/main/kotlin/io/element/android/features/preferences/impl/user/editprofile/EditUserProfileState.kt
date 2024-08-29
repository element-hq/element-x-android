/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import android.net.Uri
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.permissions.api.PermissionsState
import kotlinx.collections.immutable.ImmutableList

data class EditUserProfileState(
    val isDebugBuild: Boolean,
    val userId: UserId,
    val displayName: String,
    val userAvatarUrl: Uri?,
    val avatarActions: ImmutableList<AvatarAction>,
    val saveButtonEnabled: Boolean,
    val saveAction: AsyncAction<Unit>,
    val cameraPermissionState: PermissionsState,
    val eventSink: (EditUserProfileEvents) -> Unit
)
