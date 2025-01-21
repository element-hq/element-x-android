/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

/**
 * Represents the permissions a user has in a room.
 * It's dependent of the user's power level in the room.
 */
data class UserEventPermissions(
    val canRedactOwn: Boolean,
    val canRedactOther: Boolean,
    val canSendMessage: Boolean,
    val canSendReaction: Boolean,
    val canPinUnpin: Boolean,
) {
    companion object {
        val DEFAULT = UserEventPermissions(
            canRedactOwn = true,
            canRedactOther = false,
            canSendMessage = true,
            canSendReaction = true,
            canPinUnpin = false
        )
    }
}
