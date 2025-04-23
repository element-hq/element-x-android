/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.ui.utils.time

import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun isTalkbackActive(): Boolean {
    val context = LocalContext.current
    val accessibilityManager = remember { context.getSystemService(AccessibilityManager::class.java) }
    return accessibilityManager.isTouchExplorationEnabled
}
