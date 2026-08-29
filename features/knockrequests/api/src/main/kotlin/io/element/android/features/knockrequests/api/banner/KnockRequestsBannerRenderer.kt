/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.api.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders the banner shown at the top of a room when people are waiting to join it.
 */
interface KnockRequestsBannerRenderer {
    /**
     * Draws the banner, or nothing when there is no pending request or the user cannot moderate the room.
     *
     * @param modifier layout modifier for the container.
     * @param onViewRequestsClick called when the user asks to see the full list of requests.
     */
    @Composable
    fun View(modifier: Modifier, onViewRequestsClick: () -> Unit)
}
