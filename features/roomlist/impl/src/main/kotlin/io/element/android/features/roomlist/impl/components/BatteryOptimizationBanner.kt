/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.components.Announcement
import io.element.android.libraries.designsystem.components.AnnouncementType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.push.api.battery.BatteryOptimizationEvents
import io.element.android.libraries.push.api.battery.BatteryOptimizationState
import io.element.android.libraries.push.api.battery.aBatteryOptimizationState

@Composable
internal fun BatteryOptimizationBanner(
    state: BatteryOptimizationState,
    modifier: Modifier = Modifier,
) {
    Announcement(
        modifier = modifier.roomListBannerPadding(),
        // TODO Localazy
        title = "Notification tip",
        description = "To be sure to receive all the notifications, it can help to disable the battery optimization for this application.",
        type = AnnouncementType.Actionable(
            actionText = "Yes, disable",
            onActionClick = { state.eventSink(BatteryOptimizationEvents.DoAction) },
            onDismissClick = { state.eventSink(BatteryOptimizationEvents.Dismiss) },
        ),
    )
}

@PreviewsDayNight
@Composable
internal fun BatteryOptimizationBannerPreview() = ElementPreview {
    BatteryOptimizationBanner(
        state = aBatteryOptimizationState(),
    )
}
