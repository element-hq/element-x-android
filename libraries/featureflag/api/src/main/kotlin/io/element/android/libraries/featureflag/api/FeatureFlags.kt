/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.featureflag.api

import io.element.android.appconfig.OnBoardingConfig
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType

/**
 * To enable or disable a FeatureFlags, change the `defaultValue` value.
 */
enum class FeatureFlags(
    override val key: String,
    override val title: String,
    override val description: String? = null,
    override val defaultValue: (BuildMeta) -> Boolean,
    override val isFinished: Boolean,
) : Feature {
    LocationSharing(
        key = "feature.locationsharing",
        title = "Allow user to share location",
        defaultValue = { true },
        isFinished = true,
    ),
    Polls(
        key = "feature.polls",
        title = "Polls",
        description = "Create poll and render poll events in the timeline",
        defaultValue = { true },
        isFinished = true,
    ),
    NotificationSettings(
        key = "feature.notificationsettings",
        title = "Show notification settings",
        defaultValue = { true },
        isFinished = true,
    ),
    VoiceMessages(
        key = "feature.voicemessages",
        title = "Voice messages",
        description = "Send and receive voice messages",
        defaultValue = { true },
        isFinished = true,
    ),
    PinUnlock(
        key = "feature.pinunlock",
        title = "Pin unlock",
        description = "Allow user to lock/unlock the app with a pin code or biometrics",
        defaultValue = { true },
        isFinished = true,
    ),
    Mentions(
        key = "feature.mentions",
        title = "Mentions",
        description = "Type `@` to get mention suggestions and insert them",
        defaultValue = { true },
        isFinished = false,
    ),
    RoomAliasSuggestions(
        key = "feature.roomAliasSuggestions",
        title = "Room alias suggestions",
        description = "Type `#` to get room alias suggestions and insert them",
        defaultValue = { false },
        isFinished = false,
    ),
    MarkAsUnread(
        key = "feature.markAsUnread",
        title = "Mark as unread",
        description = "Allow user to mark a room as unread",
        defaultValue = { true },
        isFinished = false,
    ),
    RoomDirectorySearch(
        key = "feature.roomdirectorysearch",
        title = "Room directory search",
        description = "Allow user to search for public rooms in their homeserver",
        defaultValue = { false },
        isFinished = false,
    ),
    ShowBlockedUsersDetails(
        key = "feature.showBlockedUsersDetails",
        title = "Show blocked users details",
        description = "Show the name and avatar of blocked users in the blocked users list",
        defaultValue = { false },
        isFinished = false,
    ),
    QrCodeLogin(
        key = "feature.qrCodeLogin",
        title = "Enable login using QR code",
        description = "Allow the user to login using the QR code flow",
        defaultValue = { OnBoardingConfig.CAN_LOGIN_WITH_QR_CODE },
        isFinished = false,
    ),
    IncomingShare(
        key = "feature.incomingShare",
        title = "Incoming Share support",
        description = "Allow the application to receive data from other applications",
        defaultValue = { true },
        isFinished = false,
    ),
    PinnedEvents(
        key = "feature.pinnedEvents",
        title = "Pinned Events",
        description = "Allow user to pin events in a room",
        defaultValue = { true },
        isFinished = false,
    ),
    SyncOnPush(
        key = "feature.syncOnPush",
        title = "Sync on push",
        description = "Subscribe to room sync when a push is received",
        defaultValue = { true },
        isFinished = false,
    ),
    OnlySignedDeviceIsolationMode(
        key = "feature.onlySignedDeviceIsolationMode",
        title = "Exclude insecure devices when sending/receiving messages",
        description = "This setting controls how end-to-end encryption (E2E) keys are shared." +
            " Enabling it will prevent the inclusion of devices that have not been explicitly verified by their owners." +
            " You'll have to stop and re-open the app manually for that setting to take effect.",
        defaultValue = { false },
        isFinished = false,
    ),
    IdentityPinningViolationNotifications(
        key = "feature.identityPinningViolationNotifications",
        title = "Identity pinning violation notifications",
        description = null,
        defaultValue = { buildMeta ->
            when (buildMeta.buildType) {
                // Do not enable this feature in release builds
                BuildType.RELEASE -> false
                else -> true
            }
        },
        isFinished = false,
    ),
}
