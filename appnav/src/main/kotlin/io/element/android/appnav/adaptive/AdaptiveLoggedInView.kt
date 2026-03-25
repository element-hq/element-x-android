/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.adaptive

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Provides an adaptive layout that shows a dual-pane view on expanded (tablet/foldable)
 * screens and a single-pane view on compact (phone) screens.
 *
 * On expanded screens:
 * - The [listPane] (Home / room list) is shown in the left pane (360dp fixed width)
 * - The [detailPane] (BackstackView with Room/Settings/etc.) is shown in the right pane
 *
 * On compact screens:
 * - Only [singlePane] is shown (the full BackstackView containing Home → Room flow)
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveLoggedInView(
    isExpanded: Boolean = currentWindowAdaptiveInfo().windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded,
    listPane: @Composable (Modifier) -> Unit,
    detailPane: @Composable (Modifier) -> Unit,
    singlePane: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isExpanded) {
        Row(modifier = modifier.fillMaxSize()) {
            listPane(
                Modifier
                    .width(360.dp)
                    .fillMaxHeight()
            )
            VerticalDivider(
                modifier = Modifier.fillMaxHeight()
            )
            detailPane(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    } else {
        singlePane(modifier)
    }
}
