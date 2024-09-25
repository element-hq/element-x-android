/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.testutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

/**
 * Run a test with a [CoroutineScope] that will be cancelled automatically and avoiding failing the test.
 */
fun runCancellableScopeTest(block: suspend (CoroutineScope) -> Unit) = runTest {
    val scope = CoroutineScope(coroutineContext + SupervisorJob())
    block(scope)
    scope.cancel()
}

/**
 * Run a test with a [CoroutineScope] that will be cancelled automatically and avoiding failing the test.
 */
fun runCancellableScopeTestWithTestScope(block: suspend (testScope: TestScope, cancellableScope: CoroutineScope) -> Unit) = runTest {
    val scope = CoroutineScope(coroutineContext + SupervisorJob())
    block(this, scope)
    scope.cancel()
}
