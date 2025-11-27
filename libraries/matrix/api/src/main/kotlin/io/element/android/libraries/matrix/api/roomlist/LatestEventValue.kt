/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails

sealed interface LatestEventValue {
    data object None : LatestEventValue
    data class Remote(
        val timestamp: Long,
        val content: EventContent,
        val senderId: UserId,
        val senderProfile: ProfileDetails,
        val isOwn: Boolean,
    ) : LatestEventValue

    data class Local(
        val timestamp: Long,
        val content: EventContent,
        val senderId: UserId,
        val senderProfile: ProfileDetails,
        val isSending: Boolean,
    ) : LatestEventValue
}
