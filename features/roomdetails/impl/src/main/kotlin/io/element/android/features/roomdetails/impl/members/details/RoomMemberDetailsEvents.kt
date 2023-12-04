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

package io.element.android.features.roomdetails.impl.members.details

sealed interface RoomMemberDetailsEvents {
    data object StartDM : RoomMemberDetailsEvents
    data object ClearStartDMState : RoomMemberDetailsEvents
    data class BlockUser(val needsConfirmation: Boolean = false) : RoomMemberDetailsEvents
    data class UnblockUser(val needsConfirmation: Boolean = false) : RoomMemberDetailsEvents
    data object ClearBlockUserError : RoomMemberDetailsEvents
    data object ClearConfirmationDialog : RoomMemberDetailsEvents
}
