/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.room

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.parcelize.Parcelize

sealed interface RoomNavigationTarget : Parcelable {
    @Parcelize
    data class Messages(val focusedEventId: EventId? = null) : RoomNavigationTarget

    @Parcelize
    data object Details : RoomNavigationTarget

    @Parcelize
    data object NotificationSettings : RoomNavigationTarget
}
