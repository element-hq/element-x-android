/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.api

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
    override val isInLabs: Boolean = false,
) : Feature {
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
    EnableKeyShareOnInvite(
        key = "feature.enableKeyShareOnInvite",
        title = "Share encrypted history with new members",
        description = "When inviting a user to an encrypted room that has history visibility set to \"shared\"," +
            " share encrypted history with that user, and accept encrypted history when you are invited to such a room." +
            "\nRequires an app restart to take effect." +
            "\n\nWARNING: this feature is EXPERIMENTAL and not all security precautions are implemented." +
            " Do not enable on production accounts.",
        defaultValue = { false },
        isFinished = false,
    ),
    Knock(
        key = "feature.knock",
        title = "Ask to join",
        description = "Allow creating rooms which users can request access to.",
        defaultValue = { false },
        isFinished = false,
    ),
    Space(
        key = "feature.space",
        title = "Spaces",
        defaultValue = { true },
        isFinished = false,
    ),
    PrintLogsToLogcat(
        key = "feature.print_logs_to_logcat",
        title = "Print logs to logcat",
        description = "Print logs to logcat in addition to log files. Requires an app restart to take effect." +
            "\n\nWARNING: this will make the logs visible in the device logs and may affect performance. " +
            "It's not intended for daily usage in release builds.",
        defaultValue = { buildMeta -> buildMeta.buildType != BuildType.RELEASE },
        // False so it's displayed in the developer options screen
        isFinished = false,
    ),
    SelectableMediaQuality(
        key = "feature.selectable_media_quality",
        title = "Select media quality per upload",
        description = "You can select the media quality for each attachment you upload.",
        defaultValue = { false },
        // False so it's displayed in the developer options screen
        isFinished = false,
    ),
    Threads(
        key = "feature.thread_timeline",
        title = "Threads",
        description = "Renders thread messages as a dedicated timeline. Restarting the app is required for this setting to fully take effect.",
        defaultValue = { false },
        isFinished = false,
        isInLabs = true,
    ),
    MultiAccount(
        key = "feature.multi_account",
        title = "Multi accounts",
        description = "Allow the application to connect to multiple accounts at the same time." +
            "\n\nWARNING: this feature is EXPERIMENTAL and UNSTABLE.",
        defaultValue = { false },
        isFinished = false,
    ),
    SyncNotificationsWithWorkManager(
        key = "feature.sync_notifications_with_workmanager",
        title = "Sync notifications with WorkManager",
        description = "Use WorkManager to schedule notification sync tasks when a push is received." +
            " This should improve reliability and battery usage.",
        defaultValue = { true },
        isFinished = false,
    ),
}
