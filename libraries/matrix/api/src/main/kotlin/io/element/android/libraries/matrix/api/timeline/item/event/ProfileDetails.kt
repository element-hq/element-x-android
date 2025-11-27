/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.timeline.item.event

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
sealed interface ProfileDetails {
    data object Unavailable : ProfileDetails

    data object Pending : ProfileDetails

    data class Ready(
        val displayName: String?,
        val displayNameAmbiguous: Boolean,
        val avatarUrl: String?
    ) : ProfileDetails

    data class Error(
        val message: String
    ) : ProfileDetails
}

/**
 * Returns a disambiguated display name for the user.
 * If the display name is null, or profile is not Ready, the user ID is returned.
 * If the display name is ambiguous, the user ID is appended in parentheses.
 * Otherwise, the display name is returned.
 */
fun ProfileDetails.getDisambiguatedDisplayName(userId: UserId): String {
    return when (this) {
        is ProfileDetails.Ready -> when {
            displayName == null -> userId.value
            displayNameAmbiguous -> "$displayName ($userId)"
            else -> displayName
        }
        else -> userId.value
    }
}

fun ProfileDetails.getDisplayName(): String? {
    return when (this) {
        is ProfileDetails.Ready -> displayName
        else -> null
    }
}

fun ProfileDetails.getAvatarUrl(): String? {
    return when (this) {
        is ProfileDetails.Ready -> avatarUrl
        else -> null
    }
}
