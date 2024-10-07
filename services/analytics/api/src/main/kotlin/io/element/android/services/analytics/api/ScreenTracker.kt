/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.analytics.api

import androidx.compose.runtime.Composable
import im.vector.app.features.analytics.plan.MobileScreen

interface ScreenTracker {
    @Composable
    fun TrackScreen(
        screen: MobileScreen.ScreenName,
    )
}
