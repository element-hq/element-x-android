/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ForceOrientationInMobileDevices(orientation: ScreenOrientation) {
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    if (windowAdaptiveInfo.windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact ||
        windowAdaptiveInfo.windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    ) {
        ForceOrientation(orientation = orientation)
    }
}
