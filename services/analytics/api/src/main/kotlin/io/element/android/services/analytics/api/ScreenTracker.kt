/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import androidx.compose.runtime.Composable
import im.vector.app.features.analytics.plan.MobileScreen

/**
 * Reports screen views to analytics from Composable code.
 */
interface ScreenTracker {
    /**
     * Records a view of [screen] when this enters composition, so it is called once per appearance rather than on every recomposition.
     *
     * @param screen the screen being shown.
     */
    @Composable
    fun TrackScreen(
        screen: MobileScreen.ScreenName,
    )
}
