/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.api.room

/**
 * Verifies if a room is considered a direct message one for Element.
 */
object DmChecker {
    /**
     * Returns whether the room with the provided info is a DM.
     * A DM is a room with at most 2 active members (one of them may have left), and it's encrypted.
     *
     * @param isDirect true if the room is direct
     * @param activeMembersCount the number of active members in the room (joined or invited)
     * @param isEncrypted true if the room is encrypted
     */
    fun isDm(isDirect: Boolean, activeMembersCount: Int, isEncrypted: Boolean): Boolean {
        return isDirect && activeMembersCount <= 2 && isEncrypted
    }
}
