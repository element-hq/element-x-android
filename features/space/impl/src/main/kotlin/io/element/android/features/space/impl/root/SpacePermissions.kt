/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import io.element.android.features.space.impl.settings.SpaceSettingsPermissions
import io.element.android.features.space.impl.settings.spaceSettingsPermissions
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

/**
 * Permissions needed for different actions in the Space screen.
 * @param settingsPermissions Permissions related to space settings.
 * @param canEditSpaceGraph Whether the user can edit the space graph (add/remove children).
 */
data class SpacePermissions(
    val settingsPermissions: SpaceSettingsPermissions,
    val canEditSpaceGraph: Boolean,
) {
    companion object {
        val DEFAULT = SpacePermissions(
            settingsPermissions = SpaceSettingsPermissions.DEFAULT,
            canEditSpaceGraph = false,
        )
    }
}

fun RoomPermissions.spacePermissions(): SpacePermissions {
    return SpacePermissions(
        settingsPermissions = spaceSettingsPermissions(),
        canEditSpaceGraph = canOwnUserSendState(StateEventType.SpaceChild) || canOwnUserSendState(StateEventType.SpaceParent),
    )
}
