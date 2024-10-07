/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.testutils.lambda

fun lambdaError(
    message: String = "This lambda should never be called."
): Nothing {
    throw AssertionError(message)
}
