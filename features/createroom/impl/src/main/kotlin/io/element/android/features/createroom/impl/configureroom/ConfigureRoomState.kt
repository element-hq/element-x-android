/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import io.element.android.features.createroom.impl.CreateRoomConfig
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.permissions.api.PermissionsState
import kotlinx.collections.immutable.ImmutableList

data class ConfigureRoomState(
    val config: CreateRoomConfig,
    val avatarActions: ImmutableList<AvatarAction>,
    val createRoomAction: AsyncAction<RoomId>,
    val cameraPermissionState: PermissionsState,
    val eventSink: (ConfigureRoomEvents) -> Unit
) {
    val isCreateButtonEnabled: Boolean = config.roomName.isNullOrEmpty().not()
}
