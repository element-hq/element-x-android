/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.time

import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun isTalkbackActive(): Boolean {
    val context = LocalContext.current
    val accessibilityManager = remember { context.getSystemService(AccessibilityManager::class.java) }
    var isTouchExplorationEnabled by remember { mutableStateOf(accessibilityManager.isTouchExplorationEnabled) }
    DisposableEffect(Unit) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled ->
            isTouchExplorationEnabled = enabled
        }
        accessibilityManager.addTouchExplorationStateChangeListener(listener)
        onDispose {
            accessibilityManager.removeTouchExplorationStateChangeListener(listener)
        }
    }
    return isTouchExplorationEnabled
}
