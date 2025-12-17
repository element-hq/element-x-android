/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.media.AvatarAction
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity
import io.element.android.libraries.permissions.api.PermissionsState
import kotlinx.collections.immutable.ImmutableList

data class ConfigureRoomState(
    val isKnockFeatureEnabled: Boolean,
    val config: CreateRoomConfig,
    val avatarActions: ImmutableList<AvatarAction>,
    val createRoomAction: AsyncAction<RoomId>,
    val cameraPermissionState: PermissionsState,
    val roomAddressValidity: RoomAddressValidity,
    val homeserverName: String,
    val eventSink: (ConfigureRoomEvents) -> Unit
) {
    val isValid: Boolean = config.roomName?.isNotEmpty() == true &&
        (config.roomVisibility is RoomVisibilityState.Private || roomAddressValidity == RoomAddressValidity.Valid)
}
