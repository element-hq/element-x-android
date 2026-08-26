/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.appsettings

import androidx.compose.runtime.Composable
import io.element.android.libraries.designsystem.components.preferences.DropdownOption
import io.element.android.libraries.preferences.api.store.RoomListActivityVisibility

enum class RoomListActivityVisibilityItem : DropdownOption {
    CURRENT {
        @Composable
        override fun getText(): String = "Badges and emphasis"
    },
    SHOW {
        @Composable
        override fun getText(): String = "Emphasis only"
    },
    HIDE {
        @Composable
        override fun getText(): String = "Nothing"
    }
}

fun RoomListActivityVisibilityItem.toRoomListActivityVisibility(): RoomListActivityVisibility = when (this) {
    RoomListActivityVisibilityItem.CURRENT -> RoomListActivityVisibility.CURRENT
    RoomListActivityVisibilityItem.SHOW -> RoomListActivityVisibility.SHOW
    RoomListActivityVisibilityItem.HIDE -> RoomListActivityVisibility.HIDE
}

fun RoomListActivityVisibility.toRoomListActivityVisibilityItem(): RoomListActivityVisibilityItem = when (this) {
    RoomListActivityVisibility.CURRENT -> RoomListActivityVisibilityItem.CURRENT
    RoomListActivityVisibility.SHOW -> RoomListActivityVisibilityItem.SHOW
    RoomListActivityVisibility.HIDE -> RoomListActivityVisibilityItem.HIDE
}
