/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
