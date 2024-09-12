/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.testutils

import io.element.android.libraries.androidutils.metadata.isInDebug
import org.junit.Assert.assertThrows

/**
 * Assert that the lambda throws only on debug mode.
 */
fun assertThrowsInDebug(lambda: () -> Any?) {
    if (isInDebug) {
        assertThrows(IllegalStateException::class.java) {
            lambda()
        }
    } else {
        lambda()
    }
}
