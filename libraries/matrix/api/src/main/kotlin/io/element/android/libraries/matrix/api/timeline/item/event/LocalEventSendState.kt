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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Immutable
sealed interface LocalEventSendState {
    data object Sending : LocalEventSendState
    sealed interface Failed : LocalEventSendState {
        data class Unknown(val error: String) : Failed
        data class VerifiedUserHasUnsignedDevice(
            /**
             * The unsigned devices belonging to verified users. A map from user ID
             * to a list of device IDs.
             */
            val devices: Map<UserId, List<String>>
        ) : Failed

        data class VerifiedUserChangedIdentity(
            /**
             * The users that were previously verified but are no longer.
             */
            val users: List<UserId>
        ) : Failed
    }
    data class Sent(
        val eventId: EventId
    ) : LocalEventSendState
}
