/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.impl

import io.element.android.libraries.matrix.api.permalink.PermalinkData

private const val BUBBLE_ICON = "💬"

/**
 * Format the permalink the same way the mention pills are formatted in the timeline,
 * when the display name of the room or of the user is not known.
 */
internal fun PermalinkData.toPillText(): String? {
    return when (this) {
        is PermalinkData.RoomLink -> {
            if (eventId != null || threadId != null) {
                "$BUBBLE_ICON > ${roomIdOrAlias.identifier}"
            } else {
                roomIdOrAlias.identifier
            }
        }
        is PermalinkData.UserLink -> userId.value
        is PermalinkData.FallbackLink,
        is PermalinkData.RoomEmailInviteLink -> null
    }
}
