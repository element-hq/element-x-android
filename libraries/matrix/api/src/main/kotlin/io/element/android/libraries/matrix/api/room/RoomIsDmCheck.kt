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
 * Returns whether the room with the provided info is a DM.
 * A DM is a room with at most 2 active members (one of them may have left).
 *
 * @param isDirect true if the room is direct
 * @param activeMembersCount the number of active members in the room (joined or invited)
 */
fun isDm(isDirect: Boolean, activeMembersCount: Int): Boolean {
    return isDirect && activeMembersCount <= 2
}

/**
 * Returns whether the [MatrixRoom] is a DM.
 */
val MatrixRoom.isDm get() = isDm(isDirect, activeMemberCount.toInt())

/**
 * Returns whether the [MatrixRoomInfo] is from a DM.
 */
val MatrixRoomInfo.isDm get() = isDm(isDirect, activeMembersCount.toInt())
