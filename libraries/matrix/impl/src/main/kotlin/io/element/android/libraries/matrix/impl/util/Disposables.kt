/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import org.matrix.rustcomponents.sdk.Disposable

/**
 * Call destroy on all elements of the iterable.
 */
internal fun Iterable<Disposable>.destroyAll() = forEach { it.destroy() }
