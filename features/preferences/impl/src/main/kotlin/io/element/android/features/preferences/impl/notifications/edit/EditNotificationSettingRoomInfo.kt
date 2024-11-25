/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.collections.immutable.ImmutableList

data class EditNotificationSettingRoomInfo(
    val roomId: RoomId,
    val name: String?,
    val heroesAvatar: ImmutableList<AvatarData>,
    val avatarData: AvatarData,
    val notificationMode: RoomNotificationMode?
)
