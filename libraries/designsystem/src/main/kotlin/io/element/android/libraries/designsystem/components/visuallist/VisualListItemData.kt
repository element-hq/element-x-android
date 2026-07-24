/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.visuallist

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class VisualListItemData(
    val message: CharSequence,
    @DrawableRes val iconId: Int? = null,
    val iconVector: ImageVector? = null,
    val iconComposable: @Composable () -> Unit = {},
)
