/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.ui

import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun View.hideKeyboard() {
    val imm = context?.getSystemService<InputMethodManager>()
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard(andRequestFocus: Boolean = false) {
    if (andRequestFocus) {
        requestFocus()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowInsetsController?.show(WindowInsets.Type.ime())
    } else {
        val imm = context?.getSystemService<InputMethodManager>()
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.isKeyboardVisible(): Boolean {
    val imm = context?.getSystemService<InputMethodManager>()
    return imm?.isAcceptingText == true
}

suspend fun View.awaitWindowFocus() = suspendCancellableCoroutine { continuation ->
    if (hasWindowFocus()) {
        continuation.resume(Unit)
    } else {
        val listener = object : ViewTreeObserver.OnWindowFocusChangeListener {
            override fun onWindowFocusChanged(hasFocus: Boolean) {
                if (hasFocus) {
                    viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    continuation.resume(Unit)
                }
            }
        }

        viewTreeObserver.addOnWindowFocusChangeListener(listener)

        continuation.invokeOnCancellation {
            viewTreeObserver.removeOnWindowFocusChangeListener(listener)
        }
    }
}
