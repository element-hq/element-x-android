/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.ScreenTracker
import io.element.android.services.toolbox.api.systemclock.SystemClock

@ContributesBinding(AppScope::class)
class DefaultScreenTracker(
    private val analyticsService: AnalyticsService,
    private val systemClock: SystemClock,
) : ScreenTracker {
    @Composable
    override fun TrackScreen(
        screen: MobileScreen.ScreenName,
    ) {
        var startTime by remember { mutableLongStateOf(0L) }
        OnLifecycleEvent { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    startTime = systemClock.epochMillis()
                }
                Lifecycle.Event.ON_PAUSE -> analyticsService.screen(
                    screen = MobileScreen(
                        durationMs = (systemClock.epochMillis() - startTime).toInt(),
                        screenName = screen
                    )
                )
                else -> Unit
            }
        }
    }
}
