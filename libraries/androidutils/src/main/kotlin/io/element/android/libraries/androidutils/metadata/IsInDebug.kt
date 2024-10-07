/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.metadata

import io.element.android.libraries.androidutils.BuildConfig

/**
 * true if the app is built in debug mode.
 * For testing purpose, this can be changed with [withReleaseBehavior].
 */
var isInDebug: Boolean = BuildConfig.DEBUG
    private set

/**
 * Run the lambda simulating the app is in release mode.
 *
 * **IMPORTANT**: this should **ONLY** be used for testing purposes.
 */
fun withReleaseBehavior(lambda: () -> Unit) {
    isInDebug = false
    lambda()
    isInDebug = BuildConfig.DEBUG
}
