/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetailsedit.impl

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.permissions.api.PermissionsState
import kotlinx.collections.immutable.ImmutableList

data class RoomDetailsEditState(
    val roomId: RoomId,
    /** The raw room name (i.e. the room name from the state event `m.room.name`), not the display name. */
    val roomRawName: String,
    val canChangeName: Boolean,
    val roomTopic: String,
    val canChangeTopic: Boolean,
    val roomAvatarUrl: String?,
    val canChangeAvatar: Boolean,
    val avatarActions: ImmutableList<AvatarAction>,
    val saveButtonEnabled: Boolean,
    val saveAction: AsyncAction<Unit>,
    val cameraPermissionState: PermissionsState,
    val isSpace: Boolean,
    val eventSink: (RoomDetailsEditEvent) -> Unit
)
