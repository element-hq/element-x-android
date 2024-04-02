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

package io.element.android.appconfig

import io.element.android.libraries.matrix.api.room.StateEventType

object TimelineConfig {
    const val MAX_READ_RECEIPT_TO_DISPLAY = 3

    /**
     * Event types that will be filtered out from the timeline (i.e. not displayed).
     */
    val excludedEvents = listOf(
        StateEventType.ROOM_ALIASES,
        StateEventType.ROOM_CANONICAL_ALIAS,
        StateEventType.ROOM_GUEST_ACCESS,
        StateEventType.ROOM_HISTORY_VISIBILITY,
        StateEventType.ROOM_JOIN_RULES,
        StateEventType.ROOM_PINNED_EVENTS,
        StateEventType.ROOM_POWER_LEVELS,
        StateEventType.ROOM_SERVER_ACL,
        StateEventType.ROOM_TOMBSTONE,
        StateEventType.SPACE_CHILD,
        StateEventType.SPACE_PARENT,
        StateEventType.POLICY_RULE_ROOM,
        StateEventType.POLICY_RULE_SERVER,
        StateEventType.POLICY_RULE_USER,
    )
}
