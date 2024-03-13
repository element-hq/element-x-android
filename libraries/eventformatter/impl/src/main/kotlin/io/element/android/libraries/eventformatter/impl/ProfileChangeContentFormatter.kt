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

package io.element.android.libraries.eventformatter.impl

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

class ProfileChangeContentFormatter @Inject constructor(
    private val sp: StringProvider,
) {
    fun format(
        senderId: UserId,
        profileChangeContent: ProfileChangeContent,
        senderDisplayName: String,
        senderIsYou: Boolean,
    ): String? = profileChangeContent.run {
        val displayNameChanged = displayName != prevDisplayName
        val avatarChanged = avatarUrl != prevAvatarUrl
        return when {
            avatarChanged && displayNameChanged -> {
                val message = format(senderId, profileChangeContent.copy(avatarUrl = null, prevAvatarUrl = null), senderDisplayName, senderIsYou)
                val avatarChangedToo = sp.getString(R.string.state_event_avatar_changed_too)
                "$message\n$avatarChangedToo"
            }
            displayNameChanged -> {
                if (displayName != null && prevDisplayName != null) {
                    if (senderIsYou) {
                        sp.getString(R.string.state_event_display_name_changed_from_by_you, prevDisplayName, displayName)
                    } else {
                        sp.getString(R.string.state_event_display_name_changed_from, senderId.value, prevDisplayName, displayName)
                    }
                } else if (displayName != null) {
                    if (senderIsYou) {
                        sp.getString(R.string.state_event_display_name_set_by_you, displayName)
                    } else {
                        sp.getString(R.string.state_event_display_name_set, senderId.value, displayName)
                    }
                } else {
                    if (senderIsYou) {
                        sp.getString(R.string.state_event_display_name_removed_by_you, prevDisplayName)
                    } else {
                        sp.getString(R.string.state_event_display_name_removed, senderId.value, prevDisplayName)
                    }
                }
            }
            avatarChanged -> {
                if (senderIsYou) {
                    sp.getString(R.string.state_event_avatar_url_changed_by_you)
                } else {
                    sp.getString(R.string.state_event_avatar_url_changed, senderDisplayName)
                }
            }
            else -> null
        }
    }
}
