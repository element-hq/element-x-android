/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Displays the content of [block] after a delay of [duration].
 */
@Composable
fun DelayedVisibility(
    duration: Duration = 300.milliseconds,
    block: @Composable () -> Unit,
) {
    // Technically this shouldn't be needed because `LocalInspectionMode` won't change, but let's make the linter happy
    val movableBlock = remember { movableContentOf { block() } }
    if (LocalInspectionMode.current) {
        // Just allow the contents to be displayed in the previews/screenshot tests
        movableBlock()
    } else {
        var shouldDisplay by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(duration)
            shouldDisplay = true
        }
        AnimatedVisibility(shouldDisplay) {
            movableBlock()
        }
    }
}
