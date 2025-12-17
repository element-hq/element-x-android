/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.util

import org.matrix.rustcomponents.sdk.Disposable

/**
 * Call destroy on all elements of the iterable.
 */
internal fun Iterable<Disposable>.destroyAll() = forEach { it.destroy() }
