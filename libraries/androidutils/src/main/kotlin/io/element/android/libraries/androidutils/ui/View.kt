/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.ui

import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

fun View.hideKeyboard() {
    val imm = context?.getSystemService<InputMethodManager>()
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

suspend fun View.hideKeyboardAndAwaitAnimation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !rootWindowInsets.isVisible(WindowInsets.Type.ime())) {
        // Keyboard is already hidden, no need to do anything
        return
    }

    val imm = context?.getSystemService<InputMethodManager>() ?: return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (imm.hideSoftInputFromWindow(windowToken, 0)) {
            // Suspend until the first frame in which the window insets no longer contain the keyboard. Observation runs on the view tree by
            // design: a view exposes only a single OnApplyWindowInsetsListener slot, and Compose already occupies that slot on its root
            // view. Replacing the listener there, even temporarily, prevents imePadding() and related APIs from receiving further insets
            // updates for the remainder of the Activity's lifetime.
            withTimeoutOrNull(1.seconds) {
                awaitPreDraw { rootWindowInsets?.isVisible(WindowInsets.Type.ime()) != true }
            }
        }
    } else {
        val future = CompletableDeferred<Unit>()
        @Suppress("DEPRECATION")
        val requested = imm.hideSoftInputFromWindow(windowToken, 0, object : ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN || resultCode == InputMethodManager.RESULT_HIDDEN) {
                    future.complete(Unit)
                }
            }
        })
        if (requested) {
            // Await the future to ensure the keyboard hide animation has completed before proceeding
            withTimeoutOrNull(1.seconds) { future.await() }
        }
    }
}

/**
 * Suspends until a drawing pass of the view tree is about to begin while [condition] holds. The condition is
 * evaluated before every frame via the [ViewTreeObserver], which accepts any number of listeners and therefore
 * leaves every other observer of the view undisturbed.
 */
internal suspend fun View.awaitPreDraw(condition: () -> Boolean) = suspendCancellableCoroutine { continuation ->
    // Retained for later removal: once the view has been attached, the observer it held while detached is no longer alive.
    val observer = viewTreeObserver
    fun remove(listener: ViewTreeObserver.OnPreDrawListener) {
        (if (observer.isAlive) observer else viewTreeObserver).removeOnPreDrawListener(listener)
    }
    val listener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (condition()) {
                remove(this)
                continuation.resume(Unit)
            }
            return true
        }
    }
    observer.addOnPreDrawListener(listener)
    continuation.invokeOnCancellation { remove(listener) }
}

fun View.showKeyboard(andRequestFocus: Boolean = false) {
    if (andRequestFocus) {
        requestFocus()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        windowInsetsController?.show(WindowInsets.Type.ime())
    } else {
        val imm = context?.getSystemService<InputMethodManager>()
        @Suppress("DEPRECATION")
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
