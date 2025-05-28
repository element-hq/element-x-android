/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
sealed interface ProfileTimelineDetails {
    data object Unavailable : ProfileTimelineDetails

    data object Pending : ProfileTimelineDetails

    data class Ready(
        val displayName: String?,
        val displayNameAmbiguous: Boolean,
        val avatarUrl: String?
    ) : ProfileTimelineDetails

    data class Error(
        val message: String
    ) : ProfileTimelineDetails
}

/**
 * Returns a disambiguated display name for the user.
 * If the display name is null, or profile is not Ready, the user ID is returned.
 * If the display name is ambiguous, the user ID is appended in parentheses.
 * Otherwise, the display name is returned.
 */
fun ProfileTimelineDetails.getDisambiguatedDisplayName(userId: UserId): String {
    return when (this) {
        is ProfileTimelineDetails.Ready -> when {
            displayName == null -> userId.value
            displayNameAmbiguous -> "$displayName ($userId)"
            else -> displayName
        }
        else -> userId.value
    }
}

fun ProfileTimelineDetails.getDisplayName(): String? {
    return when (this) {
        is ProfileTimelineDetails.Ready -> displayName
        else -> null
    }
}

fun ProfileTimelineDetails.getAvatarUrl(): String? {
    return when (this) {
        is ProfileTimelineDetails.Ready -> avatarUrl
        else -> null
    }
}
