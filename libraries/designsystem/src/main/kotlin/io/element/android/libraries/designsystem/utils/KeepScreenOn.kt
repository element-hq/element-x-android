/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn(
    keepScreenOn: Boolean = true
) {
    if (keepScreenOn) {
        val currentView = LocalView.current
        DisposableEffect(Unit) {
            currentView.keepScreenOn = true
            onDispose {
                currentView.keepScreenOn = false
            }
        }
    }
}
