/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import io.element.android.libraries.androidutils.system.setFullBrightness

@Composable
fun ForceMaxBrightness() {
    val activity = LocalActivity.current ?: return
    DisposableEffect(Unit) {
        activity.setFullBrightness(true)
        onDispose {
            activity.setFullBrightness(false)
        }
    }
}
