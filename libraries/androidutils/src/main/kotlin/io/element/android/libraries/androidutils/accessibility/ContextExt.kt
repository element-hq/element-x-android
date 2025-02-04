/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

/**
 * Whether a screen reader is enabled.
 *
 * Avoid changing UI or app behavior based on the state of accessibility.
 * See [AccessibilityManager.isTouchExplorationEnabled] for more details.
 *
 * @return true if the screen reader is enabled.
 */
fun Context.isScreenReaderEnabled(): Boolean {
    val accessibilityManager = getSystemService<AccessibilityManager>()
        ?: return false

    return accessibilityManager.let {
        it.isEnabled && it.isTouchExplorationEnabled
    }
}
