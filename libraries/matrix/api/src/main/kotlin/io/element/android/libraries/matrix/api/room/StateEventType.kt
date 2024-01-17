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

package io.element.android.libraries.matrix.api.room

enum class StateEventType {
    POLICY_RULE_ROOM,
    POLICY_RULE_SERVER,
    POLICY_RULE_USER,
    CALL_MEMBER,
    ROOM_ALIASES,
    ROOM_AVATAR,
    ROOM_CANONICAL_ALIAS,
    ROOM_CREATE,
    ROOM_ENCRYPTION,
    ROOM_GUEST_ACCESS,
    ROOM_HISTORY_VISIBILITY,
    ROOM_JOIN_RULES,
    ROOM_MEMBER_EVENT,
    ROOM_NAME,
    ROOM_PINNED_EVENTS,
    ROOM_POWER_LEVELS,
    ROOM_SERVER_ACL,
    ROOM_THIRD_PARTY_INVITE,
    ROOM_TOMBSTONE,
    ROOM_TOPIC,
    SPACE_CHILD,
    SPACE_PARENT
}
