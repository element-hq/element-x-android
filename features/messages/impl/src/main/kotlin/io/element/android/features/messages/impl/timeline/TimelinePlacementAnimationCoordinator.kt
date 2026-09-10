/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Coordinates LazyColumn [androidx.compose.foundation.lazy.LazyItemScope.animateItem] placement
 * with in-item size animations such as grouped-events expand/collapse.
 *
 * While a grouped row's [androidx.compose.animation.animateContentSize] is running, neighboring
 * items must not run their own placement animation or the two fight and overlap.
 */
@Stable
internal class TimelinePlacementAnimationCoordinator {
    var suppressPlacementAnimation by mutableIntStateOf(0)
        private set

    val isPlacementAnimationEnabled: Boolean
        get() = suppressPlacementAnimation == 0

    fun onContentSizeAnimationStarted() {
        suppressPlacementAnimation++
    }

    fun onContentSizeAnimationFinished() {
        if (suppressPlacementAnimation > 0) {
            suppressPlacementAnimation--
        }
    }
}

internal val LocalTimelinePlacementAnimationCoordinator =
    staticCompositionLocalOf<TimelinePlacementAnimationCoordinator?> { null }

@Composable
internal fun timelineItemPlacementSpecEnabled(): Boolean =
    LocalTimelinePlacementAnimationCoordinator.current?.isPlacementAnimationEnabled != false
