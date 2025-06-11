/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer

import android.os.Build
import android.view.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.libraries.androidutils.ui.awaitWindowFocus
import io.element.android.libraries.androidutils.ui.isKeyboardVisible
import io.element.android.libraries.androidutils.ui.showKeyboard

/**
 * Shows the soft keyboard when a given key changes to meet the required condition.
 *
 * Uses [showKeyboard] to show the keyboard for compatibility with [AndroidView].
 *
 * @param T
 * @param key The key to watch for changes.
 * @param onRequestFocus A callback to request focus to the view that will receive the keyboard input.
 * @param predicate The predicate that [key] must meet before showing the keyboard.
 */
@Composable
internal fun <T> SoftKeyboardEffect(
    key: T,
    onRequestFocus: () -> Unit,
    predicate: (T) -> Boolean,
) {
    val view = LocalView.current
    val latestOnRequestFocus by rememberUpdatedState(onRequestFocus)
    val latestPredicate by rememberUpdatedState(predicate)
    LaunchedEffect(key) {
        if (latestPredicate(key)) {
            // Await window focus in case returning from a dialog
            view.awaitWindowFocus()

            if (!view.isKeyboardVisible()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.windowInsetsController?.show(WindowInsets.Type.ime())
                } else {
                    // Show the keyboard, temporarily using the root view for focus
                    view.showKeyboard(andRequestFocus = true)
                }

                // Refocus to the correct view
                latestOnRequestFocus()
            }
        }
    }
}
