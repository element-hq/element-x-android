/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
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
        title = stringResource(R.string.banner_battery_optimization_title_android),
        description = stringResource(R.string.banner_battery_optimization_content_android),
        type = AnnouncementType.Actionable(
            actionText = stringResource(R.string.banner_battery_optimization_submit_android),
            onActionClick = { state.eventSink(BatteryOptimizationEvents.RequestDisableOptimizations) },
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
