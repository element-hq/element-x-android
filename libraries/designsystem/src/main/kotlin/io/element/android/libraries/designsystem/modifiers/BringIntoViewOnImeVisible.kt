/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.modifiers

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Keeps a focused field visible above the keyboard. Intended for a field — or a group that wraps
 * one — inside a scrollable container: the content is never clipped by a pinned footer, but the IME
 * is shown only after focus arrives, so we re-request bringIntoView once it is visible to scroll the
 * content back into the reduced viewport.
 *
 * Tracks focus via `hasFocus` rather than `isFocused`, so it also works when applied to a wrapper
 * whose child — rather than the wrapper itself — takes focus.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Modifier.bringIntoViewOnImeVisible(): Modifier {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var hasFocus by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible, hasFocus) {
        if (isImeVisible && hasFocus) {
            // Delay to ensure the keyboard is fully shown before scrolling the content into view.
            delay(100.milliseconds)
            bringIntoViewRequester.bringIntoView()
        }
    }
    return this
        .bringIntoViewRequester(bringIntoViewRequester)
        .onFocusChanged { hasFocus = it.hasFocus }
}
