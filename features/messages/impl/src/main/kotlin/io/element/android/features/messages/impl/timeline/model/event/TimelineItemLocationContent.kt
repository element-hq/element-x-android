/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.features.location.api.Location
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisplayName

data class TimelineItemLocationContent(
    val senderId: UserId,
    val senderProfile: ProfileDetails,
    val description: String? = null,
    val assetType: AssetType? = null,
    val mode: Mode,
) : TimelineItemEventContent {

    val hideTimestamp = mode is Mode.Live && mode.canStop

    val location = when (mode) {
        is Mode.Live -> mode.lastKnownLocation
        is Mode.Static -> mode.location
    }

    /**
     * The pin variant to display on the map.
     * Returns a default variant when location is null (map will show loading placeholder anyway).
     */
    val pinVariant: PinVariant = when (mode) {
        is Mode.Live -> {
            if (mode.isActive) {
                PinVariant.UserLocation(avatarData = senderAvatar(), isLive = true)
            } else {
                PinVariant.StaleLocation
            }
        }
        is Mode.Static -> {
            when (assetType) {
                AssetType.PIN -> PinVariant.PinnedLocation
                AssetType.SENDER,
                AssetType.UNKNOWN,
                null -> PinVariant.UserLocation(avatarData = senderAvatar(), isLive = false)
            }
        }
    }

    private fun senderAvatar() = AvatarData(
        senderId.value,
        name = senderProfile.getDisplayName(),
        url = senderProfile.getAvatarUrl(),
        size = AvatarSize.LocationPin
    )

    sealed interface Mode {
        data class Static(
            val location: Location,
        ) : Mode

        data class Live(
            val lastKnownLocation: Location?,
            val isActive: Boolean,
            val endsAt: String,
            val canStop: Boolean = false
        ) : Mode {
            val isLoading = lastKnownLocation == null && isActive
        }
    }

    override val type: String = "TimelineItemLocationContent"
}
