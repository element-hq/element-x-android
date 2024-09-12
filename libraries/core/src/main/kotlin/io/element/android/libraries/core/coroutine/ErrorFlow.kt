/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.core.coroutine

import kotlinx.coroutines.flow.flow

/** Create a Flow emitting a single error event. It should be useful for tests. */
fun <T> errorFlow(throwable: Throwable) = flow<T> { throw throwable }
