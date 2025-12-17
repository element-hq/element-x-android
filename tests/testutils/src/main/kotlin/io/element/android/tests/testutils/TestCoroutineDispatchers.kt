/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.tests.testutils

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Create a [CoroutineDispatchers] instance for testing.
 *
 * @param useUnconfinedTestDispatcher If true, use [UnconfinedTestDispatcher] for all dispatchers.
 * If false, use [StandardTestDispatcher] for all dispatchers.
 */
fun TestScope.testCoroutineDispatchers(
    useUnconfinedTestDispatcher: Boolean = false,
): CoroutineDispatchers = when (useUnconfinedTestDispatcher) {
    true -> CoroutineDispatchers(
        io = UnconfinedTestDispatcher(testScheduler),
        computation = UnconfinedTestDispatcher(testScheduler),
        main = UnconfinedTestDispatcher(testScheduler),
    )
    false -> CoroutineDispatchers(
        io = StandardTestDispatcher(testScheduler),
        computation = StandardTestDispatcher(testScheduler),
        main = StandardTestDispatcher(testScheduler),
    )
}
