/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.features.home.impl.R
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.push.api.battery.PushNotificationsWarningEvents
import io.element.android.libraries.push.api.battery.PushNotificationsWarningState
import io.element.android.libraries.push.api.battery.aBatteryOptimizationState

@Composable
internal fun PushServerRateLimitedBanner(
    pushServer: String,
    pushDistributorAppName: String,
    onNavigateToPushDistributor: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Announcement(
        modifier = modifier.roomListBannerPadding(),
        // TODO: use actual string resource when available
        title = "Notifications may not arrive",
        // TODO: use actual string resource when available
        description = String.format("Your chosen push server '%1\$s' may block some notifications. Try selecting a different push server in your push notification distributor app.", pushServer),
        type = AnnouncementType.Actionable(
            // TODO: use actual string resource when available
            actionText = String.format("Open %1\$s", pushDistributorAppName),
            onActionClick = onNavigateToPushDistributor,
            onDismissClick = onDismiss,
        ),
    )
}

@PreviewsDayNight
@Composable
internal fun PushServerRateLimitedBannerPreview() = ElementPreview {
    PushServerRateLimitedBanner(
        pushServer = "nrfy.sh",
        pushDistributorAppName = "ntfy",
        onNavigateToPushDistributor = {},
        onDismiss = {},
    )
}
