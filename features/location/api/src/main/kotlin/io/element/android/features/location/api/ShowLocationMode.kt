/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.AssetType
import kotlinx.parcelize.Parcelize

sealed interface ShowLocationMode : Parcelable {
    @Parcelize
    data class Static(
        val location: Location,
        val senderName: String,
        val senderId: UserId,
        val senderAvatarUrl: String?,
        val timestamp: Long,
        val assetType: AssetType?,
    ) : ShowLocationMode

    @Parcelize
    data object Live : ShowLocationMode
}
