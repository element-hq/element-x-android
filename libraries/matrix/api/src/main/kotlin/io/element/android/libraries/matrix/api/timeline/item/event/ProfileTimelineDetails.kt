/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
