/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.lambda

fun lambdaError(
    message: String = "This lambda should never be called."
): Nothing {
    // Throwing an exception here is not enough, it can be caught.
    // Instead exit the process to make sure the test fails.
    // The error will be:
    // "Could not stop all services."
    // In this case, put a breakpoint here and run the test in debug mode to identify which lambda is failing.
    throw AssertionError(message)
}
