/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.tooltip

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipScope
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.material3.TooltipBox as M3TooltipBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipBox(
    positionProvider: PopupPositionProvider,
    tooltip: @Composable TooltipScope.() -> Unit,
    state: TooltipState,
    modifier: Modifier = Modifier,
    focusable: Boolean = true,
    enableUserInput: Boolean = true,
    content: @Composable () -> Unit,
) = M3TooltipBox(
    positionProvider = positionProvider,
    tooltip = tooltip,
    state = state,
    modifier = modifier,
    focusable = focusable,
    enableUserInput = enableUserInput,
    content = content,
)
