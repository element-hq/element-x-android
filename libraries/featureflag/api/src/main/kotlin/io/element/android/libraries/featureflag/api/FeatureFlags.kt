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

package io.element.android.libraries.featureflag.api

/**
 * To enable or disable a FeatureFlags, change the `defaultValue` value.
 * Warning: to enable a flag for the release app, you MUST update the file
 * [io.element.android.libraries.featureflag.impl.StaticFeatureFlagProvider]
 */
enum class FeatureFlags(
    override val key: String,
    override val title: String,
    override val description: String? = null,
    override val defaultValue: Boolean,
    override val isFinished: Boolean,
) : Feature {
    LocationSharing(
        key = "feature.locationsharing",
        title = "Allow user to share location",
        defaultValue = true,
        isFinished = true,
    ),
    Polls(
        key = "feature.polls",
        title = "Polls",
        description = "Create poll and render poll events in the timeline",
        defaultValue = true,
        isFinished = true,
    ),
    NotificationSettings(
        key = "feature.notificationsettings",
        title = "Show notification settings",
        defaultValue = true,
        isFinished = true,
    ),
    VoiceMessages(
        key = "feature.voicemessages",
        title = "Voice messages",
        description = "Send and receive voice messages",
        defaultValue = true,
        isFinished = true,
    ),
    PinUnlock(
        key = "feature.pinunlock",
        title = "Pin unlock",
        description = "Allow user to lock/unlock the app with a pin code or biometrics",
        defaultValue = true,
        isFinished = true,
    ),
    Mentions(
        key = "feature.mentions",
        title = "Mentions",
        description = "Type `@` to get mention suggestions and insert them",
        defaultValue = true,
        isFinished = false,
    ),
    MarkAsUnread(
        key = "feature.markAsUnread",
        title = "Mark as unread",
        description = "Allow user to mark a room as unread",
        defaultValue = true,
        isFinished = false,
    ),
    RoomListFilters(
        key = "feature.roomlistfilters",
        title = "Room list filters",
        description = "Allow user to filter the room list",
        defaultValue = true,
        isFinished = false,
    ),
}
