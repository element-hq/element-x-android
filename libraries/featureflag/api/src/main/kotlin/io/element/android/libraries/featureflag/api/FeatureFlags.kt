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
        description = "Spaces are under active development, only developers should enable this flog for now.",
        defaultValue = { false },
        isFinished = false,
    ),
    MediaUploadOnSendQueue(
        key = "feature.media_upload_through_send_queue",
        title = "Media upload through send queue",
        description = "Support for treating media uploads as regular events, with an improved retry and cancellation implementation.",
        defaultValue = { true },
        isFinished = true,
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
    SharePos(
        key = "feature.share_pos_v2",
        title = "Share pos in sliding sync",
        description = "Keep the sliding sync pos to make initial syncs faster. Requires an app restart to take effect." +
            "\n\nWARNING: this may cause issues with syncs.",
        defaultValue = { true },
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
}
