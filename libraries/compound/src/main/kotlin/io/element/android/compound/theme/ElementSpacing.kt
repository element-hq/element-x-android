/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * M3-aligned spacing scale for consistent layout spacing throughout the app.
 *
 * Use these tokens instead of hardcoded dp values for padding, margins, and gaps.
 */
object ElementSpacing {
    /** 2.dp — hairline gaps, icon-to-badge offsets */
    val xxs: Dp = 2.dp

    /** 4.dp — tight internal padding, small chip gaps */
    val xs: Dp = 4.dp

    /** 8.dp — standard internal spacing, list item gaps */
    val s: Dp = 8.dp

    /** 12.dp — medium internal padding, section gaps */
    val m: Dp = 12.dp

    /** 16.dp — standard screen-edge padding, card padding */
    val l: Dp = 16.dp

    /** 24.dp — section separators, dialog padding */
    val xl: Dp = 24.dp

    /** 32.dp — large section breaks, screen top/bottom padding */
    val xxl: Dp = 32.dp
}
